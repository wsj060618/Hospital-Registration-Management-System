package View;

import Dao.UserDao;
import Dao.DoctorDao;
import Model.Users;
import Model.Doctor;
import Util.DBUtil;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import javax.imageio.ImageIO;

/**
 * 医院挂号管理系统登录界面
 */
public class LoginForm extends JFrame {
    // 界面核心组件
    private JTextField usernameField;    // 用户名输入框
    private JPasswordField passwordField;// 密码输入框（隐藏输入）
    private JTextField doctorIdField;    // 医生ID输入框（医生专用）
    private JButton loginBtn;            // 登录按钮
    private JButton resetBtn;            // 重置按钮
    private JRadioButton adminRadioBtn;  // 管理员单选按钮
    private JRadioButton doctorRadioBtn; // 医生单选按钮
    private ButtonGroup userTypeGroup;   // 按钮组，确保只能选择一个
    private JLabel doctorIdLabel;        // 医生ID标签

    // 构造方法：初始化界面
    public LoginForm() {
        initComponents();        // 初始化组件与布局
        bindBasicEvents();       // 绑定基础交互事件
        this.setVisible(true);   // 显示窗口
    }

    /**
     * 初始化界面组件：布局、样式、组件添加
     */
    private void initComponents() {
        // 窗口基础配置
        setTitle("登录 - 医院挂号管理系统");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 400);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // 设置窗口图标
        setIconImage(getDefaultIcon());

        // 主面板
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        mainPanel.setBackground(Color.WHITE);

