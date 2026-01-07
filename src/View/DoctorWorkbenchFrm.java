package View;

import Model.Doctor;
import Model.Patient;
import Model.RegistrationRecord;
import Dao.DoctorDao;
import Dao.PatientDao;
import Dao.RegistrationRecordDao;
import Util.DBUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 医生工作台界面
 */
public class DoctorWorkbenchFrm extends JFrame {
    private JTable registrationTable; // 接诊列表表格
    private DefaultTableModel tableModel; // 表格数据模型
    private Doctor currentDoctor; // 当前登录医生
    private SimpleDateFormat dateFormat; // 日期格式化器
    private JTextField nameFilterField; // 姓名筛选输入框

    // DAO层对象
    private RegistrationRecordDao registrationDao = new RegistrationRecordDao();
    private PatientDao patientDao = new PatientDao();
    private DoctorDao doctorDao = new DoctorDao();

    public DoctorWorkbenchFrm(Doctor doctor) {
        this.currentDoctor = doctor;
        dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        initFrame();
        initComponents();
        loadRegistrationData(null); // 加载接诊数据，初始无筛选
        setVisible(true);
    }

    // 初始化窗口
    private void initFrame() {
        getDefaultIcon();
        setTitle("医生工作台 - " + currentDoctor.getName());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1000, 700);
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
        JLabel titleLabel = new JLabel("今日接诊管理", JLabel.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        titleLabel.setForeground(Color.RED); // 改为红色
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // 搜索面板 - 放在标题下方
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(Color.WHITE);

        JLabel nameLabel = new JLabel("患者姓名：");
        nameLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        searchPanel.add(nameLabel);

        nameFilterField = new JTextField(15);
        nameFilterField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        searchPanel.add(nameFilterField);

        JButton filterBtn = new JButton("筛选");
        filterBtn.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        filterBtn.setPreferredSize(new Dimension(80, 30));
        filterBtn.setBackground(Color.WHITE);
        filterBtn.setForeground(Color.BLACK);
        filterBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String patientName = nameFilterField.getText().trim();
                loadRegistrationData(patientName);
            }
        });
        searchPanel.add(filterBtn);

        JButton clearFilterBtn = new JButton("清除筛选");
        clearFilterBtn.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        clearFilterBtn.setPreferredSize(new Dimension(100, 30));
        clearFilterBtn.setBackground(Color.WHITE);
        clearFilterBtn.setForeground(Color.BLACK);
        clearFilterBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                nameFilterField.setText("");
                loadRegistrationData(null);
            }
        });
        searchPanel.add(clearFilterBtn);

        // 创建一个中间面板来容纳搜索面板和表格面板
        JPanel centerPanel = new JPanel(new BorderLayout(0, 20));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.add(searchPanel, BorderLayout.NORTH);

        // 表格面板
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        // 表格列名
        String[] columnNames = {"挂号单号", "患者姓名", "性别", "联系电话", "接诊时间", "挂号时间", "就诊状态", "操作"};
        tableModel = new DefaultTableModel(null, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7; // 仅操作列可点击
            }
        };
        registrationTable = new JTable(tableModel);
        registrationTable.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        registrationTable.setRowHeight(35);
        registrationTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));
        registrationTable.getTableHeader().setBackground(Color.RED); // 改为红色
        registrationTable.getTableHeader().setForeground(Color.BLACK);

        // 设置列宽
        registrationTable.getColumnModel().getColumn(0).setPreferredWidth(120);
        registrationTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        registrationTable.getColumnModel().getColumn(2).setPreferredWidth(60);
        registrationTable.getColumnModel().getColumn(3).setPreferredWidth(110);
        registrationTable.getColumnModel().getColumn(4).setPreferredWidth(100); // 接诊时间列宽
        registrationTable.getColumnModel().getColumn(5).setPreferredWidth(150);
        registrationTable.getColumnModel().getColumn(6).setPreferredWidth(80);
        registrationTable.getColumnModel().getColumn(7).setPreferredWidth(100);

        // 操作列按钮渲染
        registrationTable.getColumnModel().getColumn(7).setCellRenderer(new ButtonRenderer());
        registrationTable.getColumnModel().getColumn(7).setCellEditor(new ButtonEditor(new JCheckBox()));

        JScrollPane scrollPane = new JScrollPane(registrationTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // 将表格面板添加到中间面板的中心
        centerPanel.add(tablePanel, BorderLayout.CENTER);

        // 将中间面板添加到主面板的中心
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // 底部按钮面板
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        btnPanel.setBackground(Color.WHITE);
        JButton refreshBtn = new JButton("刷新列表");
        refreshBtn.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        refreshBtn.setPreferredSize(new Dimension(100, 35));
        refreshBtn.setBackground(Color.RED); // 改为红色
        refreshBtn.setForeground(Color.WHITE); // 设置文字为白色
        refreshBtn.addActionListener(e -> loadRegistrationData(nameFilterField.getText().trim()));
        btnPanel.add(refreshBtn);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);
    }

    /**
     * 加载接诊列表数据 - 仅显示今天的号源，支持姓名筛选
     */
    private void loadRegistrationData(String patientName) {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            // 使用今天的日期
            String todayStr = dateFormat.format(new Date());
            
            // 查询当前医生当天的挂号记录，支持姓名筛选
            ResultSet rs = registrationDao.listByDoctorAndDate(conn, currentDoctor.getDoctorId(), todayStr, patientName);
            // 清空表格
            tableModel.setRowCount(0);
    
            while (rs.next()) {
                RegistrationRecord record = new RegistrationRecord();
                record.setRecordId(rs.getLong("recordId"));
                record.setRegistrationNo(rs.getString("registrationNo"));
                record.setRegistrationTime(rs.getTimestamp("registrationTime"));
                record.setStatus(rs.getString("status"));
    
                // 获取患者信息
                Patient patient = patientDao.getById(conn, rs.getLong("patientId"));
                if (patient == null) continue;
    
                // 获取接诊时间
                Date visitDate = rs.getDate("visitDate");
                String timeSlot = rs.getString("timeSlot");
                String visitTime = timeSlot; // 直接使用时间段作为接诊时间显示
    
                // 根据就诊状态设置操作按钮文字
                String actionText;
                if ("已就诊".equals(record.getStatus())) {
                    actionText = "取消已就诊";
                } else {
                    actionText = "标记已就诊";
                }
    
                // 组装表格行数据
                Object[] rowData = {
                        record.getRegistrationNo(),
                        patient.getName(),
                        patient.getGender() == 1 ? "男" : "女",
                        patient.getPhone(),
                        visitTime, // 显示接诊时间
                        new SimpleDateFormat("yyyy-MM-dd HH:mm").format(record.getRegistrationTime()),
                        record.getStatus(),
                        actionText
                };
                tableModel.addRow(rowData);
            }

            tableModel.fireTableDataChanged();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "加载接诊列表失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        } finally {
            DBUtil.close(conn, null, null);
        }
    }

    /**
     * 标记就诊状态（已就诊/取消已就诊）
     */
    private boolean updateVisitStatus(Connection conn, Long recordId, String newStatus) {
        boolean autoCommit = false;
        try {
            // 保存原有自动提交状态
            autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            // 直接更新为指定的新状态
            boolean success = registrationDao.updateStatus(conn, recordId, newStatus);
            if (success) {
                conn.commit();
                return true;
            } else {
                if (!conn.isClosed()) {
                    conn.rollback();
                }
                return false;
            }
        } catch (SQLException e) {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                // 无论成败，都恢复自动提交状态
                if (conn != null && !conn.isClosed()) {
                    conn.setAutoCommit(autoCommit);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 表格按钮渲染器（自定义操作按钮）
     */
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setBackground(Color.WHITE); // 改为白色
            setForeground(Color.BLACK); // 设置文字为黑色
            setFont(new Font("微软雅黑", Font.PLAIN, 12));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText(value.toString());
            return this;
        }
    }

    /**
     * 表格按钮编辑器（处理按钮点击事件）
     */
    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean isPushed;
        private int currentRow; // 新增：记录当前编辑行索引

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.setBackground(Color.WHITE); // 改为白色
            button.setForeground(Color.BLACK); // 设置文字为黑色
            button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    fireEditingStopped();
                    loadRegistrationData(nameFilterField.getText().trim());
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            isPushed = true;
            currentRow = row; // 新增：记录当前编辑行索引
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                // 获取当前行的挂号单号
                String registrationNo = (String) tableModel.getValueAt(currentRow, 0);
                if (registrationNo == null || registrationNo.isEmpty()) {
                    JOptionPane.showMessageDialog(DoctorWorkbenchFrm.this, "挂号单号为空，无法操作！", "错误", JOptionPane.ERROR_MESSAGE);
                    isPushed = false;
                    return label;
                }

                // 直接从表格中获取当前状态（第6列），而不是依赖按钮文字
                String currentStatus = (String) tableModel.getValueAt(currentRow, 6);
                String newStatus;
                if ("已就诊".equals(currentStatus)) {
                    newStatus = "未就诊";
                } else {
                    newStatus = "已就诊";
                }

                // 根据挂号单号查询recordId并更新状态
                Connection conn = null;
                try {
                    conn = DBUtil.getConnection();
                    Long recordId = registrationDao.getRecordIdByNo(conn, registrationNo);

                    // 校验recordId是否有效
                    if (recordId == null || recordId <= 0) {
                        JOptionPane.showMessageDialog(DoctorWorkbenchFrm.this, "未查询到该挂号单的记录ID！", "错误", JOptionPane.ERROR_MESSAGE);
                        return label;
                    }

                    // 直接调用更新方法，传入新状态
                    boolean success = updateVisitStatus(conn, recordId, newStatus);
                    if (success) {
                        JOptionPane.showMessageDialog(DoctorWorkbenchFrm.this, "操作成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                        // 刷新列表，保持当前筛选条件
                        loadRegistrationData(nameFilterField.getText().trim());
                    } else {
                        JOptionPane.showMessageDialog(DoctorWorkbenchFrm.this, "更新状态失败，请重试！", "错误", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(DoctorWorkbenchFrm.this, "操作失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                } finally {
                    DBUtil.close(conn, null, null);
                }
            }
            isPushed = false;
            return label;
        }

        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
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
        // 模拟登录医生（实际从登录界面传递）
        Doctor testDoctor = new Doctor();
        testDoctor.setDoctorId(1L);
        testDoctor.setName("王医生");
        testDoctor.setTitle("主任医师");
        testDoctor.setDeptId(1L);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new DoctorWorkbenchFrm(testDoctor);
            }
        });
    }
}