package Model;

import java.util.Date;

/**
 * 号源实体类，映射 AppointmentSlot 表
 */
public class AppointmentSlot {
    private Long slotId; // 自增主键
    private Date visitDate; // 出诊日期
    private String timeSlot; // 出诊时段
    private Integer totalNum; // 总号源数
    private Integer remainingNum; // 剩余号源数（实时更新）
    private Long doctorId; // 外键，关联医生表

    // 无参构造函数
    public AppointmentSlot() {}

    // 全参构造函数
    public AppointmentSlot(Long slotId, Date visitDate, String timeSlot, Integer totalNum, Integer remainingNum, Long doctorId) {
        this.slotId = slotId;
        this.visitDate = visitDate;
        this.timeSlot = timeSlot;
        this.totalNum = totalNum;
        this.remainingNum = remainingNum;
        this.doctorId = doctorId;
    }

    // getter/setter 方法
    public Long getSlotId() {
        return slotId;
    }

    public void setSlotId(Long slotId) {
        this.slotId = slotId;
    }

    public Date getVisitDate() {
        return visitDate;
    }

    public void setVisitDate(Date visitDate) {
        this.visitDate = visitDate;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(String timeSlot) {
        this.timeSlot = timeSlot;
    }

    public Integer getTotalNum() {
        return totalNum;
    }

    public void setTotalNum(Integer totalNum) {
        this.totalNum = totalNum;
    }

    public Integer getRemainingNum() {
        return remainingNum;
    }

    public void setRemainingNum(Integer remainingNum) {
        this.remainingNum = remainingNum;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

}