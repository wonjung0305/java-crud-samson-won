package com.example.attendance;

import com.example.attendance.repository.AttendanceRepository;
import com.example.attendance.repository.MemberRepository;

// import com.example.attendance.repository.AttendanceArrayListRepository;
// import com.example.attendance.repository.MemberArrayListRepository;

// import com.example.attendance.repository.AttendanceFileRepository;
// import com.example.attendance.repository.MemberFileRepository;

import com.example.attendance.repository.AttendanceDbRepository;
import com.example.attendance.repository.MemberDbRepository;
import com.example.attendance.service.AttendanceService;
import com.example.attendance.view.ConsoleView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Main {
    private static final String SEMESTER_FILE = "semester.txt";
    private static final String DEFAULT_SEMESTER = "2026-1";

    public static void main(String[] args){
        // MemberRepository memberRepository = new MemberArrayListRepository();   // 메모리 버전
        // MemberRepository memberRepository = new MemberFileRepository();       // 파일 버전
        MemberRepository memberRepository = new MemberDbRepository();            // DB 버전

        // AttendanceRepository attendanceRepository = new AttendanceArrayListRepository();   // 메모리 버전
        // AttendanceRepository attendanceRepository = new AttendanceFileRepository();        // 파일 버전
        AttendanceRepository attendanceRepository = new AttendanceDbRepository();            // DB 버전

        String initialSemester = loadCurrentSemester();   // 마지막으로 쓰던 학기 이어받기
        AttendanceService service = new AttendanceService(attendanceRepository, memberRepository, initialSemester);

        ConsoleView view = new ConsoleView(service);
        view.start();

        saveCurrentSemester(service.getCurrentSemester());   // 종료 시점의 학기 저장
    }

    // 마지막 학기 읽어오기
    private static String loadCurrentSemester() {
        File file = new File(SEMESTER_FILE);

        // 텍스트 파일에 학기 안써져있으면, 기본으로
        if (!file.exists()) return DEFAULT_SEMESTER;

        // 파일 읽어서 있으면 해당 학기 반환
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();
            return (line == null || line.trim().isEmpty()) ? DEFAULT_SEMESTER : line.trim();
        } catch (IOException e) {
            System.out.println("학기 정보 로드 실패: " + e.getMessage());
            return DEFAULT_SEMESTER;
        }
    }

    // 현재 학기를 semester.txt에 저장
    private static void saveCurrentSemester(String semester) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(SEMESTER_FILE))) {
            bw.write(semester);
        } catch (IOException e) {
            System.out.println("학기 정보 저장 실패: " + e.getMessage());
        }
    }
}
