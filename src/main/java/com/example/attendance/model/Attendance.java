package com.example.attendance.model;

// 특정 주차에 대한 정보
public class Attendance {
    private Long id;   // pk
    private String studentId;   // 학번
    private String semester;   // 학기 (예: "2026-1")
    private int week;   // 주차
    private int workoutCount;   // 오운완 인증 횟수
    private boolean attendance;   // 정모 참석 여부
    private int fine;   // 벌금

    // 기본 생성자
    public Attendance(){

    }

    // 매개변수 생성자 (데이터 주입)
    public Attendance(Long id, String studentId, String semester, int week, int workoutCount, boolean attendance) {
        this.id = id;
        this.studentId = studentId;
        this.semester = semester;
        this.week = week;
        this.workoutCount = workoutCount;
        this.attendance = attendance;
        this.fine = 0;
    }

    // 횟수 증가용
    public void incrementWorkoutCount(){
        this.workoutCount++;
    }

    // Getter, Setter

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getStudentId() {
        return studentId;
    }
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getSemester() {
        return semester;
    }
    public void setSemester(String semester) {
        this.semester = semester;
    }

    public int getWeek() {
        return week;
    }
    public void setWeek(int week) {
        this.week = week;
    }

    public int getWorkoutCount() {
        return workoutCount;
    }
    public void setWorkoutCount(int workoutCount) {
        this.workoutCount = workoutCount;
    }

    public boolean isAttendance() {
        return attendance;
    }
    public void setAttendance(boolean attendance) {
        this.attendance = attendance;
    }

    public int getFine() {
        return fine;
    }
    public void setFine(int fine) {
        this.fine = fine;
    }
}
