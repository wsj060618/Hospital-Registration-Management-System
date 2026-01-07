package View;

import Dao.UserDao;
import Model.Patient;
import Util.DBUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 患者挂号系统登录界面
 */
public class PatientLoginForm extends JFrame {
    // 界面核心组件
    private JTextField idCardField;     // 身份证号输入框
    private JTextField nameField;       // 姓名输入框
    private JComboBox<String> genderComboBox; // 性别选择下拉框
    private JTextField phoneField;      // 手机号输入框
    private JButton loginBtn;            // 登录按钮
    private JButton resetBtn;            // 重置按钮

    // 构造方法：初始化界面
    public PatientLoginForm() {
        initComponents();        // 初始化组件与布局
        bindBasicEvents();       // 绑定基础交互事件
        this.setVisible(true);   // 显示窗口
    }

    /**
     * 初始化界面组件：布局、样式、组件添加
     */
    private void initComponents() {
        // 窗口基础配置
        setTitle("患者登录 - 医院挂号管理系统");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 450);
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
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 20)); // 改为5行2列
        formPanel.setBackground(Color.WHITE);

        // 身份证号标签与输入框
        JLabel idCardLabel = new JLabel("身份证号");
        idCardLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        idCardField = new JTextField();
        idCardField.setToolTipText("请输入您的18位身份证号");
        initInputFieldStyle(idCardField); // 统一输入框样式

        // 姓名标签与输入框
        JLabel nameLabel = new JLabel("姓名");
        nameLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        nameField = new JTextField();
        nameField.setToolTipText("请输入您的姓名");
        initInputFieldStyle(nameField); // 统一输入框样式

        // 性别标签与下拉框
        JLabel genderLabel = new JLabel("性别");
        genderLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        genderComboBox = new JComboBox<>(new String[]{"未知", "男", "女"});
        genderComboBox.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        genderComboBox.setPreferredSize(new Dimension(220, 40));
        genderComboBox.setBackground(Color.WHITE);
        genderComboBox.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));

        // 手机号标签与输入框
        JLabel phoneLabel = new JLabel("手机号");
        phoneLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        phoneField = new JTextField();
        phoneField.setToolTipText("请输入您的手机号");
        initInputFieldStyle(phoneField); // 统一输入框样式

        // 将组件添加到表单面板
        formPanel.add(idCardLabel);
        formPanel.add(idCardField);
        formPanel.add(nameLabel);
        formPanel.add(nameField);
        formPanel.add(genderLabel);
        formPanel.add(genderComboBox);
        formPanel.add(phoneLabel);
        formPanel.add(phoneField);
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
        // 重置按钮：清空输入框，焦点回到身份证号
        resetBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                idCardField.setText("");
                nameField.setText("");
                genderComboBox.setSelectedIndex(0); // 重置为未知
                phoneField.setText("");
                idCardField.requestFocus(); // 焦点跳转
            }
        });

        // 登录按钮
        loginBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String idCard = idCardField.getText().trim();
                String name = nameField.getText().trim();
                int gender = genderComboBox.getSelectedIndex(); // 0-未知，1-男，2-女
                String phone = phoneField.getText().trim();

                // 简单输入验证
                if (idCard.isEmpty() || name.isEmpty() || phone.isEmpty()) {
                    JOptionPane.showMessageDialog(PatientLoginForm.this,
                            "请输入身份证号、姓名和手机号！", "提示", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                // 数据库连接和登录验证
                Connection conn = null;
                int rs = -1;

                try {
                    // 获取数据库连接
                    conn = DBUtil.getConnection();
                    if (conn == null) {
                        JOptionPane.showMessageDialog(PatientLoginForm.this,
                                "数据库连接失败，请检查数据库配置！", "错误：", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // 进行登录操作
                    UserDao userDao = new UserDao();
                    Long patientId = userDao.loginPatient(conn, idCard, name, gender, phone);
                    
                    if (patientId > 0) {
                        // 登录成功
                        JOptionPane.showMessageDialog(PatientLoginForm.this,
                                "登录成功！欢迎患者：" + name, "登录成功", JOptionPane.INFORMATION_MESSAGE);
                    
                        // 启动挂号界面
                        Patient patient = new Patient();
                        patient.setPatientId(patientId); // 设置患者ID
                        patient.setIdCard(idCard);
                        patient.setName(name);
                        patient.setGender(gender);
                        patient.setPhone(phone);
                    
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                // 启动挂号界面
                                new PatientRegistrationFrm(patient);
                            }
                        });
                        // 关闭当前登录窗口
                        dispose();
                    } else {
                        // 登录失败
                        JOptionPane.showMessageDialog(PatientLoginForm.this,
                                "登录失败，请检查输入的信息！", "登录失败", JOptionPane.ERROR_MESSAGE);
                    }
                } finally {
                    // 关闭数据库连接资源
                    try {
                        if (conn != null) conn.close();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        // 身份证号输入框按回车键：焦点跳转到姓名
        idCardField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                nameField.requestFocus();
            }
        });

        // 姓名输入框按回车键：焦点跳转到性别
        nameField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                genderComboBox.requestFocus();
            }
        });

        // 性别下拉框按回车键：焦点跳转到手机号
        genderComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == genderComboBox && e.getActionCommand().equals("comboBoxChanged")) {
                    phoneField.requestFocus();
                }
            }
        });

        // 手机号输入框按回车键：触发登录按钮事件
        phoneField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loginBtn.doClick(); // 模拟点击登录按钮
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
                new PatientLoginForm();
            }
        });
    }
}