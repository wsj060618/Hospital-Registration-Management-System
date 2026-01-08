package View;

import Dao.DoctorDao;
import Dao.UserDao;
import Model.*;
import Dao.DepartmentDao;
import Dao.PatientDao;
import Dao.AppointmentSlotDao;
import Util.DBUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
* 管理员管理界面
 */
public class AdminManagementFrm extends JFrame {
    private Users currentAdmin; // 当前登录管理员
    private JTabbedPane tabbedPane; // 选项卡面板
    private JTable patientTable; // 患者表格
    private JTable deptTable; // 科室表格
    private JTable slotTable; // 号源表格
    private JTable userTable; // 用户表格
    private JTable doctorTable; // 医生表格
    private DefaultTableModel patientModel, deptModel, slotModel, userModel, doctorModel; // 表格模型

    // DAO层对象
    private DoctorDao doctorDao = new DoctorDao();
    private PatientDao patientDao = new PatientDao();
    private DepartmentDao deptDao = new DepartmentDao();
    private AppointmentSlotDao slotDao = new AppointmentSlotDao();
    private UserDao userDao = new UserDao();

    public AdminManagementFrm(Users currentAdmin) {
        this.currentAdmin = currentAdmin;
        initFrame();
        initComponents();
        loadAllData();
        setVisible(true);
    }

    // 初始化窗口
    private void initFrame() {
        setIconImage(getDefaultIcon());
        setTitle("管理员工作台 - " + currentAdmin.getUsername());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(Color.WHITE);
    }

    // 初始化组件
    private void initComponents() {
// 主面板
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);
        add(mainPanel);

