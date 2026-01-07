package View;

import Model.Department;
import Model.Doctor;
import Model.AppointmentSlot;
import Model.Patient;
import Dao.DepartmentDao;
import Dao.DoctorDao;
import Dao.AppointmentSlotDao;
import Dao.RegistrationRecordDao;
import Util.DBUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 患者挂号主界面
 * 流程：选择科室 → 加载对应医生 → 加载医生号源 → 确认挂号
 */
public class PatientRegistrationFrm extends JFrame {
    // 核心组件
    private JComboBox<Department> deptComboBox; // 科室下拉框
    private JComboBox<Doctor> doctorComboBox; // 医生下拉框
    private JTable slotTable; // 号源表格
    private DefaultListModel<AppointmentSlot> slotListModel; // 号源列表模型
    private JList<AppointmentSlot> slotList; // 号源列表
    private Patient currentPatient; // 当前登录患者
    private JLabel tipLabel; // 提示标签

    // DAO 层对象
    private DepartmentDao deptDao = new DepartmentDao();
    private DoctorDao doctorDao = new DoctorDao();
    private AppointmentSlotDao slotDao = new AppointmentSlotDao();
    private RegistrationRecordDao registrationDao = new RegistrationRecordDao();

    public PatientRegistrationFrm(Patient patient) {
        this.currentPatient = patient;
        initFrame();
        initComponents();
        loadDepartments(); // 初始化加载所有科室
        setVisible(true);
    }

    // 初始化窗口属性
    private void initFrame() {
        setTitle("患者挂号 - 医院挂号管理系统");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null); // 居中显示
        setResizable(false);
        getContentPane().setBackground(Color.WHITE);
    }

    // 初始化界面组件
    private void initComponents() {
        // 设置窗口图标
        setIconImage(getDefaultIcon());

        // 主面板（边界布局）
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);
        add(mainPanel);

        // 顶部标题
        JLabel titleLabel = new JLabel("患者挂号", JLabel.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        titleLabel.setForeground(Color.RED); // 改为红色
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // 中间选择面板（左右布局：左侧选择区，右侧号源区）
        JPanel selectPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        selectPanel.setBackground(Color.WHITE);
        mainPanel.add(selectPanel, BorderLayout.CENTER);

        // 左侧选择面板（科室+医生选择）
        JPanel leftPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        selectPanel.add(leftPanel);

        // 科室选择区域
        JPanel deptPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        deptPanel.setBackground(Color.WHITE);
        JLabel deptLabel = new JLabel("选择科室：");
        deptLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        deptComboBox = new JComboBox<>();
        deptComboBox.setPreferredSize(new Dimension(250, 30));
        deptComboBox.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        // 科室选择事件：加载对应医生
        deptComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadDoctorsByDept();
            }
        });
        deptPanel.add(deptLabel);
        deptPanel.add(deptComboBox);
        leftPanel.add(deptPanel);

        // 医生选择区域
        JPanel doctorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        doctorPanel.setBackground(Color.WHITE);
        JLabel doctorLabel = new JLabel("选择医生：");
        doctorLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        doctorComboBox = new JComboBox<>();
        doctorComboBox.setPreferredSize(new Dimension(250, 30));
        doctorComboBox.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        // 医生选择事件：加载对应号源
        doctorComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadSlotsByDoctor();
            }
        });
        doctorPanel.add(doctorLabel);
        doctorPanel.add(doctorComboBox);
        leftPanel.add(doctorPanel);

        // 提示区域
        tipLabel = new JLabel("请先选择科室和医生查看号源", JLabel.CENTER);
        tipLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        tipLabel.setForeground(Color.GRAY);
        leftPanel.add(tipLabel);

        // 右侧号源展示区域
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        selectPanel.add(rightPanel);

        // 号源标题
        JLabel slotTitleLabel = new JLabel("可预约号源", JLabel.LEFT);
        slotTitleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        rightPanel.add(slotTitleLabel, BorderLayout.NORTH);

        // 号源列表（带滚动条）
        slotListModel = new DefaultListModel<>();
        slotList = new JList<>(slotListModel);
        slotList.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        slotList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        slotList.setFixedCellHeight(40);
        // 自定义号源列表渲染（显示日期、时段、剩余号源）
        slotList.setCellRenderer(new ListCellRenderer<AppointmentSlot>() {
            @Override
            public Component getListCellRendererComponent(JList<? extends AppointmentSlot> list, AppointmentSlot slot, int index, boolean isSelected, boolean cellHasFocus) {
                JPanel panel = new JPanel(new GridLayout(2, 1));
                panel.setBorder(new EmptyBorder(5, 5, 5, 5));
                if (isSelected) {
                    panel.setBackground(Color.RED); // 改为红色
                } else {
                    panel.setBackground(Color.WHITE);
                }

                // 日期+时段
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String dateStr = sdf.format(slot.getVisitDate());
                String timeSlot = dateStr + " " + slot.getTimeSlot();
                JLabel timeLabel = new JLabel(timeSlot);
                timeLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
                timeLabel.setForeground(isSelected ? Color.WHITE : Color.BLACK);

                // 剩余号源
                String tip = "剩余号源：" + slot.getRemainingNum() + "/" + slot.getTotalNum();
                JLabel numLabel = new JLabel(tip);
                numLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
                numLabel.setForeground(isSelected ? Color.WHITE : Color.GRAY);

                panel.add(timeLabel);
                panel.add(numLabel);
                return panel;
            }
        });
        JScrollPane slotScrollPane = new JScrollPane(slotList);
        rightPanel.add(slotScrollPane, BorderLayout.CENTER);

        // 底部按钮面板
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        btnPanel.setBackground(Color.WHITE);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);

        // 确认挂号按钮
        JButton registerBtn = new JButton("确认挂号");
        registerBtn.setFont(new Font("微软雅黑", Font.BOLD, 14));
        registerBtn.setBackground(Color.RED); // 改为红色
        registerBtn.setForeground(Color.WHITE);
        registerBtn.setPreferredSize(new Dimension(120, 40));
        registerBtn.setFocusPainted(false);
        registerBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                confirmRegistration();
            }
        });

        // 取消按钮
        JButton cancelBtn = new JButton("取消");
        cancelBtn.setFont(new Font("微软雅黑", Font.BOLD, 14));
        cancelBtn.setBackground(Color.WHITE);
        cancelBtn.setPreferredSize(new Dimension(120, 40));
        cancelBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // 关闭窗口
            }
        });

        btnPanel.add(registerBtn);
        btnPanel.add(cancelBtn);
    }

    /**
     * 加载所有科室到下拉框
     */
    private void loadDepartments() {
        Connection conn = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            rs = deptDao.listAll(conn);
            deptComboBox.removeAllItems();
            while (rs.next()) {
                Department dept = new Department();
                dept.setDeptId(rs.getLong("deptId"));
                dept.setDeptName(rs.getString("deptName"));
                dept.setDescription(rs.getString("description"));
                deptComboBox.addItem(dept);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "加载科室失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        } finally {
            closeConnection(conn, null, rs);
        }
    }

    /**
     * 根据选择的科室加载对应医生
     */
    private void loadDoctorsByDept() {
        Department selectedDept = (Department) deptComboBox.getSelectedItem();
        if (selectedDept == null) return;

        Connection conn = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            rs = doctorDao.listByDeptId(conn, selectedDept.getDeptId());
            doctorComboBox.removeAllItems();
            while (rs.next()) {
                Doctor doctor = new Doctor();
                doctor.setDoctorId(rs.getLong("doctorId"));
                doctor.setName(rs.getString("name"));
                doctor.setTitle(rs.getString("title"));
                doctor.setLicenseNo(rs.getString("licenseNo"));
                doctor.setDeptId(rs.getLong("deptId"));
                // 下拉框显示：医生姓名 + 职称
                doctorComboBox.addItem(doctor);
            }
            // 清空号源列表
            slotListModel.clear();
            tipLabel.setText("请选择医生查看可用号源");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "加载医生失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        } finally {
            closeConnection(conn, null, rs);
        }
    }

    /**
     * 根据选择的医生加载可用号源（仅显示剩余号源>0的）
     */
