package Dao;

import Model.Users;
import Util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDao {
    /**
     * 管理员 / 医生登录方法
     * @param conn 数据库连接
     * @param username 用户名
     * @param password 密码
     * @param identity 身份类型
     * @return ResultSet 登录结果集
     */
    public ResultSet login (Connection conn, String username, String password, String identity) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ? AND identity = ?";
        try {
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, username);
            pst.setString(2, password);
            pst.setString(3, identity);
            return pst.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 患者登录方法
     * @param conn 数据库连接
     * @param idCard 身份证号
     * @param name 姓名
     * @param gender 性别
     * @param phone 手机号
     * @return Long 患者ID（0: 数据库错误； 其他值: 患者ID）
     */
    public Long loginPatient (Connection conn, String idCard, String name, int gender, String phone) {
        // 查询患者是否存在
        String checkSql = "SELECT * FROM patient WHERE idCard = ?";
        try {
            PreparedStatement pst = conn.prepareStatement(checkSql);
            pst.setString(1, idCard);
            ResultSet q_rs = pst.executeQuery();
            
            // 患者存在
            if (q_rs.next()) {
                // 更新患者信息
                String updateSql = "UPDATE patient SET name = ?, gender = ?, phone = ? WHERE idCard = ?";
                pst = conn.prepareStatement(updateSql);
                pst.setString(1, name);
                pst.setInt(2, gender);
                pst.setString(3, phone);
                pst.setString(4, idCard);
                pst.executeUpdate();
                // 返回患者ID
                return q_rs.getLong("patientId");
            } else {
                // 患者不存在，插入数据
                String insertSql = "INSERT INTO patient (idCard, name, gender, phone) VALUES (?, ?, ?, ?)";
                pst = conn.prepareStatement(insertSql, PreparedStatement.RETURN_GENERATED_KEYS);
                pst.setString(1, idCard);
                pst.setString(2, name);
                pst.setInt(3, gender);
                pst.setString(4, phone);
                pst.executeUpdate();
                
                // 获取自动生成的患者ID
                ResultSet generatedKeys = pst.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                }
                return 0L; // 插入成功但获取ID失败
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return 0L; // 数据库错误
        }
    }

    /**
     * 查询所有用户
     * @param conn 数据库连接
     * @return ResultSet 所有用户结果集
     * @throws SQLException 数据库操作异常
     */
    public ResultSet listAllUsers(Connection conn) throws SQLException {
        String sql = "SELECT * FROM users ORDER BY identity, username";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        return pstmt.executeQuery();
    }

    /**
     * 新增用户
     * @param conn 数据库连接
     * @param user 用户对象
     * @return 新增用户的数量
     * @throws SQLException 数据库操作异常
     */
    public int addUser(Connection conn, Users user) throws SQLException {
        String sql = "INSERT INTO users(username, password, identity) VALUES(?, ?, ?)";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, user.getUsername());
        pstmt.setString(2, user.accessGetPassword(this));
        pstmt.setString(3, user.getIdentity());
        return pstmt.executeUpdate();
    }

    /**
     * 修改用户信息
     * @param conn 数据库连接
     * @param originalUsername 原用户名
     * @param user 用户对象（包含新的用户名、密码和身份）
     * @return 修改的用户数量
     * @throws SQLException 数据库操作异常
     */
    public int updateUser(Connection conn, String originalUsername, Users user) throws SQLException {
        String sql = "UPDATE users SET username = ?, password = ?, identity = ? WHERE username = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, user.getUsername());
        pstmt.setString(2, user.accessGetPassword(this));
        pstmt.setString(3, user.getIdentity());
        pstmt.setString(4, originalUsername);
        return pstmt.executeUpdate();
    }

    /**
     * 根据用户名删除用户
     * @param conn 数据库连接
     * @param username 用户名
     * @return 删除用户的数量
     * @throws SQLException 数据库操作异常
     */
    public int deleteUserByUsername(Connection conn, String username) throws SQLException {
        String sql = "DELETE FROM users WHERE username = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, username);
        return pstmt.executeUpdate();
    }

    /**
     * 根据用户名模糊搜索用户
     * @param conn 数据库连接
     * @param username 用户名关键词
     * @return 用户结果集
     * @throws SQLException 数据库操作异常
     */
    public ResultSet listByUsername(Connection conn, String username) throws SQLException {
        if (conn == null) {
            throw new SQLException("数据库连接不能为空");
        }

        String sql = "SELECT * FROM users WHERE username LIKE ? ORDER BY identity, username";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, "%" + username + "%");
        return pstmt.executeQuery();
    }

    /**
     * 主方法：测试登录方法
     */
    public static void main(String[] args) {
        UserDao userDao = new UserDao();
        Connection conn = DBUtil.getConnection();
        ResultSet rs = userDao.login(conn, "admin", "123456", "admin");
        try  {
           if (rs.next()) {
               String username = rs.getString("username");
               String password = rs.getString("password");
               String identity = rs.getString("identity");
               System.out.println(username);
               System.out.println(password);
               System.out.println(identity);
           }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                rs.close();
                conn.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}