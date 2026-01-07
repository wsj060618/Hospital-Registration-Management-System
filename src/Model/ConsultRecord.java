package Model;

import java.util.Date;

/**
 * 接诊实体类，映射 ConsultRecord 表
 */
public class ConsultRecord {
    private Long consultId; // 自增主键
    private Long recordId; // 外键，关联挂号记录表（唯一）
    private Long doctorId; // 外键，关联医生表
    private Date consultTime; // 接诊时间
    private String visitStatus; // 就诊状态（未就诊/已就诊）
    private String initialDiagnosis; // 初步诊断

    // 无参构造函数
    public ConsultRecord() {}

    // 全参构造函数
    public ConsultRecord(Long consultId, Long recordId, Long doctorId, Date consultTime, String visitStatus, String initialDiagnosis) {
        this.consultId = consultId;
        this.recordId = recordId;
        this.doctorId = doctorId;
        this.consultTime = consultTime;
        this.visitStatus = visitStatus;
        this.initialDiagnosis = initialDiagnosis;
    }

    // getter/setter 方法
    public Long getConsultId() {
        return consultId;
    }

    public void setConsultId(Long consultId) {
        this.consultId = consultId;
    }

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public Date getConsultTime() {
        return consultTime;
    }

    public void setConsultTime(Date consultTime) {
        this.consultTime = consultTime;
    }

    public String getVisitStatus() {
        return visitStatus;
    }

    public void setVisitStatus(String visitStatus) {
        this.visitStatus = visitStatus;
    }

    public String getInitialDiagnosis() {
        return initialDiagnosis;
    }

    public void setInitialDiagnosis(String initialDiagnosis) {
        this.initialDiagnosis = initialDiagnosis;
    }
}