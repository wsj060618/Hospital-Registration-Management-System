package Model;

import java.util.Date;

/**
 * 患者实体类，映射 Patient 表
 */
public class Patient {
    private Long patientId; // 自增主键（系统分配唯一标识）
    private String idCard; // 身份证号（唯一）
    private String name; // 患者姓名
    private Integer gender; // 性别：0-未知，1-男，2-女
    private String phone; // 联系电话（唯一）
    private Date createTime; // 建档时间

    // 无参构造函数
    public Patient() {}

    // 全参构造函数
    public Patient(Long patientId, String idCard, String name, Integer gender, String phone, Date createTime) {
        this.patientId = patientId;
        this.idCard = idCard;
        this.name = name;
        this.gender = gender;
        this.phone = phone;
        this.createTime = createTime;
    }

    // getter/setter 方法
    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
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