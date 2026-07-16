package com.example.attendance.repository;

import com.example.attendance.model.Member;
import com.example.attendance.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MemberDbRepository implements MemberRepository {

    // signleton 방식
    private Connection getConnection() {
        return DBConnection.getInstance().getConnection();
    }

    // 부원 등록
    @Override
    public void save(Member member) {
        String sql = "INSERT INTO members (student_id, name, phone_number, department, active_semester, status) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setString(1, member.getStudentId());
            pstmt.setString(2, member.getName());
            pstmt.setString(3, member.getPhoneNumber());
            pstmt.setString(4, member.getDepartment());
            pstmt.setInt(5, member.getActiveSemester());
            pstmt.setString(6, member.getStatus());

            pstmt.executeUpdate();   // 저장(업데이트)
        } catch (SQLException e) {
            throw new RuntimeException("부원 저장 실패: " + e.getMessage(), e);
        }
    }

    // 학번 -> 부원 정보
    @Override
    public Optional<Member> findByStudentId(String studentId) {
        String sql = "SELECT * FROM members WHERE student_id = ?";

        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setString(1, studentId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("부원 조회 실패: " + e.getMessage(), e);
        }

        return Optional.empty();
    }

    // 이름 -> 부원 정보
    @Override
    public Optional<Member> findByName(String name) {
        String sql = "SELECT * FROM members WHERE name = ?";

        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setString(1, name);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("부원 조회 실패: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    // 부원 전체 정보
    @Override
    public List<Member> findAll() {
        List<Member> result = new ArrayList<>();
        String sql = "SELECT * FROM members";

        try (PreparedStatement pstmt = getConnection().prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            // 끝까지 검색
            while (rs.next()) {
                result.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("부원 전체 조회 실패: " + e.getMessage(), e);
        }
        return result;
    }

    // 정보 수정
    @Override
    public void update(Member member) {
        String sql = "UPDATE members SET name = ?, phone_number = ?, department = ?, active_semester = ?, status = ? WHERE student_id = ?";

        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setString(1, member.getName());
            pstmt.setString(2, member.getPhoneNumber());
            pstmt.setString(3, member.getDepartment());
            pstmt.setInt(4, member.getActiveSemester());
            pstmt.setString(5, member.getStatus());
            pstmt.setString(6, member.getStudentId());

            pstmt.executeUpdate();   // 업데이트
        } catch (SQLException e) {
            throw new RuntimeException("부원 수정 실패: " + e.getMessage(), e);
        }
    }

    // 학번 -> 삭제
    @Override
    public void deleteByStudentId(String studentId) {
        String sql = "DELETE FROM members WHERE student_id = ?";

        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setString(1, studentId);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("부원 삭제 실패: " + e.getMessage(), e);
        }
    }

    // Member 객체로 변환
    private Member mapRow(ResultSet rs) throws SQLException {
        // Member 객체 등록
        Member member = new Member(
                rs.getString("student_id"),
                rs.getString("name"),
                rs.getString("phone_number"),
                rs.getString("department"),
                rs.getInt("active_semester")
        );
        member.setStatus(rs.getString("status"));   // 활동 상태 세팅
        return member;
    }
}
