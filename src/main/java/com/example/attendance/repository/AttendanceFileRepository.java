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

public class AttendanceFileRepository implements AttendanceRepository {
    private static final String FILE_PATH = "attendance.csv";
    private final List<Attendance> store = new ArrayList<>();

    private Long sequence = 0L;   // PK 발급용

    public AttendanceFileRepository() {
        loadFromFile();   // 이전 기록 복원

        // sequence 초기화
        for (Attendance a : store) {

            // sequence를 최대값(가장 큰 id)으로 갱신하는 것
            if (a.getId() > sequence) {
                sequence = a.getId();
            }
        }
    }

    // 데이터 담아오기
    private void loadFromFile() {
        File file = new File(FILE_PATH);

        // 에러 방지(파일 없는 경우)
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;

            // 끝까지 읽기
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;   // 비어있는 경우 넘어가기
                String[] data = line.split(",");

                // 순서: id, 학번, 학기, 주차, 운동횟수, 정모참여, 벌금, 인증날짜들(|로 구분)
                Long id = Long.parseLong(data[0]);
                String studentId = data[1];
                String semester = data[2];
                int week = Integer.parseInt(data[3]);
                int workoutCount = Integer.parseInt(data[4]);
                boolean attendance = Boolean.parseBoolean(data[5]);
                int fine = Integer.parseInt(data[6]);

                // 날짜 컬럼이 없는(예전 형식) 파일도 깨지지 않도록 방어
                List<LocalDate> workoutDates = new ArrayList<>();
                if (data.length > 7 && !data[7].isEmpty()) {
                    for (String dateStr : data[7].split("\\|")) {
                        workoutDates.add(LocalDate.parse(dateStr));
                    }
                }

                Attendance a = new Attendance(id, studentId, semester, week, workoutCount, attendance);
                a.setFine(fine);
                a.setWorkoutDates(workoutDates);

                store.add(a);
            }
        } catch (IOException e) {
            System.out.println("출석 파일 로드 실패: " + e.getMessage());
        }
    }

    // 현재 메모리에 있는 리스트를 파일에 저장
    private void saveToFile() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (Attendance a : store) {
                bw.write(a.toCsvString());
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("출석 파일 저장 실패: " + e.getMessage());
        }
    }

    @Override
    public void save(Attendance attendance) {
        // id가 없는 경우, 새로 만들기
        if (attendance.getId() == null) {
            attendance.setId(++sequence);
        }

        store.add(attendance);
        saveToFile();
    }

    @Override
    public Optional<Attendance> findByStudentIdAndWeek(String studentId, String semester, int week) {
        for (Attendance a : store) {
            if ((a.getStudentId().equals(studentId)) && (a.getSemester().equals(semester)) && (a.getWeek() == week)) {
                return Optional.of(a);
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Attendance> findAll() {
        return new ArrayList<>(store);
    }

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

    @Override
    public void update(Attendance attendance) {
        for (int i = 0; i < store.size(); i++) {
            if (store.get(i).getId().equals(attendance.getId())) {
                store.set(i, attendance);
            }
        }
        saveToFile();
    }

    @Override
    public void delete(Long id) {
        store.removeIf(attendance -> attendance.getId().equals(id));
        saveToFile();
    }

    @Override
    public void saveAll() {
        saveToFile();
    }
}
