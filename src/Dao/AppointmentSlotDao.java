package Dao;

import Model.AppointmentSlot;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 号源数据访问层，处理号源表的查询、更新操作
 */
public class AppointmentSlotDao {
    /**
     * 根据医生ID和当前完整时间查询可用号源
     * 适配timeSlot格式：上午9:00-10:00 / 下午2:00-3:00（区分上下午，12小时制转24小时制）
     * 修复：确保SQL占位符数量和参数设置数量一致
     * @param conn 数据库连接
     * @param doctorId 医生ID
     * @param currentDateTimeStr 当前完整时间字符串（格式：yyyy-MM-dd HH:mm:ss）
     * @return 可用号源结果集
     * @throws SQLException 数据库操作异常
     */
    public ResultSet listByDoctorId(Connection conn, Long doctorId, String currentDateTimeStr) throws SQLException {
        if (conn == null) {
            throw new SQLException("数据库连接不能为空");
        }
        if (doctorId == null) {
            throw new SQLException("医生ID不能为空");
        }
        if (currentDateTimeStr == null || currentDateTimeStr.isEmpty()) {
            throw new SQLException("当前时间字符串不能为空");
        }

        // 核心修复：确保SQL中有2个占位符（?），分别对应doctorId和currentDateTimeStr
        String sql = "SELECT slotId, visitDate, timeSlot, totalNum, remainingNum, doctorId " +
                "FROM appointment_slot " +
                "WHERE doctorId = ? " +
                "AND remainingNum > 0 " +
                "AND STR_TO_DATE( " +
                "    CONCAT( " +
                "        visitDate, ' ', " +
                "        DATE_FORMAT(STR_TO_DATE(SUBSTRING_INDEX(timeSlot, '-', 1), '%H:%i'), '%H:%i:%s') " +
                "    ), '%Y-%m-%d %H:%i:%s' " +
                ") >= STR_TO_DATE(?, '%Y-%m-%d %H:%i:%s') " +
                "ORDER BY visitDate, timeSlot";

        PreparedStatement pstmt = conn.prepareStatement(sql);
        // 按顺序设置参数：第1个?对应doctorId，第2个?对应currentDateTimeStr
        pstmt.setLong(1, doctorId);
        pstmt.setString(2, currentDateTimeStr);
        return pstmt.executeQuery();
    }
    /**
     * 扣减号源余量（挂号成功后调用，需在事务中执行）
     * @param conn 数据库连接（需开启事务）
     * @param slotId 号源ID
     * @return 受影响行数（1表示成功，0表示号源已被抢完）
     * @throws SQLException 数据库操作异常
     */
    public int decreaseRemainingNum(Connection conn, Long slotId) throws SQLException {
        if (conn == null) {
            throw new SQLException("数据库连接不能为空");
        }
        if (slotId == null) {
            throw new SQLException("号源ID不能为空");
        }

        // 乐观锁：更新时校验剩余号源>0，防止并发冲突
        String sql = "UPDATE appointment_slot " +
                "SET remainingNum = remainingNum - 1 " +
                "WHERE slotId = ? AND remainingNum > 0";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setLong(1, slotId);
        return pstmt.executeUpdate();
    }

    /**
     * 查询所有号源（按出诊日期+时段排序）
     * @param conn 数据库连接
     * @return 所有号源结果集
     * @throws SQLException 数据库操作异常
     */
    public ResultSet listAll(Connection conn) throws SQLException {
        String sql = "SELECT * FROM appointment_slot ORDER BY visitDate, timeSlot";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        return pstmt.executeQuery();
    }

    /**
     * 新增号源（管理员操作）
     * @param conn 数据库连接
     * @param slot 号源实体对象
     * @return 受影响行数（1表示成功）
     * @throws SQLException 数据库操作异常
     */
    public int add(Connection conn, AppointmentSlot slot) throws SQLException {
        String sql = "INSERT INTO appointment_slot(visitDate, timeSlot, totalNum, remainingNum, doctorId) VALUES(?, ?, ?, ?, ?)";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setDate(1, new java.sql.Date(slot.getVisitDate().getTime()));
        pstmt.setString(2, slot.getTimeSlot());
        pstmt.setInt(3, slot.getTotalNum());
        pstmt.setInt(4, slot.getRemainingNum());
        pstmt.setLong(5, slot.getDoctorId());
        return pstmt.executeUpdate();
    }

    /**
     * 根据日期筛选号源
     * @param conn 数据库连接
     * @param dateStr 日期字符串（格式：yyyy-MM-dd）
     * @return 筛选后的号源结果集
     * @throws SQLException 数据库操作异常
     */
    public ResultSet listByDate(Connection conn, String dateStr) throws SQLException {
        if (conn == null) {
            throw new SQLException("数据库连接不能为空");
        }
        if (dateStr == null || dateStr.isEmpty()) {
            throw new SQLException("日期字符串不能为空");
        }
    
        String sql = "SELECT * FROM appointment_slot WHERE DATE(visitDate) = ? ORDER BY visitDate, timeSlot";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, dateStr);
        return pstmt.executeQuery();
    }

    /**
     * 根据号源ID删除号源
     * @param conn 数据库连接
     * @param slotId 号源ID
     * @return 删除的号源数量
     * @throws SQLException 数据库操作异常
     */
    public int deleteById(Connection conn, Long slotId) throws SQLException {
        if (conn == null) {
            throw new SQLException("数据库连接不能为空");
        }
        if (slotId == null) {
            throw new SQLException("号源ID不能为空");
        }
        
        String sql = "DELETE FROM appointment_slot WHERE slotId = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setLong(1, slotId);
        return pstmt.executeUpdate();
    }
}