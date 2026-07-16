package com.example.attendance.model;

/**
 * 동아리원 정보를 관리하는 클래스
 */
public class Member {
    // 이름, 전화번호, 학번 (프로필 관리)
    private String studentId;   // 학번(pk)
    private String name;   // 이름
    private String phoneNumber;   // 전화번호
    private String department;   // 학부
    private int activeSemester;   // 활동 학기 수
    private String status; // 활동, 휴학

    // 기본 생성자
    public Member(){}

    // 매개변수 생성자
    public Member(String studentId, String name, String phoneNumber, String department, int activeSemester) {
        this.studentId = studentId;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.department = department;
        this.activeSemester = activeSemester;
        this.status = "활동";
    }

    // YB/OB 등록
    public boolean isOb(){
        return this.activeSemester > 3;
    }

    public String getStudentId() {
        return studentId;
    }
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getDepartment() {
        return department;
    }
    public void setDepartment(String department) {
        this.department = department;
    }

    public int getActiveSemester() {
        return activeSemester;
    }
    public void setActiveSemester(int activeSemester) {
        this.activeSemester = activeSemester;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    // CSV 한 줄로 변환
    public String toCsvString() {
        return String.join(",", studentId, name, phoneNumber, department, status, String.valueOf(activeSemester));
    }
}
