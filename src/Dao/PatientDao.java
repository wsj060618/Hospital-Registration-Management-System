package Dao;

import Model.Patient;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PatientDao {
    /**
     * 根据姓名模糊查询患者
     * @param conn 数据库连接
     * @param name 患者姓名
     * @return 符合条件的患者结果集
     */
    public ResultSet listByName(Connection conn, String name) throws SQLException {
        String sql = "SELECT * FROM patient WHERE name LIKE ? ORDER BY createTime DESC";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, "%" + name + "%");
        return pstmt.executeQuery();
    }

    /**
     * 根据患者ID查询患者信息
     * @param conn 数据库连接
     * @param patientId 患者ID
     * @return 符合条件的患者对象
     */
    public Patient getById(Connection conn, Long patientId) throws SQLException {
        String sql = "SELECT * FROM patient WHERE patientId = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setLong(1, patientId);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            Patient patient = new Patient();
            patient.setPatientId(rs.getLong("patientId"));
            patient.setName(rs.getString("name"));
            patient.setGender(rs.getInt("gender"));
            patient.setIdCard(rs.getString("idCard"));
            patient.setPhone(rs.getString("phone"));
            patient.setCreateTime(rs.getTimestamp("createTime"));
            return patient;
        }
        return null;
    }

    /**
     * 新增患者
     * @param conn 数据库连接
     * @param patient 患者对象
     * @return 新增患者的ID
     */
    public int add(Connection conn, Patient patient) throws SQLException {
        String sql = "INSERT INTO patient(idCard, name, gender, phone, createTime) VALUES(?, ?, ?, ?, ?)";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, patient.getIdCard());
        pstmt.setString(2, patient.getName());
        pstmt.setInt(3, patient.getGender());
        pstmt.setString(4, patient.getPhone());
        pstmt.setTimestamp(5, new java.sql.Timestamp(patient.getCreateTime().getTime()));
        return pstmt.executeUpdate();
    }

    /**
     * 根据患者ID删除患者
     * @param conn 数据库连接
     * @param patientId 患者ID
     * @return 删除的患者数量
     */
    public int deleteById(Connection conn, Long patientId) throws SQLException {
        String sql = "DELETE FROM patient WHERE patientId = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setLong(1, patientId);
        return pstmt.executeUpdate();
    }

    /**
     * 根据姓名删除患者
     * @param conn 数据库连接
     * @param name 患者姓名
     * @return 删除的患者数量
     */
    public int deleteByName(Connection conn, String name) throws SQLException {
        String sql = "DELETE FROM patient WHERE name = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, name);
        return pstmt.executeUpdate();
    }
}