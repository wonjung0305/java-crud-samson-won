package com.example.attendance.repository;

import com.example.attendance.model.Attendance;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * csv파일(2차)에 활동 기록, 벌금 등의 정보를 저장하는 클래스
 */
public class AttendanceFileRepository implements AttendanceRepository {
    private static final String FILE_PATH = "attendance.csv";   // 경로
    private final List<Attendance> store = new ArrayList<>();   // 임시로 저장할 공간
    private Long sequence = 0L;   // PK 발급용

    // 생성자
    public AttendanceFileRepository() {
        loadFromFile();   // 이전 기록 복원

        // sequence 초기화
        for (Attendance a : store) {

            // sequence를 최대값(가장 큰 id)으로 갱신
            if (a.getId() > sequence) {
                sequence = a.getId();
            }
        }
    }

    // 데이터 복원 -> store에 저장
    private void loadFromFile() {
        File file = new File(FILE_PATH);

        // 에러 방지(파일 없는 경우)
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;

            // 끝까지 읽기
            while ((line = br.readLine()) != null) {
                // 비어있는 경우 넘어가기
                if (line.trim().isEmpty()){
                    continue;
                }
                String[] data = line.split(",");

                // 순서: id, 학번, 학기, 주차, 운동횟수, 정모참여, 벌금, 인증날짜들(|로 구분)
                // 데이터 나눠 담기 및 문자->형태 변환
                Long id = Long.parseLong(data[0]);
                String studentId = data[1];
                String semester = data[2];
                int week = Integer.parseInt(data[3]);
                int workoutCount = Integer.parseInt(data[4]);
                boolean attendance = Boolean.parseBoolean(data[5]);
                int fine = Integer.parseInt(data[6]);

                List<LocalDate> workoutDates = new ArrayList<>();

                // data의 7번 째에 있는 날짜가 비어있지 않고, 있는 경우
                if (data.length > 7 && !data[7].isEmpty()) {
                    for (String dateStr : data[7].split("\\|")) {
                        workoutDates.add(LocalDate.parse(dateStr)); // LocalDate 객체로 변환하여 저장
                    }
                }

                // 객체 생성
                Attendance a = new Attendance(id, studentId, semester, week, workoutCount, attendance);
                a.setFine(fine);
                a.setWorkoutDates(workoutDates);

                store.add(a);
            }
        } catch (IOException e) {
            System.out.println("출석 파일 로드 실패: " + e.getMessage());
        }
    }

    // 현재 메모리에(store) 있는 리스트를 파일에 저장
    private void saveToFile() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH))) {
            // store에 있는 모든 정보 저장
            for (Attendance a : store) {
                bw.write(a.toCsvString());   // csv 형태로 변환
                bw.newLine();   // \n
            }
        } catch (IOException e) {
            System.out.println("출석 파일 저장 실패: " + e.getMessage());
        }
    }

    // id 등록
    @Override
    public void save(Attendance attendance) {
        // id가 없는 경우, 새로 만들기
        if (attendance.getId() == null) {
            attendance.setId(++sequence);
        }

        store.add(attendance);   // store에 추가
        saveToFile();   // 파일에 저장
    }

    // 학번 -> 해당 학기,주차 정보 검색
    @Override
    public Optional<Attendance> findByStudentIdAndWeek(String studentId, String semester, int week) {
        for (Attendance a : store) {
            if ((a.getStudentId().equals(studentId)) && (a.getSemester().equals(semester)) && (a.getWeek() == week)) {
                return Optional.of(a);
            }
        }
        return Optional.empty();   // 못 찾은 경우
    }

    // 전체 기록 -> 쓸 일 X
    @Override
    public List<Attendance> findAll() {
        return new ArrayList<>(store);
    }

    // 학번 -> 해당 학기 기록 출력
    @Override
    public List<Attendance> findByStudentIdAndSemester(String studentId, String semester) {
        List<Attendance> result = new ArrayList<>();
        for (Attendance a : store) {
            if (a.getStudentId().equals(studentId) && a.getSemester().equals(semester)) {
                result.add(a);
            }
        }
        return result;
    }

    // 학기 -> 주차 (그 주차 학생들 기록)
    @Override
    public List<Attendance> findByWeek(String semester, int week) {
        List<Attendance> result = new ArrayList<>();
        for (Attendance a : store) {
            if (a.getSemester().equals(semester) && a.getWeek() == week) {
                result.add(a);
            }
        }
        return result;
    }

    // 정보 수정
    @Override
    public void update(Attendance attendance) {
        for (int i = 0; i < store.size(); i++) {
            if (store.get(i).getId().equals(attendance.getId())) {
                store.set(i, attendance);
                return;
            }
        }
        saveToFile();
    }

    // 정보 삭제
    @Override
    public void delete(Long id) {
        store.removeIf(attendance -> attendance.getId().equals(id));
        saveToFile();   // 삭제 후 파일 업데이트
    }

    // 파일에 저장(프로그램 종료 시 사용)
    @Override
    public void saveAll() {
        saveToFile();
    }
}
