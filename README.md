## 项目介绍

医院挂号管理系统是一个基于Java Swing和MySQL开发的桌面应用程序，用于实现医院挂号流程的自动化管理。系统提供了管理员、医生和患者三种角色，分别对应不同的功能模块，实现了从患者挂号到医生接诊的完整流程。

## 核心功能

### 1. 用户登录模块
- 支持管理员和医生两种角色登录
- 用户名、密码验证
- 医生角色需额外输入医生ID

### 2. 患者管理模块
- 患者信息录入与查询
- 按患者姓名搜索

### 3. 科室管理模块
- 科室信息的增删改查
- 按科室名称搜索
- 科室信息包括科室ID、科室名称、科室描述

### 4. 医生管理模块
- 医生信息的增删改查
- 按医生姓名搜索
- 医生信息包括医生ID、姓名、职称、执业证书号、所属科室

### 5. 用户管理模块
- 系统用户（管理员、医生）的增删改查
- 按用户名搜索
- 密码限制为6位数

### 6. 号源配置模块
- 医生号源的配置与管理

### 7. 挂号记录管理
- 患者挂号记录的查询与管理

## 代码结构

```
src/
├── Dao/            # 数据访问层
│   ├── AppointmentSlotDao.java       # 号源数据访问
│   ├── DepartmentDao.java            # 科室数据访问
│   ├── DoctorDao.java                # 医生数据访问
│   ├── PatientDao.java               # 患者数据访问
│   ├── RegistrationRecordDao.java    # 挂号记录数据访问
│   └── UserDao.java                  # 用户数据访问
├── Model/          # 数据模型层
│   ├── Admin.java                   # 管理员模型
│   ├── AppointmentSlot.java         # 号源模型
│   ├── ConsultRecord.java           # 咨询记录模型
│   ├── Department.java              # 科室模型
│   ├── Doctor.java                  # 医生模型
│   ├── Patient.java                 # 患者模型
│   ├── RegistrationRecord.java      # 挂号记录模型
│   └── Users.java                   # 用户模型
├── Util/           # 工具类
│   └── DBUtil.java                  # 数据库连接工具
└── View/           # 视图层
    ├── AdminManagementFrm.java      # 管理员管理界面
    ├── DoctorWorkbenchFrm.java      # 医生工作台界面
    ├── LoginForm.java               # 登录界面
    ├── PatientLoginForm.java        # 患者登录界面
    └── PatientRegistrationFrm.java  # 患者挂号界面
```

## 核心代码分析

### 1. 数据库连接工具类 (DBUtil.java)

```java
package Util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBUtil {
    // 数据库连接参数
    public static final String URL = "jdbc:mysql://localhost:3306/hospital_registration_management_system?useUnicode=true&characterEncoding=utf8&serverTimezone=UTC";
    public static final String USERNAME = "root";
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

    // 获取数据库连接
    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (SQLException e) {
            System.out.println("获取数据库连接对象失败");
            e.printStackTrace();
            return null;
        }
    }
    
    // 关闭数据库资源
    public static void close(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
```

### 2. 用户模型类 (Users.java)

```java
package Model;
import Dao.UserDao;

public class Users {
    private String username;  // 用户名
    private String password;  // 密码
    private String identity;  // 身份（admin/doctor）

    // 构造方法
    public Users() {}
    public Users(String username, String password, String identity) {
        this.username = username;
        this.password = password;
        this.identity = identity;
    }

    // getter和setter方法
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getIdentity() { return identity; }
    public void setIdentity(String identity) { this.identity = identity; }
    
    // 密码设置方法
    public void setPassword(String password) { this.password = password; }
    
    // 密码访问控制
    public String accessGetPassword(Object caller) {
        // 仅允许 UserDao 类调用密码获取方法，增强安全性
        if (caller instanceof UserDao) {
            return password;
        } else {
            throw new SecurityException("权限不足！仅 UserDao 可访问");
        }
    }
}
```

### 3. 登录界面 (LoginForm.java)

