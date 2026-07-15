package com.example.attendance.repository;

import com.example.attendance.model.Member;

import java.util.List;
import java.util.Optional;

public interface MemberRepository {
    void save(Member member);   // 부원 등록

    Optional<Member> findByStudentId(String studentId);
    Optional<Member> findByName(String name);

    List<Member> findAll();

    void update(Member member);   // 부원 정보 수정
    void deleteByStudentId(String studentId);

    // 프로그램 종료 시 저장소를 최종 flush하는 훅 (파일/DB 버전에서만 의미 있음)
    default void saveAll() {}
}
