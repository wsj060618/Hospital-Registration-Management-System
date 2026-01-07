package Dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * 挂号记录数据访问层，处理挂号记录的插入、查询操作
 */
public class RegistrationRecordDao {

    /**
     * 新增挂号记录 + 扣减号源余量（事务绑定操作）
     * @param conn 数据库连接（需开启事务，外部统一提交/回滚）
     * @param patientId 患者ID（关联patient表的patientId）
     * @param slotId 号源ID（关联appointment_slot表的slotId）
     * @param registrationNo 挂号单号（唯一）
     * @param registrationTime 挂号时间
     * @param status 挂号状态（未就诊/已就诊/已取消）
     * @return true-挂号成功，false-号源已被抢完
     * @throws SQLException 数据库操作异常
     */
    public boolean addRegistration(Connection conn, Long patientId, Long slotId,
                                   String registrationNo, Date registrationTime, String status) throws SQLException {
        // 非空校验
        if (conn == null) {
            throw new SQLException("数据库连接不能为空");
        }
        if (patientId == null || slotId == null || registrationNo == null || registrationTime == null || status == null) {
            throw new SQLException("挂号参数不能为空");
        }

        // 1. 插入挂号记录
        String insertSql = "INSERT INTO registration_record " +
                "(patientId, slotId, registrationNo, registrationTime, status) " +
                "VALUES (?, ?, ?, ?, ?)";
        PreparedStatement insertPstmt = conn.prepareStatement(insertSql);
        insertPstmt.setLong(1, patientId);
        insertPstmt.setLong(2, slotId);
        insertPstmt.setString(3, registrationNo);
        insertPstmt.setTimestamp(4, new java.sql.Timestamp(registrationTime.getTime()));
        insertPstmt.setString(5, status);
        int insertRows = insertPstmt.executeUpdate();
        insertPstmt.close();

        if (insertRows != 1) {
            return false; // 插入挂号记录失败
        }

        // 2. 扣减号源余量（调用AppointmentSlotDao的方法）
        AppointmentSlotDao slotDao = new AppointmentSlotDao();
        int updateRows = slotDao.decreaseRemainingNum(conn, slotId);
        if (updateRows != 1) {
            return false; // 号源已被抢完，事务回滚
        }

        return true; // 挂号成功
    }

    /**
     * 根据患者ID查询挂号记录（扩展方法，供患者查询历史挂号）
     * @param conn 数据库连接
     * @param patientId 患者ID
     * @return 挂号记录结果集
     * @throws SQLException 数据库操作异常
     */
    public ResultSet listByPatientId(Connection conn, Long patientId) throws SQLException {
        if (conn == null) {
            throw new SQLException("数据库连接不能为空");
        }
        if (patientId == null) {
            throw new SQLException("患者ID不能为空");
        }

        String sql = "SELECT r.recordId, r.registrationNo, r.registrationTime, r.status, " +
                "d.name AS doctorName, d.title AS doctorTitle, " +
                "a.visitDate, a.timeSlot " +
                "FROM registration_record r " +
                "LEFT JOIN appointment_slot a ON r.slotId = a.slotId " +
                "LEFT JOIN doctor d ON a.doctorId = d.doctorId " +
                "WHERE r.patientId = ? " +
                "ORDER BY r.registrationTime DESC";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setLong(1, patientId);
        return pstmt.executeQuery();
    }

    /**
     * 根据医生ID、日期和患者姓名查询挂号记录（扩展方法，供医生查询排班）
     * @param conn 数据库连接
     * @param doctorId 医生ID
     * @param dateStr 日期字符串（格式：yyyy-MM-dd）
     * @param patientName 患者姓名（可选，用于模糊查询）
     * @return 挂号记录结果集
     * @throws SQLException 数据库操作异常
     */
    public ResultSet listByDoctorAndDate(Connection conn, Long doctorId, String dateStr, String patientName) throws SQLException {
        // 将日期筛选条件应用到就诊日期（visitDate）而非挂号日期（registrationTime）
        String sql = "SELECT r.*, s.visitDate, s.timeSlot FROM registration_record r " +
                "LEFT JOIN appointment_slot s ON r.slotId = s.slotId " +
                "LEFT JOIN patient p ON r.patientId = p.patientId " +
                "WHERE s.doctorId = ? AND DATE(s.visitDate) = ? AND r.status != '已取消' ";
        
        // 如果提供了患者姓名，则添加模糊查询条件
        if (patientName != null && !patientName.trim().isEmpty()) {
            sql += " AND p.name LIKE ? ";
        }
        
        sql += "ORDER BY r.registrationTime";
        
        PreparedStatement pstmt = conn.prepareStatement(sql);
        int paramIndex = 1;
        pstmt.setLong(paramIndex++, doctorId);
        pstmt.setString(paramIndex++, dateStr);
        
        // 如果提供了患者姓名，则设置参数
        if (patientName != null && !patientName.trim().isEmpty()) {
            pstmt.setString(paramIndex++, "%" + patientName.trim() + "%");
        }
        
        return pstmt.executeQuery();
    }

    /**
     * 根据挂号单号查询挂号记录ID（扩展方法，供更新状态时使用）
     * @param conn 数据库连接
     * @param registrationNo 挂号单号
     * @return 挂号记录ID（如果存在），否则返回null
     * @throws SQLException 数据库操作异常
     */
    public Long getRecordIdByNo(Connection conn, String registrationNo) throws SQLException {
        String sql = "SELECT recordId FROM registration_record WHERE registrationNo = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, registrationNo);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            return rs.getLong("recordId");
        }
        return null;
    }

    /**
     * 更新挂号状态（扩展方法，供管理员更新挂号状态）
     * @param conn 数据库连接
     * @param recordId 挂号记录ID
     * @param status 新状态
     * @return 是否更新成功
     * @throws SQLException 数据库操作异常
     */
    public boolean updateStatus(Connection conn, Long recordId, String status) throws SQLException {
        String sql = "UPDATE registration_record SET status = ? WHERE recordId = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, status);
        pstmt.setLong(2, recordId);
        return pstmt.executeUpdate() > 0;
    }
}