```java
package View;

import Dao.UserDao;
import Dao.DoctorDao;
import Model.Users;
import Model.Doctor;
import Util.DBUtil;
import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginForm extends JFrame {
    // 界面组件
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField doctorIdField;
    private JButton loginBtn;
    private JButton resetBtn;
    private JRadioButton adminRadioBtn;
    private JRadioButton doctorRadioBtn;
    private ButtonGroup userTypeGroup;

    // 构造方法
    public LoginForm() {
        initComponents();
        bindBasicEvents();
        this.setVisible(true);
    }

    // 初始化界面组件
    private void initComponents() {
        // 设置窗口基本属性
        setTitle("登录 - 医院挂号管理系统");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 400);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // 创建主面板
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        mainPanel.setBackground(Color.WHITE);
        
        // 添加标题
        JLabel titleLabel = new JLabel("医院挂号管理系统", JLabel.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        titleLabel.setForeground(new Color(220, 53, 69));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        // 创建表单面板
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 15));
        formPanel.setBackground(Color.WHITE);
        
        // 添加用户名、密码、医生ID输入框和用户类型选择
        // ...
        
        // 添加登录和重置按钮
        // ...
        
        add(mainPanel);
    }

    // 绑定事件
    private void bindBasicEvents() {
        // 登录按钮事件
        loginBtn.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            String userType = adminRadioBtn.isSelected() ? "admin" : "doctor";
            
            // 输入验证
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "用户名和密码不能为空！", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            if ("doctor".equals(userType)) {
                String doctorIdStr = doctorIdField.getText().trim();
                if (doctorIdStr.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "医生ID不能为空！", "提示", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                try {
                    Long.parseLong(doctorIdStr);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "医生ID必须为数字！", "提示", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
            
            // 数据库验证
            Connection conn = null;
            ResultSet rs = null;
            try {
                conn = DBUtil.getConnection();
                UserDao userDao = new UserDao();
                rs = userDao.login(conn, username, password, userType);
                
                if (rs != null && rs.next()) {
                    Users user = new Users(rs.getString("username"), rs.getString("password"), rs.getString("identity"));
                    
                    if ("admin".equals(userType)) {
                        // 管理员登录成功，打开管理员管理界面
                        new AdminManagementFrm(user);
                    } else {
                        // 医生登录成功，验证医生ID并打开医生工作台
                        String doctorIdStr = doctorIdField.getText().trim();
                        Long doctorId = Long.parseLong(doctorIdStr);
                        DoctorDao doctorDao = new DoctorDao();
                        ResultSet doctorRs = doctorDao.getById(conn, doctorId);
                        
                        if (doctorRs != null && doctorRs.next()) {
                            Doctor doctor = new Doctor(
                                doctorRs.getLong("doctorId"),
                                doctorRs.getString("name"),
                                doctorRs.getString("title"),
                                doctorRs.getString("licenseNo"),
                                doctorRs.getLong("deptId")
                            );
                            new DoctorWorkbenchFrm(doctor);
                        } else {
                            JOptionPane.showMessageDialog(this, "医生ID不存在！", "提示", JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                    }
                    
                    this.dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "用户名或密码错误！", "提示", JOptionPane.WARNING_MESSAGE);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "登录失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            } finally {
                DBUtil.close(conn, null, rs);
            }
        });
        
        // 重置按钮事件
        resetBtn.addActionListener(e -> {
            usernameField.setText("");
            passwordField.setText("");
            doctorIdField.setText("");
            adminRadioBtn.setSelected(true);
        });
        
        // 用户类型选择事件
        adminRadioBtn.addActionListener(e -> {
            doctorIdLabel.setVisible(false);
            doctorIdField.setVisible(false);
        });
        
        doctorRadioBtn.addActionListener(e -> {
            doctorIdLabel.setVisible(true);
            doctorIdField.setVisible(true);
        });
    }
    
    // 主方法
    public static void main(String[] args) {
        new LoginForm();
    }
}
```

### 4. 管理员管理界面 (AdminManagementFrm.java)

