package Model;

import java.util.Date;

/**
 * 挂号记录实体类，映射 RegistrationRecord 表
 */
public class RegistrationRecord {
    private Long recordId; // 自增主键
    private Long patientId; // 外键，关联患者表
    private Long slotId; // 外键，关联号源表
    private String registrationNo; // 挂号单号（唯一）
    private Date registrationTime; // 挂号时间
    private String status; // 挂号状态（未就诊/已就诊/已取消）

    // 无参构造函数
    public RegistrationRecord() {}

    // 全参构造函数
    public RegistrationRecord(Long recordId, Long patientId, Long slotId, String registrationNo, Date registrationTime, String status) {
        this.recordId = recordId;
        this.patientId = patientId;
        this.slotId = slotId;
        this.registrationNo = registrationNo;
        this.registrationTime = registrationTime;
        this.status = status;
    }

    // getter/setter 方法
    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public Long getSlotId() {
        return slotId;
    }

    public void setSlotId(Long slotId) {
        this.slotId = slotId;
    }

    public String getRegistrationNo() {
        return registrationNo;
    }

    public void setRegistrationNo(String registrationNo) {
        this.registrationNo = registrationNo;
    }

    public Date getRegistrationTime() {
        return registrationTime;
    }

    public void setRegistrationTime(Date registrationTime) {
        this.registrationTime = registrationTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}