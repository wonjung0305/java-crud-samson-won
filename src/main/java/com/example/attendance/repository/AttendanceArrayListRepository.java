package com.example.attendance.repository;

import com.example.attendance.model.Attendance;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AttendanceArrayListRepository implements AttendanceRepository {

    private final List<Attendance> store = new ArrayList<>();
    private Long sequence = 0L;   // PK 발급용

    @Override
    public void save(Attendance attendance) {
        // id가 없는 경우, 새로 만들기
        if(attendance.getId() == null){
            attendance.setId(++sequence);
        }

        store.add(attendance);
    }

    @Override
    public Optional<Attendance> findByStudentIdAndWeek(String studentId, String semester, int week){
        for(Attendance a : store){
            // 찾았을 때
            if((a.getStudentId().equals(studentId)) && (a.getSemester().equals(semester)) && (a.getWeek() == week)){
                return Optional.of(a);
            }
        }

        return Optional.empty();   // 없는 경우
    }

    @Override
    public List<Attendance> findAll() {
        return new ArrayList<>(store);   // 복사하여 반환
    }

    @Override
    public List<Attendance> findByStudentIdAndSemester(String studentId, String semester) {
        List<Attendance> result = new ArrayList<>();

        for (Attendance a : store){
            if(a.getStudentId().equals(studentId) && a.getSemester().equals(semester)){
                result.add(a);
            }
        }
        return result;
    }

    @Override
    public List<Attendance> findByWeek(String semester, int week) {
        List<Attendance> result = new ArrayList<>();

        for (Attendance a : store){
            if(a.getSemester().equals(semester) && a.getWeek() == week){
                result.add(a);
            }
        }
        return result;
    }

    @Override
    public void update(Attendance attendance) {
        for(int i = 0; i < store.size(); i++){
            if(store.get(i).getId().equals(attendance.getId())){    // ID가 일치하는 데이터를 찾아
                store.set(i, attendance);   // 새 데이터로 교체
            }
        }
    }

    @Override
    public void delete(Long id) {
        store.removeIf(attendance -> attendance.getId().equals(id)); // 임의의 변수 == id인 경우
    }
}