        // 北部：标题区域
        JLabel titleLabel = new JLabel("医院挂号管理系统", JLabel.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24)); // 字体：加粗24号
        titleLabel.setForeground(new Color(220, 53, 69));      // 字体颜色：红色
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // 中部：登录表单
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 15)); // 改为5行2列
        formPanel.setBackground(Color.WHITE);

        // 用户名标签与输入框
        JLabel userLabel = new JLabel("用户名");
        userLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        usernameField = new JTextField();
        initInputFieldStyle(usernameField); // 统一输入框样式

        // 密码标签与输入框
        JLabel pwdLabel = new JLabel("密码");
        pwdLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        passwordField = new JPasswordField();
        initInputFieldStyle(passwordField); // 统一输入框样式

        // 医生ID标签与输入框
        doctorIdLabel = new JLabel("医生ID（医生必填）");
        doctorIdLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        doctorIdField = new JTextField();
        initInputFieldStyle(doctorIdField);
        // 默认显示医生ID输入框（仅医生需要填写）
        doctorIdLabel.setVisible(true);
        doctorIdField.setVisible(true);

        // 用户类型选择
        JLabel userTypeLabel = new JLabel("用户类型");
        userTypeLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        
        // 创建单选按钮和按钮组
        userTypeGroup = new ButtonGroup();
        doctorRadioBtn = new JRadioButton("医生");
        adminRadioBtn = new JRadioButton("管理员");
        adminRadioBtn.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        doctorRadioBtn.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        adminRadioBtn.setBackground(Color.WHITE); // 单选按钮背景色设置为白色
        doctorRadioBtn.setBackground(Color.WHITE); // 单选按钮背景色设置为白色
        adminRadioBtn.setFocusPainted(false); // 取消管理员单选按钮聚焦框
        doctorRadioBtn.setFocusPainted(false); // 取消医生单选按钮聚焦框
        doctorRadioBtn.setSelected(true); // 默认选择医生
        
        // 将单选按钮添加到按钮组
        userTypeGroup.add(doctorRadioBtn);
        userTypeGroup.add(adminRadioBtn);
        
        // 创建单选按钮面板
        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        radioPanel.setBackground(Color.WHITE);
        radioPanel.add(doctorRadioBtn);
        radioPanel.add(adminRadioBtn);

        // 将组件添加到表单面板
        formPanel.add(userLabel);
        formPanel.add(usernameField);
        formPanel.add(pwdLabel);
        formPanel.add(passwordField);
        formPanel.add(doctorIdLabel);
        formPanel.add(doctorIdField);
        formPanel.add(userTypeLabel);
        formPanel.add(radioPanel);
        formPanel.add(new JLabel(""));
        formPanel.add(new JLabel(""));
        mainPanel.add(formPanel, BorderLayout.CENTER);

        // 南部：按钮区域（居中排列）
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
        buttonPanel.setBackground(Color.WHITE);

        // 登录按钮
        loginBtn = new JButton("登录");
        initButtonStyle(loginBtn, true); // 主按钮样式

        // 重置按钮
        resetBtn = new JButton("重置");
        initButtonStyle(resetBtn, false); // 次要按钮样式

        buttonPanel.add(loginBtn);
        buttonPanel.add(resetBtn);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // 将主面板添加到窗口
        add(mainPanel);
    }

    /**
     * 初始化输入框样式：统一边框、字体、尺寸
     */
    private void initInputFieldStyle(JComponent field) {
        field.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        field.setPreferredSize(new Dimension(220, 40)); // 输入框宽×高
        field.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1)); // 输入框边框
        // 输入框聚焦时边框高亮
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                field.setBorder(BorderFactory.createLineBorder(new Color(220, 53, 69), 2)); // 红色边框
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                field.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
            }
        });
    }

    /**
     * 初始化按钮样式：统一字体、尺寸、悬停效果
     * @param button 按钮组件
     * @param isMain 是否为主按钮（登录按钮）
     */
    private void initButtonStyle(JButton button, boolean isMain) {
        button.setFont(new Font("微软雅黑", Font.BOLD, 16));
        button.setPreferredSize(new Dimension(120, 35));
        button.setFocusPainted(false); // 取消按钮焦点边框
        button.setCursor(new Cursor(Cursor.HAND_CURSOR)); // 悬停时显示手型光标

        if (isMain) {
            // 主按钮样式
            button.setBackground(Color.RED);
            button.setForeground(Color.WHITE);
        } else {
            // 次要按钮样式
            button.setBackground(Color.WHITE);
            button.setForeground(Color.BLACK);
        }
    }

    /**
     * 绑定基础交互事件：重置、回车键登录、焦点跳转
     */
    private void bindBasicEvents() {
        // 重置按钮：清空输入框，焦点回到用户名
        resetBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                usernameField.setText("");
                passwordField.setText("");
                doctorIdField.setText("");
                userTypeGroup.clearSelection(); // 清空选择
                doctorRadioBtn.setSelected(true); // 重置为默认选择医生
                usernameField.requestFocus(); // 焦点跳转
            }
        });

        // 用户类型选择切换事件
        doctorRadioBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 医生用户显示医生ID输入框
                doctorIdLabel.setVisible(true);
                doctorIdField.setVisible(true);
            }
        });

        adminRadioBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 管理员用户隐藏医生ID输入框
                doctorIdLabel.setVisible(false);
                doctorIdField.setVisible(false);
                doctorIdField.setText("");
            }
        });

        // 登录按钮：实现登录业务逻辑
        loginBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText().trim();
                char[] pwdChars = passwordField.getPassword();
                String password = new String(pwdChars);
                Arrays.fill(pwdChars, ' '); // 清空密码数组，提升安全性
                
                // 获取用户类型
                String userType = adminRadioBtn.isSelected() ? "admin" : "doctor";

                // 输入验证
                if (username.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(LoginForm.this,
                            "请输入用户名和密码！", "提示", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                // 医生用户需要验证医生ID
                Long doctorId = null;
                if ("doctor".equals(userType)) {
                    if (doctorIdField.getText().trim().isEmpty()) {
                        JOptionPane.showMessageDialog(LoginForm.this,
                                "医生用户请输入医生ID！", "提示", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }
                    try {
                        doctorId = Long.parseLong(doctorIdField.getText().trim());
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(LoginForm.this,
                                "医生ID格式错误，请输入有效的数字！", "提示", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }
                }

                // 数据库连接和登录验证
                Connection conn = null;
                ResultSet rs = null;

                try {
                    // 获取数据库连接
                    conn = DBUtil.getConnection();
                    if (conn == null) {
                        JOptionPane.showMessageDialog(LoginForm.this,
                                "数据库连接失败，请检查数据库配置！", "错误：", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // 进行登录操作
                    UserDao userDao = new UserDao();
                    rs = userDao.login(conn, username, password, userType);

                    if (rs.next()) {
                        // 登录成功
                        JOptionPane.showMessageDialog(LoginForm.this,
                                "登录成功！欢迎 " + userType + "：" + username, "登录成功", JOptionPane.INFORMATION_MESSAGE);
                        
                        // 根据用户类型跳转到相应界面
                        if ("admin".equals(userType)) {
                            // 管理员登录，创建Users对象并打开管理员界面
                            Users admin = new Users(username, password, "admin");
                            new AdminManagementFrm(admin);
                        } else if ("doctor".equals(userType)) {
                            // 医生登录，获取医生信息并打开医生界面
                            DoctorDao doctorDao = new DoctorDao();
                            ResultSet doctorRs = doctorDao.getById(conn, doctorId);
                            if (doctorRs.next()) {
                                Doctor doctor = new Doctor();
                                doctor.setDoctorId(doctorRs.getLong("doctorId"));
                                doctor.setName(doctorRs.getString("name"));
                                doctor.setTitle(doctorRs.getString("title"));
                                doctor.setLicenseNo(doctorRs.getString("licenseNo"));
                                doctor.setDeptId(doctorRs.getLong("deptId"));
                                
                                // 打开医生工作台
                                new DoctorWorkbenchFrm(doctor);
                            } else {
                                JOptionPane.showMessageDialog(LoginForm.this,
                                        "未找到该医生ID对应的医生信息！", "错误", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                        }
                        
                        // 关闭当前登录窗口
                        dispose();
                    } else {
                        // 登录失败
                        JOptionPane.showMessageDialog(LoginForm.this,
                                "用户名、密码或用户类型错误，请重新输入！", "登录失败", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(LoginForm.this,
                            "登录过程中发生错误：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                } finally {
                    // 关闭数据库连接资源
                    try {
                        if (rs != null) rs.close();
                        if (conn != null) conn.close();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        // 用户名输入框按回车键：焦点跳转到密码框
        usernameField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                passwordField.requestFocus();
            }
        });

        // 密码输入框按回车键：焦点跳转到医生ID框（如果是医生用户）
        passwordField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (doctorRadioBtn.isSelected()) {
                    doctorIdField.requestFocus();
                } else {
                    loginBtn.doClick();
                }
            }
        });

        // 医生ID输入框按回车键：触发登录
        doctorIdField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loginBtn.doClick();
            }
        });
    }

    /**
     * 获取默认窗口图标
     */
    private Image getDefaultIcon() {
        try {
            // 使用文件路径加载图标
            File iconFile = new File("resource/ico_ICRC.png");
            if (iconFile.exists()) {
                return ImageIO.read(iconFile);
            }
            // 图标不存在时返回系统默认图标
            System.out.println("图标文件不存在: " + iconFile.getAbsolutePath());
            return Toolkit.getDefaultToolkit().createImage(new byte[0]);
        } catch (IOException e) {
            // 加载失败时返回系统默认图标
            System.out.println("图标加载失败: " + e.getMessage());
            return Toolkit.getDefaultToolkit().createImage(new byte[0]);
        }
    }

    /**
     * 主方法：测试登录界面（直接运行即可查看效果）
     */
    public static void main(String[] args) {
        // 设置 Nimbus UI主题
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LoginForm();
            }
        });
    }
}