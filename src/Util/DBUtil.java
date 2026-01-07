// 修改后的DBUtil.java
package Util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBUtil {
    // 连接字符串：主机名、数据库名称、端口号、数据库字符编码、时区设置
    public static final String URL = "jdbc:mysql://localhost:3306/hospital_registration_management_system?useUnicode=true&characterEncoding=utf8&serverTimezone=UTC";
    // 数据库用户名
    public static final String USERNAME = "root";
    // 数据库密码
    public static final String PASSWORD = "060618";

    // 静态代码块加载驱动
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("数据库驱动加载成功");
        } catch (ClassNotFoundException e) {
            System.out.println("数据库驱动加载失败");
            e.printStackTrace();
        }
    }

    // 获取mysql连接对象
    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (SQLException e) {
            System.out.println("获取数据库连接对象失败");
            e.printStackTrace();
            return null;
        }
    }
    
    // 关闭数据库连接资源
    public static void close(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 测试数据库连接
    public static void main(String[] args) {
        Connection conn = null;
        try {
            conn = getConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (conn != null) {
            System.out.println("数据库连接成功");
        } else {
            System.out.println("数据库连接失败");
        }
    }
}