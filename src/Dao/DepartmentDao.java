package Dao;

import Model.Department;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 科室数据访问层，处理科室表的查询操作
 */
public class DepartmentDao {

    /**
     * 查询所有科室（供挂号界面加载科室下拉框）
     * @param conn 数据库连接（外部传入，统一管理生命周期）
     * @return 所有科室的ResultSet结果集（需在调用层关闭）
     * @throws SQLException 数据库操作异常
     */
    public ResultSet listAll(Connection conn) throws SQLException {
        // 非空校验
        if (conn == null) {
            throw new SQLException("数据库连接不能为空");
        }

        // SQL查询所有科室（按科室名称排序）
        String sql = "SELECT deptId, deptName, description FROM department ORDER BY deptName";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        return pstmt.executeQuery();
    }

    /**
     * 根据科室ID查询单个科室（扩展方法，可选）
     * @param conn 数据库连接
     * @param deptId 科室ID
     * @return 科室结果集
     * @throws SQLException 数据库操作异常
     */
    public ResultSet getById(Connection conn, Long deptId) throws SQLException {
        if (conn == null) {
            throw new SQLException("数据库连接不能为空");
        }
        if (deptId == null) {
            throw new SQLException("科室ID不能为空");
        }

        String sql = "SELECT deptId, deptName, description FROM department WHERE deptId = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setLong(1, deptId);
        return pstmt.executeQuery();
    }

    /**
     * 新增科室（用于管理员添加新科室）
     * @param conn 数据库连接
     * @param dept 科室实体对象（包含deptName和description）
     * @return 受影响的行数（成功新增返回1）
     * @throws SQLException 数据库操作异常
     */
    public int add(Connection conn, Department dept) throws SQLException {
        String sql = "INSERT INTO department(deptName, description) VALUES(?, ?)";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, dept.getDeptName());
        pstmt.setString(2, dept.getDescription());
        return pstmt.executeUpdate();
    }

    /**
     * 根据科室ID删除科室
     * @param conn 数据库连接
     * @param deptId 科室ID
     * @return 受影响的行数
     * @throws SQLException 数据库操作异常
     */
    public int deleteById(Connection conn, Long deptId) throws SQLException {
        String sql = "DELETE FROM department WHERE deptId = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setLong(1, deptId);
        return pstmt.executeUpdate();
    }

    /**
     * 根据科室名称模糊搜索科室
     * @param conn 数据库连接
     * @param deptName 科室名称关键词
     * @return 科室结果集
     * @throws SQLException 数据库操作异常
     */
    public ResultSet listByName(Connection conn, String deptName) throws SQLException {
        if (conn == null) {
            throw new SQLException("数据库连接不能为空");
        }
    
        String sql = "SELECT deptId, deptName, description FROM department WHERE deptName LIKE ? ORDER BY deptName";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, "%" + deptName + "%");
        return pstmt.executeQuery();
    }

}