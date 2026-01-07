package Model;

/**
 * 医生实体类，映射 Doctor 表
 */
public class Doctor {
    private Long doctorId; // 自增主键
    private String name; // 医生姓名
    private String title; // 职称（主治医师/主任医师等）
    private String licenseNo; // 执业证书号（唯一）
    private Long deptId; // 外键，关联科室表

    // 无参构造函数
    public Doctor() {}

    // 全参构造函数
    public Doctor(Long doctorId, String name, String title, String licenseNo, Long deptId) {
        this.doctorId = doctorId;
        this.name = name;
        this.title = title;
        this.licenseNo = licenseNo;
        this.deptId = deptId;
    }

    // getter/setter 方法
    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLicenseNo() {
        return licenseNo;
    }

    public void setLicenseNo(String licenseNo) {
        this.licenseNo = licenseNo;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    // 重写toString()方法，使JComboBox显示医生姓名和职称
    @Override
    public String toString() {
        return name + " (" + title + ")";
    }
}