        // 顶部标题
        JLabel titleLabel = new JLabel("管理员管理中心", JLabel.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 28));
        titleLabel.setForeground(new Color(0, 120, 215));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // 选项卡面板
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        // 1. 患者管理选项卡
        JPanel patientPanel = createPatientPanel();
        tabbedPane.addTab("患者管理", patientPanel);

        // 2. 科室管理选项卡
        JPanel deptPanel = createDeptPanel();
        tabbedPane.addTab("科室管理", deptPanel);

        // 3. 医生管理选项卡
        JPanel doctorPanel = createDoctorPanel();
        tabbedPane.addTab("医生管理", doctorPanel);

        // 4. 用户管理选项卡
        JPanel userPanel = createUserPanel();
        tabbedPane.addTab("用户管理", userPanel);
        
        // 5. 号源配置选项卡
        JPanel slotPanel = createSlotPanel();
        tabbedPane.addTab("号源配置", slotPanel);
    }

    /**
     * 创建患者管理面板
     */
    private JPanel createPatientPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(Color.WHITE);
    
        // 搜索面板
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        searchPanel.setBackground(Color.WHITE);
        JLabel nameLabel = new JLabel("患者姓名：");
        JTextField nameField = new JTextField(20);
        JButton searchBtn = new JButton("搜索");
        JButton refreshBtn = new JButton("刷新");
        JButton addBtn = new JButton("新增患者");
        JButton deleteBtn = new JButton("删除患者");
        
        // 新增患者按钮事件
        addBtn.addActionListener(e -> new PatientAddDialog(this));
        // 搜索按钮事件
        searchBtn.addActionListener(e -> loadPatientData(nameField.getText().trim()));
        // 刷新按钮事件
        refreshBtn.addActionListener(e -> {
            nameField.setText("");
            loadPatientData("");
        });
        // 删除患者按钮事件
        deleteBtn.addActionListener(e -> {
            int selectedRow = patientTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "请先选择要删除的患者！", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // 获取患者ID和姓名
            Long patientId = (Long) patientModel.getValueAt(selectedRow, 0);
            String patientName = (String) patientModel.getValueAt(selectedRow, 1);
            
            // 确认删除
            int confirm = JOptionPane.showConfirmDialog(this,
                    "确定要删除患者\"" + patientName + "\"吗？\n此操作不可恢复！",
                    "删除确认", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                Connection conn = null;
                try {
                    conn = DBUtil.getConnection();
                    int count = patientDao.deleteById(conn, patientId);
                    if (count > 0) {
                        JOptionPane.showMessageDialog(this, "患者删除成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                        loadPatientData(""); // 刷新患者列表
                        nameField.setText(""); // 清空搜索框
                    } else {
                        JOptionPane.showMessageDialog(this, "患者删除失败，未找到该患者！", "错误", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    String errorMessage = ex.getMessage();
                    if (errorMessage != null && !errorMessage.isEmpty()) {
                        System.out.println("SQL异常信息：" + errorMessage);
                        // 不区分大小写，使用更宽泛的匹配
                        String lowerCaseMessage = errorMessage.toLowerCase();
                        if (lowerCaseMessage.contains("foreign key constraint") || lowerCaseMessage.contains("cannot delete or update a parent row")) {
                            JOptionPane.showMessageDialog(this, "删除失败：该患者已挂号，无法直接删除！\n请先取消该患者的所有挂号记录后再尝试删除。", "错误", JOptionPane.ERROR_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(this, "删除失败：" + errorMessage, "错误", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, "删除失败：未知错误", "错误", JOptionPane.ERROR_MESSAGE);
                    }
                    ex.printStackTrace();
                } finally {
                    DBUtil.close(conn, null, null);
                }
            }
        });

        // 将搜索相关组件添加到搜索面板
        searchPanel.add(nameLabel);
        searchPanel.add(nameField);
        searchPanel.add(searchBtn);
        searchPanel.add(refreshBtn);
        panel.add(searchPanel, BorderLayout.NORTH);

        // 患者表格
        String[] patientColumns = {"患者ID", "姓名", "性别", "身份证号", "联系电话", "建档时间"};
        patientModel = new DefaultTableModel(null, patientColumns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        patientTable = new JTable(patientModel);
        patientTable.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        patientTable.setRowHeight(30);
        patientTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));
        JScrollPane scrollPane = new JScrollPane(patientTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // 创建底部操作按钮面板
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.add(addBtn); // 新增按钮在前
bottomPanel.add(deleteBtn); // 删除按钮在后
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * 创建科室管理面板
     */
    private JPanel createDeptPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(Color.WHITE);

        // 创建顶部搜索面板
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        searchPanel.setBackground(Color.WHITE);
        JLabel nameLabel = new JLabel("科室名称：");
        JTextField nameField = new JTextField(20);
        JButton searchBtn = new JButton("搜索");
        JButton refreshBtn = new JButton("刷新"); // 刷新按钮放在搜索后面

        searchBtn.addActionListener(e -> loadDeptData(nameField.getText().trim()));
        refreshBtn.addActionListener(e -> {
            nameField.setText("");
            loadDeptData("");
        });

        searchPanel.add(nameLabel);
        searchPanel.add(nameField);
        searchPanel.add(searchBtn);
        searchPanel.add(refreshBtn);
        panel.add(searchPanel, BorderLayout.NORTH);

        // 科室表格
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

        // 创建底部操作按钮面板
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        bottomPanel.setBackground(Color.WHITE);
        JButton addDeptBtn = new JButton("新增科室");
        JButton deleteDeptBtn = new JButton("删除科室");

        addDeptBtn.addActionListener(e -> new DepartmentAddDialog(this));

        // 删除科室按钮事件
        deleteDeptBtn.addActionListener(e -> {
            int selectedRow = deptTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "请先选择要删除的科室！", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 获取科室ID
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

        bottomPanel.add(addDeptBtn); // 新增按钮
        bottomPanel.add(deleteDeptBtn); // 删除按钮
        panel.add(bottomPanel, BorderLayout.SOUTH);

        loadDeptData(); // 初始加载数据
        return panel;
    }


    /**
     * 创建号源配置面板
     */
    private JPanel createSlotPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(Color.WHITE);

        // 配置面板
        JPanel configPanel = new JPanel();
        configPanel.setLayout(new BoxLayout(configPanel, BoxLayout.Y_AXIS));
        configPanel.setBackground(Color.WHITE);
        JPanel doctorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));

        // 筛选组件
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        filterPanel.setBackground(Color.WHITE);
        JLabel filterDateLabel = new JLabel("筛选日期：");
        JTextField filterDateField = new JTextField(15);
        JButton filterBtn = new JButton("按日期搜索");
        JButton clearFilterBtn = new JButton("刷新");
        JLabel doctorLabel = new JLabel("医生：");
        // 修改为JComboBox<Object[]>以同时存储医生ID和显示文本
        JComboBox<Object[]> doctorComboBox = new JComboBox<>();
        JLabel dateLabel = new JLabel("出诊日期：");
        JTextField dateField = new JTextField(15);
        JLabel timeLabel = new JLabel("时段：");
        JTextField timeField = new JTextField(15);
        JLabel numLabel = new JLabel("总号源：");
        JTextField numField = new JTextField(5);
        JButton saveBtn = new JButton("保存号源");
        JButton deleteBtn = new JButton("删除号源"); // 新增删除按钮

        // 添加筛选组件到配置面板
        filterPanel.add(filterDateLabel);
        filterPanel.add(filterDateField);
        filterPanel.add(filterBtn);
        filterPanel.add(clearFilterBtn);
        configPanel.add(filterPanel);

        // 添加保存号源相关到配置面板
        doctorPanel.add(doctorLabel);
        doctorPanel.add(doctorComboBox);
        doctorPanel.add(dateLabel);
        doctorPanel.add(dateField);
        doctorPanel.add(timeLabel);
        doctorPanel.add(timeField);
        doctorPanel.add(numLabel);
        doctorPanel.add(numField);
        doctorPanel.add(saveBtn);
        doctorPanel.add(deleteBtn); // 添加删除按钮到配置面板
        doctorPanel.setBackground(Color.WHITE);
        configPanel.add(doctorPanel);
        panel.add(configPanel, BorderLayout.NORTH);

        // 加载医生到下拉框
        doctorComboBox.removeAllItems(); // 清空现有选项
        Connection conn = null;
        ResultSet doctorRs = null;
        ResultSet deptRs = null;

        try {
            conn = DBUtil.getConnection();
            doctorRs = doctorDao.listAll(conn);

            while (doctorRs.next()) {
                String doctorName = doctorRs.getString("name");
                Long doctorId = doctorRs.getLong("doctorId"); // 获取医生ID
                Long deptId = doctorRs.getLong("deptId");

                // 获取科室名称
                String deptName = "未知科室";
                deptRs = deptDao.getById(conn, deptId);
                if (deptRs.next()) {
                    deptName = deptRs.getString("deptName");
                }

                String displayText = doctorName + "（" + deptName + "）";
                doctorComboBox.addItem(new Object[]{doctorId, displayText}); // 存储医生ID和显示文本

                // 关闭科室结果集
                if (deptRs != null) {
                    deptRs.close();
                    deptRs = null;
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "加载医生列表失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } finally {
            DBUtil.close(conn, null, doctorRs);
            if (deptRs != null) {
                try {
                    deptRs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        // 设置下拉框渲染器，使其只显示文本部分
        doctorComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value != null) {
                    Object[] item = (Object[]) value;
                    setText((String) item[1]);
                }
                return this;
            }
        });

        // 保存号源按钮事件监听器
        saveBtn.addActionListener(e -> {
            try {
                // 获取医生ID
                int selectedDoctorIndex = doctorComboBox.getSelectedIndex();
                if (selectedDoctorIndex == -1) {
                    JOptionPane.showMessageDialog(this, "请选择医生！", "提示", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                Object[] selectedDoctor = (Object[]) doctorComboBox.getSelectedItem();
                Long doctorId = (Long) selectedDoctor[0];

                // 获取出诊日期
                String dateStr = dateField.getText().trim(); // 直接 trim() 避免后续处理
                if (dateStr.isEmpty()) { // JTextField.getText() 不会返回 null，直接检查是否为空
                    JOptionPane.showMessageDialog(this, "请输入出诊日期！", "提示", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                // 独立的日期解析try-catch块
                Date visitDate = null;
            try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    dateFormat.setLenient(false); // 严格验证日期格式
                    visitDate = dateFormat.parse(dateStr);
                } catch (ParseException ex) {
                    JOptionPane.showMessageDialog(this, "日期格式错误！请使用yyyy-MM-dd格式，例如：2025-01-02", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // 验证日期范围：不能是过去的日期
                // 使用Calendar类替代已弃用的Date方法
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                Date today = cal.getTime();

                // 添加额外的null检查，确保visitDate不为null
                if (visitDate != null && visitDate.before(today)) {
                    JOptionPane.showMessageDialog(this, "出诊日期不能是过去的日期！", "提示", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // 获取时段
                String timeSlot = timeField.getText();
                if (timeSlot == null || timeSlot.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "请输入时段！", "提示", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // 验证时段格式：HH:mm-HH:mm
                String timeSlotPattern = "^([01]?[0-9]|2[0-3]):[0-5][0-9]-([01]?[0-9]|2[0-3]):[0-5][0-9]$";
                if (!timeSlot.matches(timeSlotPattern)) {
                    JOptionPane.showMessageDialog(this, "时段格式错误！请使用HH:mm-HH:mm格式，例如：09:00-10:00", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // 验证时段范围和合理性
                String[] timeParts = timeSlot.split("-");
                String startTimeStr = timeParts[0];
                String endTimeStr = timeParts[1];

                // 独立的时间解析try-catch块
                Date startTime = null;
                Date endTime = null;
                try {
                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                    timeFormat.setLenient(false);
                    startTime = timeFormat.parse(startTimeStr);
                    endTime = timeFormat.parse(endTimeStr);
                } catch (ParseException ex) {
                    JOptionPane.showMessageDialog(this, "时间格式错误！请使用HH:mm格式，例如：09:00", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // 验证结束时间必须大于开始时间
                if (!endTime.after(startTime)) {
                    JOptionPane.showMessageDialog(this, "时段结束时间必须大于开始时间！", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // 获取总号源
                String numStr = numField.getText();
                if (numStr == null || numStr.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "请输入总号源！", "提示", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                int totalNum = Integer.parseInt(numStr);

                // 验证总号源范围：1-100
                if (totalNum <= 0 || totalNum > 100) {
                    JOptionPane.showMessageDialog(this, "总号源必须在1-100之间！", "提示", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // 调用保存号源方法
                saveAppointmentSlot(doctorId, visitDate, timeSlot, totalNum);

                // 清空输入框
                dateField.setText("");
                timeField.setText("");
                numField.setText("");

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "总号源必须是数字！", "错误", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "保存号源失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

        // 筛选按钮事件
        filterBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String filterDateStr = filterDateField.getText();
                if (filterDateStr == null || filterDateStr.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(AdminManagementFrm.this, "请输入筛选日期！", "提示", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // 验证日期格式
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                sdf.setLenient(false);
                try {
                    sdf.parse(filterDateStr);
                } catch (ParseException ex) {
                    JOptionPane.showMessageDialog(AdminManagementFrm.this, "日期格式错误，请使用yyyy-MM-dd格式！", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // 按日期筛选号源
                Connection conn = null;
                try {
                    conn = DBUtil.getConnection();
                    ResultSet rs = slotDao.listByDate(conn, filterDateStr);
                    slotModel.setRowCount(0);
                    while (rs.next()) {
                        // 根据doctorId获取医生姓名
                        String doctorName = getDoctorNameById(conn, rs.getLong("doctorId"));
                        Object[] row = {
                                rs.getLong("slotId"),
                                doctorName,
                                new SimpleDateFormat("yyyy-MM-dd").format(rs.getDate("visitDate")),
                                rs.getString("timeSlot"),
                                rs.getInt("totalNum"),
                                rs.getInt("remainingNum")
                        };
                        slotModel.addRow(row);
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(AdminManagementFrm.this, "筛选号源失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                } finally {
                    DBUtil.close(conn, null, null);
                }
            }
        });

        // 清除筛选按钮事件
        clearFilterBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                filterDateField.setText("");
                loadSlotData(); // 重新加载所有号源
            }
        });

        // 删除号源按钮事件
        deleteBtn.addActionListener(e -> {
            int selectedRow = slotTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "请先选择要删除的号源！", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 获取号源ID
            Long slotId = (Long) slotModel.getValueAt(selectedRow, 0);

            // 确认删除
            int confirm = JOptionPane.showConfirmDialog(this,
                    "确定要删除选中的号源吗？\n此操作不可恢复！",
                    "删除确认", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                Connection deleteConn = null;
                try {
                    deleteConn = DBUtil.getConnection();
                    int count = slotDao.deleteById(deleteConn, slotId);
                    if (count > 0) {
                        JOptionPane.showMessageDialog(this, "号源删除成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                        loadSlotData(); // 刷新号源列表
                    } else {
                        JOptionPane.showMessageDialog(this, "号源删除失败，未找到该号源！", "错误", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    // 处理外键约束错误
                    String errorMessage = ex.getMessage();
                    if (errorMessage.contains("foreign key constraint") || errorMessage.contains("Cannot delete or update a parent row")) {
                        JOptionPane.showMessageDialog(this, "删除失败：该号源已被患者预约，无法直接删除！\n请先取消该号源的所有预约。", "错误", JOptionPane.ERROR_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "删除失败：" + errorMessage, "错误", JOptionPane.ERROR_MESSAGE);
                    }
                    ex.printStackTrace();
                } finally {
                    DBUtil.close(deleteConn, null, null);
                }
            }
        });

        // 号源表格
        String[] slotColumns = {"号源ID", "医生", "出诊日期", "时段", "总号源", "剩余号源"};
        slotModel = new DefaultTableModel(null, slotColumns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        slotTable = new JTable(slotModel);
        slotTable.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        slotTable.setRowHeight(30);
        slotTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));
        JScrollPane scrollPane = new JScrollPane(slotTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * 加载所有数据
     */
    private void loadAllData() {
        loadPatientData("");
        loadDeptData();
        loadSlotData();
        loadDoctorData("");
    }

    /**
     * 加载患者数据
     */
    private void loadPatientData(String name) {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            ResultSet rs = patientDao.listByName(conn, name);
            patientModel.setRowCount(0);
            while (rs.next()) {
                Object[] row = {
                        rs.getLong("patientId"),
                        rs.getString("name"),
                        rs.getInt("gender") == 1 ? "男" : "女",
                        rs.getString("idCard"),
                        rs.getString("phone"),
                        new SimpleDateFormat("yyyy-MM-dd").format(rs.getTimestamp("createTime"))
                };
                patientModel.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "加载患者失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        } finally {
            DBUtil.close(conn, null, null);
        }
    }

    /**
     * 加载科室数据
     */
    private void loadDeptData() {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            ResultSet rs = deptDao.listAll(conn);
            deptModel.setRowCount(0);
            while (rs.next()) {
                Object[] row = {
                        rs.getLong("deptId"),
                        rs.getString("deptName"),
                        rs.getString("description")
                };
                deptModel.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "加载科室失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        } finally {
            DBUtil.close(conn, null, null);
        }
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

    /**
     * 加载号源数据
     */
    private void loadSlotData() {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            ResultSet rs = slotDao.listAll(conn);
            slotModel.setRowCount(0);
            while (rs.next()) {
                // 根据doctorId获取医生姓名
                String doctorName = getDoctorNameById(conn, rs.getLong("doctorId"));
                Object[] row = {
                        rs.getLong("slotId"),
                        doctorName,
                        new SimpleDateFormat("yyyy-MM-dd").format(rs.getDate("visitDate")),
                        rs.getString("timeSlot"),
                        rs.getInt("totalNum"),
                        rs.getInt("remainingNum")
                };
                slotModel.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "加载号源失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        } finally {
            DBUtil.close(conn, null, null);
        }
    }

    /**
     * 保存号源配置
     */
    private void saveAppointmentSlot(Long doctorId, Date visitDate, String timeSlot, int totalNum) {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            AppointmentSlot slot = new AppointmentSlot();
            slot.setDoctorId(doctorId);
            slot.setVisitDate(visitDate);
            slot.setTimeSlot(timeSlot);
            slot.setTotalNum(totalNum);
            slot.setRemainingNum(totalNum); // 初始剩余号源=总号源

            int result = slotDao.add(conn, slot);
            if (result > 0) {
                JOptionPane.showMessageDialog(this, "号源配置成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                loadSlotData(); // 刷新号源列表
            } else {
                JOptionPane.showMessageDialog(this, "号源配置失败！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // 检查是否是唯一约束违反错误（MySQL错误代码1062）
            if (e instanceof com.mysql.cj.jdbc.exceptions.MysqlDataTruncation) {
                JOptionPane.showMessageDialog(this, "号源数据格式错误！", "错误", JOptionPane.ERROR_MESSAGE);
            } else if (e.getErrorCode() == 1062) {
                // 唯一约束违反，提示用户该号源已存在
                JOptionPane.showMessageDialog(this, "该医生在该日期和时段的号源已存在！", "错误", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "配置异常：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        } finally {
            DBUtil.close(conn, null, null);
        }
    }

    /**
     * 根据医生ID获取姓名
     */
    private String getDoctorNameById(Connection conn, Long doctorId) {
        // 1. 前置校验：避免无效查询
        if (doctorId == null || doctorId <= 0) {
            return "未知医生()";
        }
        if (conn == null) {
            throw new RuntimeException("数据库连接为空，无法查询医生信息");
        }

        ResultSet rs = null;
        Doctor doctor = null;
        try {
            // 2. 查询医生信息
            rs = doctorDao.getById(conn, doctorId);
            if (rs != null && rs.next()) {
                doctor = new Doctor(
                        rs.getLong("doctorId"),
                        rs.getString("name"),
                        rs.getString("title"),
                        rs.getString("licenseNo"),
                        rs.getLong("deptId")
                );
            }
        } catch (SQLException e) {
            // 3. 增强异常信息，便于排查（携带医生ID）
            throw new RuntimeException("查询医生信息失败，doctorId=" + doctorId, e);
        } finally {
            // 4. 释放ResultSet资源（关键：避免连接泄漏）
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    // 关闭资源的异常无需抛上层，记录日志即可
                    System.err.println("关闭ResultSet失败：" + e.getMessage());
                }
            }
        }

        // 5. 简化空值处理，统一拼接逻辑
        String name = doctor != null ? doctor.getName() : "未知医生";
        String title = doctor != null ? (doctor.getTitle() == null ? "" : doctor.getTitle()) : "";
        return String.format("%s(%s)", name, title);
    }

    /**
     * 新增患者对话框（内部类）
     */
    class PatientAddDialog extends JDialog {
        public PatientAddDialog(JFrame parent) {
            super(parent, "新增患者", true);
            setSize(500, 400);
            setLocationRelativeTo(parent);
            setResizable(false);

            JPanel panel = new JPanel(new GridLayout(5, 2, 10, 20));
            panel.setBorder(new EmptyBorder(20, 20, 20, 20));
            panel.setBackground(Color.WHITE);

            // 表单组件
            panel.add(new JLabel("姓名："));
            JTextField nameField = new JTextField();
            panel.add(nameField);

            panel.add(new JLabel("性别："));
            JComboBox<String> genderBox = new JComboBox<>(new String[]{"男", "女"});
            panel.add(genderBox);

            panel.add(new JLabel("身份证号："));
            JTextField idCardField = new JTextField();
            panel.add(idCardField);

            panel.add(new JLabel("联系电话："));
            JTextField phoneField = new JTextField();
            panel.add(phoneField);

            // 按钮面板
            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
            JButton saveBtn = new JButton("保存");
            JButton cancelBtn = new JButton("取消");

            // 保存事件
            saveBtn.addActionListener(e -> {
                String name = nameField.getText().trim();
                String idCard = idCardField.getText().trim();
                String phone = phoneField.getText().trim();
                int gender = genderBox.getSelectedIndex() + 1; // 1-男，2-女
                if (name.isEmpty() || idCard.isEmpty() || phone.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "请填写完整信息！", "提示", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // 保存患者
                Connection conn = null;
                try {
                    conn = DBUtil.getConnection();
                    Patient patient = new Patient();
                    patient.setName(name);
                    patient.setIdCard(idCard);
                    patient.setPhone(phone);
                    patient.setGender(gender);
                    patient.setCreateTime(new Date());

                    int result = patientDao.add(conn, patient);
                    if (result > 0) {
                        JOptionPane.showMessageDialog(this, "新增成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                        dispose();
                        loadPatientData(""); // 刷新患者列表
                    } else {
                        JOptionPane.showMessageDialog(this, "新增失败！", "错误", JOptionPane.ERROR_MESSAGE);
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

            // 组装界面
            add(panel, BorderLayout.CENTER);
            add(btnPanel, BorderLayout.SOUTH);

            setVisible(true);
        }
    }

    /**
     * 新增科室对话框（内部类）
     */
    class DepartmentAddDialog extends JDialog {
        public DepartmentAddDialog(JFrame parent) {
            super(parent, "新增科室", true);
            setSize(500, 250);
            setLocationRelativeTo(parent);
            setResizable(false);
            JPanel panel = new JPanel(new GridLayout(3, 2, 10, 30));
            panel.setBorder(new EmptyBorder(20, 20, 20, 20));
            panel.setBackground(Color.WHITE);

            panel.add(new JLabel("科室名称："));
            JTextField nameField = new JTextField();
            panel.add(nameField);

            panel.add(new JLabel("科室描述："));
            JTextField descField = new JTextField();
            panel.add(descField);

            // 按钮面板
            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
            JButton saveBtn = new JButton("保存");
            JButton cancelBtn = new JButton("取消");

            saveBtn.addActionListener(e -> {
                String deptName = nameField.getText().trim();
                String desc = descField.getText().trim();
                if (deptName.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "科室名称不能为空！", "提示", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                Connection conn = null;
                try {
                    System.out.println("尝试获取数据库连接...");
                    conn = DBUtil.getConnection();
                    if (conn == null) {
                        JOptionPane.showMessageDialog(this, "数据库连接失败！", "错误", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    System.out.println("数据库连接成功");

                    Department dept = new Department();
                    dept.setDeptName(deptName);
                    dept.setDescription(desc);
                    System.out.println("准备执行SQL: INSERT INTO department(deptName, description) VALUES('" + deptName + "', '" + desc + "')");
                    int result = deptDao.add(conn, dept);
                    System.out.println("SQL执行结果: " + result);

                    if (result > 0) {
                        JOptionPane.showMessageDialog(this, "新增成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                        dispose();
                        loadDeptData(); // 刷新科室列表
                    } else {
                        JOptionPane.showMessageDialog(this, "新增失败！", "错误", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    System.err.println("SQL异常: " + ex.getMessage());
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

    /**
     * 创建用户管理面板
     */
    private JPanel createUserPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(Color.WHITE);

        // 创建顶部搜索面板
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        searchPanel.setBackground(Color.WHITE);
        JLabel usernameLabel = new JLabel("用户名：");
        JTextField usernameField = new JTextField(20);
        JButton searchBtn = new JButton("搜索");
        JButton refreshBtn = new JButton("刷新"); // 刷新按钮放在搜索后面

        searchBtn.addActionListener(e -> loadUserData(usernameField.getText().trim()));
        refreshBtn.addActionListener(e -> {
            usernameField.setText("");
            loadUserData("");
        });

        searchPanel.add(usernameLabel);
        searchPanel.add(usernameField);
        searchPanel.add(searchBtn);
        searchPanel.add(refreshBtn);
        panel.add(searchPanel, BorderLayout.NORTH);

        // 用户表格
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

        // 创建底部操作按钮面板
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        bottomPanel.setBackground(Color.WHITE);
        JButton addBtn = new JButton("新增用户");
        JButton editBtn = new JButton("修改用户");
        JButton deleteBtn = new JButton("删除用户");

        addBtn.addActionListener(e -> new UserAddDialog(this));
        // 修改用户按钮事件
        editBtn.addActionListener(e -> {
            int selectedRow = userTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "请先选择要修改的用户！", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 获取选中用户的信息
            String username = (String) userModel.getValueAt(selectedRow, 0);
            String password = (String) userModel.getValueAt(selectedRow, 1);
            String identity = (String) userModel.getValueAt(selectedRow, 2);

            // 打开修改对话框
            new UserEditDialog(this, username, password, identity);
        });
        // 删除用户按钮事件
        deleteBtn.addActionListener(e -> {
            int selectedRow = userTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "请先选择要删除的用户！", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 获取用户名
            String username = (String) userModel.getValueAt(selectedRow, 0);
            // 确认删除
            int confirm = JOptionPane.showConfirmDialog(this,
                    "确定要删除用户\"" + username + "\"吗？\n此操作不可恢复！",
                    "删除确认", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                Connection conn = null;
                try {
                    conn = DBUtil.getConnection();
                    int count = userDao.deleteUserByUsername(conn, username);
                    if (count > 0) {
                        JOptionPane.showMessageDialog(this, "用户删除成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                        loadUserData(); // 刷新用户列表
                    } else {
                        JOptionPane.showMessageDialog(this, "用户删除失败，未找到该用户！", "错误", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "删除失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                } finally {
                    DBUtil.close(conn, null, null);
                }
            }
        });

        bottomPanel.add(addBtn); // 新增按钮
        bottomPanel.add(editBtn); // 修改按钮
        bottomPanel.add(deleteBtn); // 删除按钮
        panel.add(bottomPanel, BorderLayout.SOUTH);

        loadUserData(); // 初始加载数据
        return panel;
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


    // 新增用户对话框（内部类）
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

                if (username.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "用户名和密码不能为空！", "提示", JOptionPane.WARNING_MESSAGE);
                    return;
                }
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

    // 修改用户对话框（内部类）
    class UserEditDialog extends JDialog {
        private String originalUsername;

        public UserEditDialog(JFrame parent, String originalUsername, String password, String identity) {
            super(parent, "修改用户", true);
            this.originalUsername = originalUsername;
            setSize(500, 300);
            setLocationRelativeTo(parent);
            setResizable(false);

            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBorder(new EmptyBorder(20, 20, 20, 20));
            panel.setBackground(Color.WHITE);

            // 原用户名行
            JPanel originalUsernamePanel = new JPanel();
            originalUsernamePanel.setLayout(new BoxLayout(originalUsernamePanel, BoxLayout.X_AXIS));
            originalUsernamePanel.setBackground(Color.WHITE);
            JLabel originalUsernameLabel = new JLabel("原用户名：");
            originalUsernameLabel.setPreferredSize(new Dimension(80, 25));
            originalUsernamePanel.add(originalUsernameLabel);
            originalUsernamePanel.add(new JLabel(originalUsername));
            originalUsernamePanel.add(Box.createHorizontalGlue());
            panel.add(originalUsernamePanel);
            panel.add(Box.createVerticalStrut(10));

            // 新用户名行
            JPanel newUsernamePanel = new JPanel();
            newUsernamePanel.setLayout(new BoxLayout(newUsernamePanel, BoxLayout.X_AXIS));
            newUsernamePanel.setBackground(Color.WHITE);
            JLabel newUsernameLabel = new JLabel("新用户名：");
            newUsernameLabel.setPreferredSize(new Dimension(80, 25));
            newUsernamePanel.add(newUsernameLabel);
            JTextField usernameField = new JTextField(originalUsername);
            usernameField.setPreferredSize(new Dimension(200, 25));
            newUsernamePanel.add(usernameField);
            newUsernamePanel.add(Box.createHorizontalGlue());
            panel.add(newUsernamePanel);
            panel.add(Box.createVerticalStrut(10));

            // 密码行
            JPanel passwordPanel = new JPanel();
            passwordPanel.setLayout(new BoxLayout(passwordPanel, BoxLayout.X_AXIS));
            passwordPanel.setBackground(Color.WHITE);
            JLabel passwordLabel = new JLabel("密码：");
            passwordLabel.setPreferredSize(new Dimension(80, 25));
            passwordPanel.add(passwordLabel);
            JPasswordField passwordField = new JPasswordField(password);
            passwordField.setPreferredSize(new Dimension(200, 25));
            passwordPanel.add(passwordField);
            passwordPanel.add(Box.createHorizontalGlue());
            panel.add(passwordPanel);
            panel.add(Box.createVerticalStrut(10));

            // 身份行
            JPanel identityPanel = new JPanel();
            identityPanel.setLayout(new BoxLayout(identityPanel, BoxLayout.X_AXIS));
            identityPanel.setBackground(Color.WHITE);
            JLabel identityLabel = new JLabel("身份：");
            identityLabel.setPreferredSize(new Dimension(80, 25));
            identityPanel.add(identityLabel);
            JComboBox<String> identityBox = new JComboBox<>(new String[]{"admin", "doctor"});
            identityBox.setSelectedItem(identity);
            identityBox.setPreferredSize(new Dimension(200, 25));
            identityPanel.add(identityBox);
            identityPanel.add(Box.createHorizontalGlue());
            panel.add(identityPanel);
            panel.add(Box.createVerticalStrut(20));

            // 按钮面板
            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
            btnPanel.setBackground(Color.WHITE);
            JButton saveBtn = new JButton("保存");
            JButton cancelBtn = new JButton("取消");

            // 保存事件
            saveBtn.addActionListener(e -> {
                String newUsername = usernameField.getText().trim();
                String newPassword = new String(passwordField.getPassword()).trim();
                String newIdentity = (String) identityBox.getSelectedItem();

                if (newUsername.isEmpty() || newPassword.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "用户名和密码不能为空！", "提示", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (newPassword.length() != 6) {
                    JOptionPane.showMessageDialog(this, "密码必须为6位数！", "提示", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                Connection conn = null;
                try {
                    conn = DBUtil.getConnection();
                    Users user = new Users(newUsername, newPassword, newIdentity);
                    int count = userDao.updateUser(conn, originalUsername, user);
                    if (count > 0) {
                        JOptionPane.showMessageDialog(this, "用户修改成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                        dispose();
                        loadUserData(); // 刷新用户列表
                    } else {
                        JOptionPane.showMessageDialog(this, "用户修改失败，未找到该用户！", "错误", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "修改失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                } finally {
                    DBUtil.close(conn, null, null);
                }
            });

            // 取消事件
            cancelBtn.addActionListener(e -> dispose());

            btnPanel.add(saveBtn);
            btnPanel.add(cancelBtn);
            panel.add(btnPanel);

            add(panel);
            setVisible(true);
        }
    }


    /**
     * 创建医生管理面板
     */
    private JPanel createDoctorPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(Color.WHITE);

        // 搜索面板
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        searchPanel.setBackground(Color.WHITE);
        JLabel nameLabel = new JLabel("医生姓名：");
        JTextField nameField = new JTextField(20);
        JButton searchBtn = new JButton("搜索");
        JButton refreshBtn = new JButton("刷新");
        JButton addBtn = new JButton("新增医生");
        JButton deleteBtn = new JButton("删除医生");

        // 新增医生按钮事件
        addBtn.addActionListener(e -> new DoctorAddDialog(this));
        // 搜索按钮事件
        searchBtn.addActionListener(e -> loadDoctorData(nameField.getText().trim()));
        // 刷新按钮事件
        refreshBtn.addActionListener(e -> {
            nameField.setText("");
            loadDoctorData("");
        });
        // 删除医生按钮事件
        deleteBtn.addActionListener(e -> {
            int selectedRow = doctorTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "请先选择要删除的医生！", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 获取医生ID和姓名
            Long doctorId = (Long) doctorModel.getValueAt(selectedRow, 0);
            String doctorName = (String) doctorModel.getValueAt(selectedRow, 1);

            // 确认删除
            int confirm = JOptionPane.showConfirmDialog(this,
                    "确定要删除医生\"" + doctorName + "\"吗？\n此操作不可恢复！",
                    "删除确认", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                Connection conn = null;
                try {
                    conn = DBUtil.getConnection();
                    int count = doctorDao.deleteById(conn, doctorId);
                    if (count > 0) {
                        JOptionPane.showMessageDialog(this, "医生删除成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                        loadDoctorData(""); // 刷新医生列表
                        nameField.setText(""); // 清空搜索框
                    } else {
                        JOptionPane.showMessageDialog(this, "医生删除失败，未找到该医生！", "错误", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    String errorMessage = ex.getMessage();
                    if (errorMessage != null && (errorMessage.contains("foreign key constraint") || errorMessage.contains("Cannot delete or update a parent row"))) {
                        JOptionPane.showMessageDialog(this, "删除失败：该医生存在关联的咨询记录，无法直接删除！请先处理相关的号源或接诊记录后再尝试删除。", "错误", JOptionPane.ERROR_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "删除失败：" + errorMessage, "错误", JOptionPane.ERROR_MESSAGE);
                    }
                } finally {
                    DBUtil.close(conn, null, null);
                }
            }
        });

        // 将搜索相关组件添加到搜索面板
        searchPanel.add(nameLabel);
        searchPanel.add(nameField);
        searchPanel.add(searchBtn);
        searchPanel.add(refreshBtn);
        panel.add(searchPanel, BorderLayout.NORTH);

        // 医生表格
        String[] doctorColumns = {"医生ID", "姓名", "职称", "执业证书号", "所属科室ID"};
        doctorModel = new DefaultTableModel(null, doctorColumns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        doctorTable = new JTable(doctorModel);
        doctorTable.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        doctorTable.setRowHeight(30);
        doctorTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));
        JScrollPane scrollPane = new JScrollPane(doctorTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // 创建底部操作按钮面板
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.add(addBtn); // 新增按钮在前
        bottomPanel.add(deleteBtn); // 删除按钮在后
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * 加载医生数据
     */
    private void loadDoctorData(String name) {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            ResultSet rs = doctorDao.listByName(conn, name);
            doctorModel.setRowCount(0);
            while (rs.next()) {
                Object[] row = {
                        rs.getLong("doctorId"),
                        rs.getString("name"),
                        rs.getString("title"),
                        rs.getString("licenseNo"),
                        rs.getLong("deptId")
                };
                doctorModel.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "加载医生失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        } finally {
            DBUtil.close(conn, null, null);
        }
    }

    /**
     * 新增医生对话框（内部类）
     */
    class DoctorAddDialog extends JDialog {
        public DoctorAddDialog(JFrame parent) {
            super(parent, "新增医生", true);
            setSize(500, 400);
            setLocationRelativeTo(parent);
            setResizable(false);

            JPanel panel = new JPanel(new GridLayout(4, 2, 10, 20));
            panel.setBorder(new EmptyBorder(20, 20, 20, 20));
            panel.setBackground(Color.WHITE);

            // 表单组件
            panel.add(new JLabel("姓名："));
            JTextField nameField = new JTextField();
            panel.add(nameField);

            panel.add(new JLabel("职称："));
            JTextField titleField = new JTextField();
            panel.add(titleField);

            panel.add(new JLabel("执业证书号："));
            JTextField licenseNoField = new JTextField();
            panel.add(licenseNoField);

            panel.add(new JLabel("所属科室ID："));
            JTextField deptIdField = new JTextField();
            panel.add(deptIdField);

            // 按钮面板
            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
            JButton saveBtn = new JButton("保存");
            JButton cancelBtn = new JButton("取消");

            // 保存事件
            saveBtn.addActionListener(e -> {
                String name = nameField.getText().trim();
                String title = titleField.getText().trim();
                String licenseNo = licenseNoField.getText().trim();
                Long deptId;

                try {
                    deptId = Long.parseLong(deptIdField.getText().trim());
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "科室ID必须是数字！", "提示", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (name.isEmpty() || title.isEmpty() || licenseNo.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "请填写完整信息！", "提示", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // 保存医生
                Connection conn = null;
                try {
                    conn = DBUtil.getConnection();
                    Doctor doctor = new Doctor();
                    doctor.setName(name);
                    doctor.setTitle(title);
                    doctor.setLicenseNo(licenseNo);
                    doctor.setDeptId(deptId);

                    int result = doctorDao.add(conn, doctor);
                    if (result > 0) {
                        JOptionPane.showMessageDialog(this, "新增成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                        dispose();
                        loadDoctorData(""); // 刷新医生列表
                    } else {
                        JOptionPane.showMessageDialog(this, "新增失败！", "错误", JOptionPane.ERROR_MESSAGE);
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

            // 组装界面
            add(panel, BorderLayout.CENTER);
            add(btnPanel, BorderLayout.SOUTH);

            setVisible(true);
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
        Users admin = new Users();
        admin.setUsername("admin");
        admin.setPassword("123456");
        admin.setIdentity("admin");
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new AdminManagementFrm(admin);
            }
        });
    }
}