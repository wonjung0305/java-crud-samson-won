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
}
