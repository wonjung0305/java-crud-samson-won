package com.example.attendance.model;

public class Attendance {
    private Long id;   // pk
    private String studentId;   // 학번
    private String name;   // 이름
    private int week;   // 주차
    private int workoutCount;   // 오운완 인증 횟수
    private boolean attendance;   // 정모 참석 여부
    private int fine;   // 벌금

    // 기본 생성자
    public Attendance(){

    }

    // 매개변수 생성자 (데이터 주입)
    public Attendance(Long id, String studentId, String name, int week, int workoutCount, boolean attendance, int fine) {
        this.id = id;
        this.studentId = studentId;
        this.name = name;
        this.week = week;
        this.workoutCount = workoutCount;
        this.attendance = attendance;
        this.fine = 0;
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

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
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
