package Dao;

import Model.Doctor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 医生数据访问层，处理医生表的CRUD操作
 */
public class DoctorDao {
    /**
     * 根据科室ID查询该科室下的所有医生
     * @param conn 数据库连接
     * @param deptId 科室ID（关联department表的deptId）
     * @return 医生结果集（含医生ID、姓名、职称、执业证书号）
     * @throws SQLException 数据库操作异常
     */
    public ResultSet listByDeptId(Connection conn, Long deptId) throws SQLException {
        // 非空校验
        if (conn == null) {
            throw new SQLException("数据库连接不能为空");
        }
        if (deptId == null) {
            throw new SQLException("科室ID不能为空");
        }

        // SQL查询：关联科室ID，按医生姓名排序
        String sql = "SELECT doctorId, name, title, licenseNo, deptId FROM doctor WHERE deptId = ? ORDER BY name";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setLong(1, deptId);
        return pstmt.executeQuery();
    }

    /**
     * 根据医生ID查询单个医生（扩展方法，供接诊、号源关联使用）
     * @param conn 数据库连接
     * @param doctorId 医生ID
     * @return 医生结果集
     * @throws SQLException 数据库操作异常
     */
    public ResultSet getById(Connection conn, Long doctorId) throws SQLException {
        if (conn == null) {
            throw new SQLException("数据库连接不能为空");
        }
        if (doctorId == null) {
            throw new SQLException("医生ID不能为空");
        }

        String sql = "SELECT doctorId, name, title, licenseNo, deptId FROM doctor WHERE doctorId = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setLong(1, doctorId);
        return pstmt.executeQuery();
    }
    
    /**
     * 查询所有医生
     * @param conn 数据库连接
     * @return 所有医生的结果集
     * @throws SQLException 数据库操作异常
     */
    public ResultSet listAll(Connection conn) throws SQLException {
        // 非空校验
        if (conn == null) {
            throw new SQLException("数据库连接不能为空");
        }

        // SQL查询所有医生（按姓名排序）
        String sql = "SELECT doctorId, name, title, licenseNo, deptId FROM doctor ORDER BY name";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        return pstmt.executeQuery();
    }
    
    /**
     * 根据姓名模糊查询医生
     * @param conn 数据库连接
     * @param name 医生姓名
     * @return 符合条件的医生结果集
     * @throws SQLException 数据库操作异常
     */
    public ResultSet listByName(Connection conn, String name) throws SQLException {
        // 非空校验
        if (conn == null) {
            throw new SQLException("数据库连接不能为空");
        }

        String sql = "SELECT doctorId, name, title, licenseNo, deptId FROM doctor WHERE name LIKE ? ORDER BY name";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, "%" + name + "%");
        return pstmt.executeQuery();
    }
    
    /**
     * 新增医生
     * @param conn 数据库连接
     * @param doctor 医生对象
     * @return 新增医生的ID
     * @throws SQLException 数据库操作异常
     */
    public int add(Connection conn, Doctor doctor) throws SQLException {
        // 非空校验
        if (conn == null) {
            throw new SQLException("数据库连接不能为空");
        }
        if (doctor == null) {
            throw new SQLException("医生对象不能为空");
        }

        String sql = "INSERT INTO doctor(name, title, licenseNo, deptId) VALUES(?, ?, ?, ?)";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, doctor.getName());
        pstmt.setString(2, doctor.getTitle());
        pstmt.setString(3, doctor.getLicenseNo());
        pstmt.setLong(4, doctor.getDeptId());
        return pstmt.executeUpdate();
    }
    
    /**
     * 根据医生ID删除医生
     * @param conn 数据库连接
     * @param doctorId 医生ID
     * @return 删除的医生数量
     * @throws SQLException 数据库操作异常
     */
    public int deleteById(Connection conn, Long doctorId) throws SQLException {
        // 非空校验
        if (conn == null) {
            throw new SQLException("数据库连接不能为空");
        }
        if (doctorId == null) {
            throw new SQLException("医生ID不能为空");
        }

        String sql = "DELETE FROM doctor WHERE doctorId = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setLong(1, doctorId);
        return pstmt.executeUpdate();
    }
}