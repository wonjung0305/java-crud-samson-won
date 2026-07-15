package com.example.attendance.repository;

import com.example.attendance.model.Attendance;

import java.util.List;
import java.util.Optional;

public interface AttendanceRepository {
    void save(Attendance attendance);   // Attendance에 저장

    // Optional -> 데이터가 있을 수도, 없을 수도(null, 이때 오류를 발생시키지 않음)
        // 잘못된 검색 대비 Optional(null로 반환됨)
        // 데이터를 보낼 때 존재하면 Optional.of(값), 없으면 Optional.empty()를 사용
    Optional<Attendance> findByStudentIdAndWeek(String studentId, String semester, int week);

    // List -> 결과가 몇 개일지 모름(많을 수도, 없을 수도 [] 반환)
    List<Attendance> findAll();   // 전체 기록 조회
    List<Attendance> findByStudentIdAndSemester(String studentId, String semester);   // 학번+학기로 조회
    List<Attendance> findByWeek(String semester, int week);   // 학기+주차로 조회

    void update(Attendance attendance);
    void delete(Long id);   // 기록 삭제

    // 프로그램 종료 시 저장소를 최종 flush하는 훅 (파일/DB 버전에서만 의미 있음)
    default void saveAll() {}
}
