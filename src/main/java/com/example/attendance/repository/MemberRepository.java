package com.example.attendance.repository;

import com.example.attendance.model.Member;

import java.util.List;
import java.util.Optional;

public interface MemberRepository {
    void save(Member member);   // 부원 등록
    Optional<Member> findByStudentId(String studentId);   // 잘못된 검색 대비 Optional(null로 반환됨)
    Optional<Member> findByName(String name);
    List<Member> findAll();
    void deleteByStudentId(String studentId);
}
