package com.example.attendance.repository;

import com.example.attendance.model.Attendance;

import java.util.List;

public interface AttendanceRepository {
    void save(Attendance attendance);   // 등록

    List<Attendance> findAll();   // 전체 기록 조회
    List<Attendance> findByStudentId(String studentId);   // 학번으로 조회
    List<Attendance> findByWeek(int week);   // 주차로 조회
    List<Attendance> findByName();   // 이름으로 조회

    void update(Attendance attendance);
    void delete(Long id);   // 기록 삭제
}
