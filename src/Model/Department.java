package Model;

/**
 * 科室实体类，映射 Department 表
 */
public class Department {
    private Long deptId; // 自增主键
    private String deptName; // 科室名称（唯一）
    private String description; // 科室描述
    // 无参构造函数
    public Department() {}

    // 全参构造函数
    public Department(Long deptId, String deptName, String description, Long adminId) {
        this.deptId = deptId;
        this.deptName = deptName;
        this.description = description;
    }

    // getter/setter 方法
    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // 重写toString()方法，使JComboBox显示科室名称
    @Override
    public String toString() {
        return deptName;
    }
}