package com.example.attendance.repository;

import com.example.attendance.model.Attendance;
import com.example.attendance.util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DB(3차)에 활동 기록, 벌금 등의 정보를 저장하는 클래스
 */
public class AttendanceDbRepository implements AttendanceRepository {

    // signleton 방식
    private Connection getConnection() {
        return DBConnection.getInstance().getConnection();
    }

    // 기록 저장
    @Override
    public void save(Attendance attendance) {
        String sql = "INSERT INTO attendance (student_id, semester, week, workout_count, attendance, fine) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, attendance.getStudentId());
            pstmt.setString(2, attendance.getSemester());
            pstmt.setInt(3, attendance.getWeek());
            pstmt.setInt(4, attendance.getWorkoutCount());
            pstmt.setBoolean(5, attendance.isAttendance());
            pstmt.setInt(6, attendance.getFine());

            // 변경
            pstmt.executeUpdate();

            // 증가된 id 값 확인
            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                // 증가 된 값이 있으면, attendance에도 id 추가
                if (keys.next()) {
                    attendance.setId(keys.getLong(1));   // 첫 번째 칸에 있는 값을 Long으로 읽어옴
                }
            }

            saveWorkoutDates(attendance);
        } catch (SQLException e) {
            throw new RuntimeException("출석 기록 저장 실패: " + e.getMessage(), e);
        }
    }

    // 학번 -> 해당 학기/주차 기록 읽기
    @Override
    public Optional<Attendance> findByStudentIdAndWeek(String studentId, String semester, int week) {
        String sql = "SELECT * FROM attendance WHERE student_id = ? AND semester = ? AND week = ?";

        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setString(1, studentId);
            pstmt.setString(2, semester);
            pstmt.setInt(3, week);

            // resultset으로 받아옴
            try (ResultSet rs = pstmt.executeQuery()) {
                // 결과 행이 존재하면
                if (rs.next()) {
                    return Optional.of(mapRowWithDates(rs));   // 자바 객체로 변형해서 들고옴
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("출석 기록 조회 실패: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Attendance> findAll() {
        List<Attendance> result = new ArrayList<>();
        String sql = "SELECT * FROM attendance";

        try (PreparedStatement pstmt = getConnection().prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                result.add(mapRowWithDates(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("출석 기록 전체 조회 실패: " + e.getMessage(), e);
        }
        return result;
    }

    // 학번 -> 그 학기 운동 정보
    @Override
    public List<Attendance> findByStudentIdAndSemester(String studentId, String semester) {
        List<Attendance> result = new ArrayList<>();
        String sql = "SELECT * FROM attendance WHERE student_id = ? AND semester = ?";

        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setString(1, studentId);
            pstmt.setString(2, semester);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRowWithDates(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("출석 기록 조회 실패: " + e.getMessage(), e);
        }
        return result;
    }

    // 학기 -> 해당 주차 운동 정보
    @Override
    public List<Attendance> findByWeek(String semester, int week) {
        List<Attendance> result = new ArrayList<>();
        String sql = "SELECT * FROM attendance WHERE semester = ? AND week = ?";

        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setString(1, semester);
            pstmt.setInt(2, week);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRowWithDates(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("주차별 출석 기록 조회 실패: " + e.getMessage(), e);
        }
        return result;
    }

    // 정보 수정
    @Override
    public void update(Attendance attendance) {
        String sql = "UPDATE attendance SET workout_count = ?, attendance = ?, fine = ? WHERE id = ?";

        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, attendance.getWorkoutCount());
            pstmt.setBoolean(2, attendance.isAttendance());
            pstmt.setInt(3, attendance.getFine());
            pstmt.setLong(4, attendance.getId());

            pstmt.executeUpdate();

            // 날짜 지우고 덮어쓰기
            deleteWorkoutDates(attendance.getId());
            saveWorkoutDates(attendance);
        } catch (SQLException e) {
            throw new RuntimeException("출석 기록 수정 실패: " + e.getMessage(), e);
        }
    }

    // 정보 삭제
    @Override
    public void delete(Long id) {
        // attendance 삭제 -> workout_dates 도 함께 삭제
        String sql = "DELETE FROM attendance WHERE id = ?";

        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setLong(1, id);   // ? 에 id 값 삽입
            pstmt.executeUpdate();   // 실행 -> 삭제
        } catch (SQLException e) {
            throw new RuntimeException("출석 기록 삭제 실패: " + e.getMessage(), e);
        }
    }

    // Attendance 자바 객체로 변환
    private Attendance mapRowWithDates(ResultSet rs) throws SQLException {
        Attendance attendance = new Attendance(rs.getLong("id"), rs.getString("student_id"), rs.getString("semester"), rs.getInt("week"), rs.getInt("workout_count"), rs.getBoolean("attendance"));
        attendance.setFine(rs.getInt("fine"));   // 벌금 설정
        attendance.setWorkoutDates(loadWorkoutDates(attendance.getId()));   // 운동날짜까지 설정
        return attendance;
    }

    // ----- 날짜 정보 load 및 수정을 위한 메서드 -----
    // workout_dates 테이블에서 운동날짜 리스트로 받아옴
    private List<LocalDate> loadWorkoutDates(Long attendanceId) throws SQLException {
        List<LocalDate> dates = new ArrayList<>();
        String sql = "SELECT workout_date FROM workout_dates WHERE attendance_id = ? ORDER BY workout_date";

        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setLong(1, attendanceId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // sql의 Date -> java의 localDate 타입으로 변경
                    dates.add(rs.getDate("workout_date").toLocalDate());
                }
            }
        }
        return dates;
    }

    // attendance에 있는 날짜 정보들 -> workout_dates에 insert
    private void saveWorkoutDates(Attendance attendance) throws SQLException {
        // 비어 있는 경우
        if (attendance.getWorkoutDates().isEmpty()) {
            return;
        }

        String sql = "INSERT INTO workout_dates (attendance_id, workout_date) VALUES (?, ?)";

        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            for (LocalDate date : attendance.getWorkoutDates()) {
                pstmt.setLong(1, attendance.getId());
                pstmt.setDate(2, Date.valueOf(date));   // sql.Date로 변경
                pstmt.addBatch();   // Batch에 담기
            }
            pstmt.executeBatch();   // 다 담았으면 이때 저장
        }
    }

    // 데이터 삭제
    private void deleteWorkoutDates(Long attendanceId) throws SQLException {
        String sql = "DELETE FROM workout_dates WHERE attendance_id = ?";

        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setLong(1, attendanceId);
            pstmt.executeUpdate();
        }
    }
}