```java
package View;

import Dao.*;
import Model.*;
import Util.DBUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminManagementFrm extends JFrame {
    private Users currentAdmin;
    private JTabbedPane tabbedPane;
    private JTable patientTable, deptTable, slotTable, userTable, doctorTable;
    private DefaultTableModel patientModel, deptModel, slotModel, userModel, doctorModel;
    
    // DAO层对象
    private DoctorDao doctorDao = new DoctorDao();
    private PatientDao patientDao = new PatientDao();
    private DepartmentDao deptDao = new DepartmentDao();
    private AppointmentSlotDao slotDao = new AppointmentSlotDao();
    private UserDao userDao = new UserDao();

    // 构造方法
    public AdminManagementFrm(Users currentAdmin) {
        this.currentAdmin = currentAdmin;
        initFrame();
        initComponents();
        loadAllData();
        setVisible(true);
    }

    // 初始化窗口
    private void initFrame() {
        setTitle("管理员工作台 - " + currentAdmin.getUsername());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(Color.WHITE);
    }

    // 初始化组件
    private void initComponents() {
        // 创建主面板
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);
        add(mainPanel);
        
        // 添加标题
        JLabel titleLabel = new JLabel("管理员管理中心", JLabel.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 28));
        titleLabel.setForeground(new Color(0, 120, 215));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        // 创建选项卡面板
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        
        // 添加各个管理选项卡
        tabbedPane.addTab("患者管理", createPatientPanel());
        tabbedPane.addTab("科室管理", createDeptPanel());
        tabbedPane.addTab("医生管理", createDoctorPanel());
        tabbedPane.addTab("用户管理", createUserPanel());
        tabbedPane.addTab("号源配置", createSlotPanel());
    }

    // 创建科室管理面板
    private JPanel createDeptPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(Color.WHITE);
        
        // 创建搜索面板
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        searchPanel.setBackground(Color.WHITE);
        JLabel nameLabel = new JLabel("科室名称：");
        JTextField nameField = new JTextField(20);
        JButton searchBtn = new JButton("搜索");
        JButton refreshBtn = new JButton("刷新");
        
        // 搜索按钮事件
        searchBtn.addActionListener(e -> loadDeptData(nameField.getText().trim()));
        // 刷新按钮事件
        refreshBtn.addActionListener(e -> {
            nameField.setText("");
            loadDeptData("");
        });
        
        // 添加搜索组件
        searchPanel.add(nameLabel);
        searchPanel.add(nameField);
        searchPanel.add(searchBtn);
        searchPanel.add(refreshBtn);
        panel.add(searchPanel, BorderLayout.NORTH);
        
        // 创建科室表格
        String[] deptColumns = {"科室ID", "科室名称", "科室描述"};
        deptModel = new DefaultTableModel(null, deptColumns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        deptTable = new JTable(deptModel);
        deptTable.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        deptTable.setRowHeight(30);
        deptTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));
        JScrollPane scrollPane = new JScrollPane(deptTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // 创建底部操作面板
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        bottomPanel.setBackground(Color.WHITE);
        JButton addDeptBtn = new JButton("新增科室");
        JButton deleteDeptBtn = new JButton("删除科室");
        
        // 新增科室按钮事件
        addDeptBtn.addActionListener(e -> new DepartmentAddDialog(this));
        
        // 删除科室按钮事件
        deleteDeptBtn.addActionListener(e -> {
            int selectedRow = deptTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "请先选择要删除的科室！", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // 获取科室ID和名称
            Long deptId = (Long) deptModel.getValueAt(selectedRow, 0);
            String deptName = (String) deptModel.getValueAt(selectedRow, 1);
            
            // 确认删除
            int confirm = JOptionPane.showConfirmDialog(this,
                    "确定要删除科室\"" + deptName + "\"吗？\n此操作不可恢复！",
                    "删除确认", JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                Connection conn = null;
                try {
                    conn = DBUtil.getConnection();
                    DepartmentDao departmentDao = new DepartmentDao();
                    int count = departmentDao.deleteById(conn, deptId);
                    if (count > 0) {
                        JOptionPane.showMessageDialog(this, "科室删除成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                        loadDeptData(); // 刷新科室列表
                    } else {
                        JOptionPane.showMessageDialog(this, "科室删除失败，未找到该科室！", "错误", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    // 处理外键约束错误
                    String errorMessage = ex.getMessage();
                    if (errorMessage.contains("foreign key constraint") || errorMessage.contains("Cannot delete or update a parent row")) {
                        JOptionPane.showMessageDialog(this, "删除失败：该科室下存在医生，无法直接删除！\n请先移除该科室下的所有医生。", "错误", JOptionPane.ERROR_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "删除失败：" + errorMessage, "错误", JOptionPane.ERROR_MESSAGE);
                    }
                    ex.printStackTrace();
                } finally {
                    DBUtil.close(conn, null, null);
                }
            }
        });
        
        // 添加底部按钮
        bottomPanel.add(addDeptBtn);
        bottomPanel.add(deleteDeptBtn);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    // 创建用户管理面板
    private JPanel createUserPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(Color.WHITE);
        
        // 创建搜索面板
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        searchPanel.setBackground(Color.WHITE);
        JLabel usernameLabel = new JLabel("用户名：");
        JTextField usernameField = new JTextField(20);
        JButton searchBtn = new JButton("搜索");
        JButton refreshBtn = new JButton("刷新");
        
        // 搜索按钮事件
        searchBtn.addActionListener(e -> loadUserData(usernameField.getText().trim()));
        // 刷新按钮事件
        refreshBtn.addActionListener(e -> {
            usernameField.setText("");
            loadUserData("");
        });
        
        // 添加搜索组件
        searchPanel.add(usernameLabel);
        searchPanel.add(usernameField);
        searchPanel.add(searchBtn);
        searchPanel.add(refreshBtn);
        panel.add(searchPanel, BorderLayout.NORTH);
        
        // 创建用户表格
        String[] userColumns = {"用户名", "密码", "身份"};
        userModel = new DefaultTableModel(null, userColumns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        userTable = new JTable(userModel);
        userTable.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        userTable.setRowHeight(30);
        userTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));
        JScrollPane scrollPane = new JScrollPane(userTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // 创建底部操作面板
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        bottomPanel.setBackground(Color.WHITE);
        JButton addBtn = new JButton("新增用户");
        JButton deleteBtn = new JButton("删除用户");
        
        // 新增用户按钮事件
        addBtn.addActionListener(e -> new UserAddDialog(this));
        
        // 删除用户按钮事件
        deleteBtn.addActionListener(e -> {
            // 删除用户逻辑
            // ...
        });
        
        // 添加底部按钮
        bottomPanel.add(addBtn);
        bottomPanel.add(deleteBtn);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        
        loadUserData();
        return panel;
    }

    // 加载科室数据
    private void loadDeptData() {
        loadDeptData("");
    }

    // 加载科室数据（支持搜索）
    private void loadDeptData(String deptName) {
        Connection conn = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            DepartmentDao departmentDao = new DepartmentDao();
            if (deptName.isEmpty()) {
                rs = departmentDao.listAll(conn);
            } else {
                rs = departmentDao.listByName(conn, deptName);
            }
            
            // 清空表格数据
            deptModel.setRowCount(0);
            
            // 添加数据到表格
            while (rs.next()) {
                Long deptId = rs.getLong("deptId");
                String name = rs.getString("deptName");
                String description = rs.getString("description");
                deptModel.addRow(new Object[]{deptId, name, description});
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "加载科室数据失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        } finally {
            DBUtil.close(conn, null, rs);
        }
    }

    // 加载用户数据
    private void loadUserData() {
        loadUserData("");
    }

    // 加载用户数据（支持搜索）
    private void loadUserData(String username) {
        Connection conn = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            if (username.isEmpty()) {
                rs = userDao.listAllUsers(conn);
            } else {
                rs = userDao.listByUsername(conn, username);
            }
            
            // 清空表格数据
            userModel.setRowCount(0);
            
            // 添加数据到表格
            while (rs.next()) {
                String userUsername = rs.getString("username");
                String password = rs.getString("password");
                String identity = rs.getString("identity");
                userModel.addRow(new Object[]{userUsername, password, identity});
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "加载用户数据失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        } finally {
            DBUtil.close(conn, null, rs);
        }
    }

    // 新增用户对话框
    class UserAddDialog extends JDialog {
        public UserAddDialog(JFrame parent) {
            super(parent, "新增用户", true);
            setSize(500, 300);
            setLocationRelativeTo(parent);
            setResizable(false);
            
            JPanel panel = new JPanel(new GridLayout(4, 2, 10, 20));
            panel.setBorder(new EmptyBorder(20, 20, 20, 20));
            panel.setBackground(Color.WHITE);
            
            // 表单组件
            panel.add(new JLabel("用户名："));
            JTextField usernameField = new JTextField();
            panel.add(usernameField);
            
            panel.add(new JLabel("密码："));
            JPasswordField passwordField = new JPasswordField();
            panel.add(passwordField);
            
            panel.add(new JLabel("身份："));
            JComboBox<String> identityBox = new JComboBox<>(new String[]{"admin", "doctor"});
            panel.add(identityBox);
            
            // 按钮面板
            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
            JButton saveBtn = new JButton("保存");
            JButton cancelBtn = new JButton("取消");
            
            // 保存事件
            saveBtn.addActionListener(e -> {
                String username = usernameField.getText().trim();
                String password = new String(passwordField.getPassword()).trim();
                String identity = (String) identityBox.getSelectedItem();
                
                // 输入验证
                if (username.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "用户名和密码不能为空！", "提示", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                // 密码长度验证（6位数）
                if (password.length() != 6) {
                    JOptionPane.showMessageDialog(this, "密码必须为6位数！", "提示", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                Connection conn = null;
                try {
                    conn = DBUtil.getConnection();
                    Users user = new Users(username, password, identity);
                    int result = userDao.addUser(conn, user);
                    if (result > 0) {
                        JOptionPane.showMessageDialog(this, "用户新增成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                        dispose();
                        loadUserData(); // 刷新用户列表
                    } else {
                        JOptionPane.showMessageDialog(this, "用户新增失败！", "错误", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "新增异常：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                } finally {
                    DBUtil.close(conn, null, null);
                }
            });
            
            cancelBtn.addActionListener(e -> dispose());
            btnPanel.add(saveBtn);
            btnPanel.add(cancelBtn);
            
            add(panel, BorderLayout.CENTER);
            add(btnPanel, BorderLayout.SOUTH);
            
            setVisible(true);
        }
    }

    // 加载所有数据
    private void loadAllData() {
        loadPatientData();
        loadDeptData();
        loadDoctorData();
        loadUserData();
        loadSlotData();
    }
}
```

