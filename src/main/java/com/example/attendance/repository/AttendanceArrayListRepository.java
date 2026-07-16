package com.example.attendance.repository;

import com.example.attendance.model.Attendance;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * ArrayList(1차)에 활동 기록, 벌금 등의 정보를 저장하는 클래스
 */
public class AttendanceArrayListRepository implements AttendanceRepository {

    private final List<Attendance> store = new ArrayList<>();
    private Long sequence = 0L;   // PK 발급용

    // 기록 저장
    @Override
    public void save(Attendance attendance) {
        // id가 없는 경우, 새로 만들기
        if(attendance.getId() == null){
            attendance.setId(++sequence);
        }

        store.add(attendance);
    }

    // 학번 -> 학기/주차로 기록 검색
    @Override
    public Optional<Attendance> findByStudentIdAndWeek(String studentId, String semester, int week){
        for(Attendance a : store){
            // 찾았을 때 반환
            if((a.getStudentId().equals(studentId)) && (a.getSemester().equals(semester)) && (a.getWeek() == week)){
                return Optional.of(a);
            }
        }

        return Optional.empty();   // 없는 경우
    }

    // 전체 기록 반환
    @Override
    public List<Attendance> findAll() {
        return new ArrayList<>(store);   // 복사하여 반환
    }

    // 학번 -> 특정 학기 기록 반환
    @Override
    public List<Attendance> findByStudentIdAndSemester(String studentId, String semester) {
        List<Attendance> result = new ArrayList<>();

        for (Attendance a : store){
            if((a.getStudentId().equals(studentId)) && (a.getSemester().equals(semester))){
                result.add(a);
            }
        }
        return result;
    }

    // 특정 학기, 특정 주차 기록 반환
    @Override
    public List<Attendance> findByWeek(String semester, int week) {
        List<Attendance> result = new ArrayList<>();

        for (Attendance a : store){
            if((a.getSemester().equals(semester)) && (a.getWeek() == week)){
                result.add(a);
            }
        }
        return result;
    }

    // 정보 수정
    @Override
    public void update(Attendance attendance) {
        for(int i = 0; i < store.size(); i++){
            if(store.get(i).getId().equals(attendance.getId())){    // ID가 일치하는 데이터를 찾아
                store.set(i, attendance);   // 새 데이터로 교체
                return;   // 종료
            }
        }
    }

    // 정보 삭제
    @Override
    public void delete(Long id) {
        store.removeIf(attendance -> attendance.getId().equals(id)); // 임의의 변수 == id인 경우
    }
}
