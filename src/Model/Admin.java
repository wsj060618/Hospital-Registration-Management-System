package Model;

import java.util.Date;

/**
 * 管理员实体类，映射 Admin 表
 */
public class Admin {
    private Long adminId; // 自增主键
    private String name; // 管理员姓名
    private String phone; // 联系电话
    private Date createTime; // 创建时间

    // 无参构造函数
    public Admin() {}

    // 全参构造函数
    public Admin(Long adminId, String name, String phone, Date createTime) {
        this.adminId = adminId;
        this.name = name;
        this.phone = phone;
        this.createTime = createTime;
    }

    // getter/setter 方法
    public Long getAdminId() {
        return adminId;
    }

    public void setAdminId(Long adminId) {
        this.adminId = adminId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}