//    private void loadSlotsByDoctor() {
//        Doctor selectedDoctor = (Doctor) doctorComboBox.getSelectedItem();
//        if (selectedDoctor == null) return;
//
//        Connection conn = null;
//        ResultSet rs = null;
//        try {
//            conn = DBUtil.getConnection();
//            rs = slotDao.listByDoctorId(conn, selectedDoctor.getDoctorId());
//            slotListModel.clear();
//            while (rs.next()) {
//                int remainingNum = rs.getInt("remainingNum");
//                if (remainingNum <= 0) continue; // 过滤已约满的号源
//
//                AppointmentSlot slot = new AppointmentSlot();
//                slot.setSlotId(rs.getLong("slotId"));
//                slot.setVisitDate(rs.getDate("visitDate"));
//                slot.setTimeSlot(rs.getString("timeSlot"));
//                slot.setTotalNum(rs.getInt("totalNum"));
//                slot.setRemainingNum(remainingNum);
//                slot.setDoctorId(rs.getLong("doctorId"));
//                slotListModel.addElement(slot);
//            }
//
//            if (slotListModel.size() == 0) {
//                tipLabel.setText("该医生暂无可用号源，请选择其他医生");
//            } else {
//                tipLabel.setText("请选择号源（剩余号源实时更新）");
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//            JOptionPane.showMessageDialog(this, "加载号源失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
//        } finally {
//            closeConnection(conn, null, rs);
//        }
//    }
    // 仅修改 loadSlotsByDoctor 方法，其他代码保持不变
    private void loadSlotsByDoctor() {
        Doctor selectedDoctor = (Doctor) doctorComboBox.getSelectedItem();
        if (selectedDoctor == null) return;

        // 1. 生成当前完整时间字符串（格式：yyyy-MM-dd HH:mm:ss）
        // 推荐使用 Java 8+ 新版时间 API，线程安全
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String currentDateTimeStr = LocalDateTime.now().format(formatter);

        Connection conn = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            // 2. 调用改造后的 Dao 方法，传入当前完整时间字符串
            rs = slotDao.listByDoctorId(conn, selectedDoctor.getDoctorId(), currentDateTimeStr);
            slotListModel.clear();
            while (rs.next()) {
                int remainingNum = rs.getInt("remainingNum");
                if (remainingNum <= 0) continue;

                AppointmentSlot slot = new AppointmentSlot();
                slot.setSlotId(rs.getLong("slotId"));
                slot.setVisitDate(rs.getDate("visitDate"));
                slot.setTimeSlot(rs.getString("timeSlot"));
                slot.setTotalNum(rs.getInt("totalNum"));
                slot.setRemainingNum(remainingNum);
                slot.setDoctorId(rs.getLong("doctorId"));
                slotListModel.addElement(slot);
            }

            if (slotListModel.size() == 0) {
                tipLabel.setText("该医生暂无可用号源（仅显示未来时间号源），请选择其他医生");
            } else {
                tipLabel.setText("请选择号源（剩余号源实时更新）");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "加载号源失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        } finally {
            closeConnection(conn, null, rs);
        }
    }
    /**
     * 确认挂号逻辑
     */
    private void confirmRegistration() {
        // 校验选择项
        Department selectedDept = (Department) deptComboBox.getSelectedItem();
        Doctor selectedDoctor = (Doctor) doctorComboBox.getSelectedItem();
        AppointmentSlot selectedSlot = slotList.getSelectedValue();

        if (selectedDept == null || selectedDoctor == null || selectedSlot == null) {
            JOptionPane.showMessageDialog(this, "请完整选择科室、医生和号源！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 校验号源是否仍可用（防止并发操作导致号源已被约满）
        if (selectedSlot.getRemainingNum() <= 0) {
            JOptionPane.showMessageDialog(this, "该号源已约满，请重新选择！", "提示", JOptionPane.ERROR_MESSAGE);
            loadSlotsByDoctor(); // 刷新号源列表
            return;
        }

        // 确认挂号
        int confirm = JOptionPane.showConfirmDialog(this,
                "确认挂号以下号源？\n" +
                        "科室：" + selectedDept.getDeptName() + "\n" +
                        "医生：" + selectedDoctor.getName() + "（" + selectedDoctor.getTitle() + "）\n" +
                        "时间：" + new SimpleDateFormat("yyyy-MM-dd").format(selectedSlot.getVisitDate()) + " " + selectedSlot.getTimeSlot() + "\n" +
                        "患者：" + currentPatient.getName() + "（身份证号：" + currentPatient.getIdCard() + "）",
                "确认挂号", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            Connection conn = null;
            try {
                conn = DBUtil.getConnection();
                conn.setAutoCommit(false); // 开启事务

                // 1. 创建挂号记录
                String registrationNo = generateRegistrationNo(); // 生成唯一挂号单号
                Date registrationTime = new Date();
                String status = "未就诊"; // 初始状态：未就诊

                // 2. 执行挂号（插入挂号记录 + 扣减号源余量）
                boolean registerSuccess = registrationDao.addRegistration(
                        conn,
                        currentPatient.getPatientId(),
                        selectedSlot.getSlotId(),
                        registrationNo,
                        registrationTime,
                        status
                );

                if (registerSuccess) {
                    // 3. 提交事务
                    conn.commit();
                    JOptionPane.showMessageDialog(this,
                            "挂号成功！\n" +
                                    "挂号单号：" + registrationNo + "\n" +
                                    "请凭此单号按时就诊",
                            "挂号成功", JOptionPane.INFORMATION_MESSAGE);
                    dispose(); // 关闭挂号窗口
                } else {
                    conn.rollback();
                    JOptionPane.showMessageDialog(this, "挂号失败，该号源可能已被约满，请重试！", "错误", JOptionPane.ERROR_MESSAGE);
                    loadSlotsByDoctor(); // 刷新号源列表
                }

            } catch (SQLIntegrityConstraintViolationException e) {
                // 处理重复预约的唯一约束异常
                try {
                    if (conn != null) conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "您已经预约了该号源，不能重复预约！", "提示", JOptionPane.WARNING_MESSAGE);
            } catch (SQLException e) {
                try {
                    if (conn != null) conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "挂号异常：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            } finally {
                closeConnection(conn, null, null);
            }
        }
    }

    /**
     * 生成唯一挂号单号（规则：患者ID + 时间戳）
     */
    private String generateRegistrationNo() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String timeStr = sdf.format(new Date());
        return "REG" + currentPatient.getPatientId() + timeStr;
    }

    /**
     * 关闭数据库连接资源
     */
    private void closeConnection(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
    // 测试入口
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 模拟已登录患者（实际场景从登录界面传递）
        Patient testPatient = new Patient();
        testPatient.setPatientId(1L);
        testPatient.setName("赵小明");
        testPatient.setGender(1); // 1-男
        testPatient.setIdCard("110101200001011234");
        testPatient.setPhone("13900139001");

        // 启动挂号界面
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new PatientRegistrationFrm(testPatient);
            }
        });
    }
}