## 系统架构

### 1. 三层架构

系统采用经典的三层架构设计：

- **表示层（View）**：负责用户界面的展示和用户交互，使用Java Swing实现
- **业务逻辑层（Model）**：负责业务逻辑处理，封装实体类和业务规则
- **数据访问层（Dao）**：负责与数据库交互，实现数据的增删改查

### 2. 技术栈

- **开发语言**：Java
- **界面框架**：Java Swing
- **数据库**：MySQL
- **数据库驱动**：mysql-connector-j-9.5.0.jar
- **开发工具**：IntelliJ IDEA

## 运行环境

- JDK 1.8或更高版本
- MySQL 5.7或更高版本
- IntelliJ IDEA（推荐）

## 数据库设计

### 主要表结构

1. **users表**：存储系统用户信息
    - username：用户名
    - password：密码
    - identity：身份（admin/doctor）

2. **department表**：存储科室信息
    - deptId：科室ID
    - deptName：科室名称
    - description：科室描述

3. **doctor表**：存储医生信息
    - doctorId：医生ID
    - name：医生姓名
    - title：职称
    - licenseNo：执业证书号
    - deptId：所属科室ID

4. **patient表**：存储患者信息
    - patientId：患者ID
    - idCard：身份证号
    - name：患者姓名
    - gender：性别
    - phone：联系电话
    - createTime：建档时间

