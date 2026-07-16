package com.example.attendance.repository;

import com.example.attendance.model.Attendance;

import java.util.List;
import java.util.Optional;

/**
 * 출석 데이터의 CRUD 규칙을 정의하는 인터페이스
 */
public interface AttendanceRepository {
    void save(Attendance attendance);   // Attendance에 저장

    // 학번으로 검색 -> 특정 학기/주차의 기록을 조회하는 메서드
    Optional<Attendance> findByStudentIdAndWeek(String studentId, String semester, int week);

    List<Attendance> findAll();   // 전체 기록 조회
    List<Attendance> findByStudentIdAndSemester(String studentId, String semester);   // 학번+학기로 조회
    List<Attendance> findByWeek(String semester, int week);   // 학기+주차로 조회

    void update(Attendance attendance);   // 기록 수정
    void delete(Long id);   // 기록 삭제

    // 파일,DB에서 종료할 때 수정된 내용 전체 저장할 때 사용
    default void saveAll() {}
}