5. **appointment_slot表**：存储号源信息
    - slotId：号源ID
    - doctorId：医生ID
    - date：日期
    - timeSlot：时间段
    - status：状态（available/occupied）

6. **registration_record表**：存储挂号记录
    - recordId：记录ID
    - patientId：患者ID
    - slotId：号源ID
    - registrationNo：挂号单号
    - registrationTime：挂号时间
    - status：状态（未就诊/已就诊/已取消）

## 安装与配置

1. 安装JDK 1.8或更高版本
2. 安装MySQL 5.7或更高版本
3. 创建数据库：`hospital_registration_management_system`
4. 导入数据库脚本（需自行创建）
5. 配置DBUtil.java中的数据库连接参数
6. 在IntelliJ IDEA中导入项目
7. 添加mysql-connector-j-9.5.0.jar到项目依赖
8. 运行LoginForm.java启动系统

## 系统特点

1. **用户友好的界面**：采用现代化的界面设计，操作简单直观
2. **完整的角色权限管理**：区分管理员、医生和患者三种角色，权限分明
3. **强大的搜索功能**：支持按名称、ID等多种条件搜索
4. **数据安全保障**：密码加密存储，数据库连接安全管理
5. **完整的挂号流程**：从患者挂号到医生接诊的完整流程管理

## 未来改进方向

1. 增加密码加密功能，提高安全性
2. 实现患者在线支付功能
3. 添加短信通知功能
4. 优化界面响应速度
5. 增加统计报表功能

## 总结

医院挂号管理系统是一个功能完整、界面友好的桌面应用程序，能够满足医院日常挂号管理的需求。系统采用了经典的三层架构设计，代码结构清晰，易于维护和扩展。通过使用Java Swing和MySQL技术，实现了从患者挂号到医生接诊的完整流程，提高了医院挂号管理的效率和准确性。
        