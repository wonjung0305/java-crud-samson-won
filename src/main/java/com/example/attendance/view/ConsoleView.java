package com.example.attendance.view;

import com.example.attendance.model.Attendance;
import com.example.attendance.model.Member;
import com.example.attendance.service.AttendanceService;
import com.example.attendance.util.ExcelExporter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.Optional;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class ConsoleView {
    private final AttendanceService service;
    private final Scanner scanner;

    public ConsoleView(AttendanceService service){
        this.service = service;
        this.scanner = new Scanner(System.in);
    }

    public void start(){
        while(true){
            System.out.println("\n========== 동아리 관리 시스템 (" + service.getCurrentSemester() + ") ==========");
            System.out.println("[부원 관리]");
            System.out.println(" 1. 신규 부원 등록");
            System.out.println(" 2. 전체 부원 명단 조회");
            System.out.println(" 3. 이번 학기 활동 인원 조회");
            System.out.println(" 4. 부원 관리 (휴학/복학)");
            System.out.println(" 5. 부원 정보 수정 (이름/전화번호/학부)");
            System.out.println(" 6. 부원 제명");
            System.out.println();

            System.out.println("[활동 관리]");
            System.out.println(" 7. 오운완 등록");
            System.out.println(" 8. 정모 출석 등록");
            System.out.println(" 9. 활동 기록 삭제");
            System.out.println();

            System.out.println("[학기 관리]");
            System.out.println(" 10. 학기 마감 (다음 학기로 전환)");
            System.out.println();

            System.out.println("[조회 및 데이터 활용]");
            System.out.println(" 11. 인원 검색 (학번 또는 이름)");
            System.out.println(" 12. 주차별 조회");
            System.out.println(" 13. 주차별 엑셀 내보내기");
            System.out.println(" 0. 프로그램 종료");

            System.out.print("▶ 메뉴를 선택해주세요: ");

            int choice = readInt();

            switch (choice){
                case 1: registerMemberMenu(); break;
                case 2: showAllMembersMenu(); break;
                case 3: showActiveMembersMenu(); break;
                case 4: modifyMemberStatusMenu(); break;
                case 5: editMemberInfoMenu(); break;
                case 6: removeMemberMenu(); break;
                case 7: recordWorkoutMenu(); break;
                case 8: recordMeetingMenu(); break;
                case 9: deleteAttendanceMenu(); break;
                case 10: closeSemesterMenu(); break;
                case 11: searchMemberMenu(); break;
                case 12: showSortedAttendanceMenu(); break;
                case 13: exportWeeklyExcelMenu(); break;
                case 0:
                    service.saveAll();
                    System.out.println("변경 사항을 저장 및 프로그램을 종료합니다.");
                    return;
                default:
                    System.out.println("존재하지 않는 메뉴입니다. 다시 입력해주세요.");
            }
        }
    }

    // 1번. 신규 부원 등록
    private void registerMemberMenu() {
        System.out.println("\n----- 신규 부원 등록 -----");

        System.out.print("학번: ");
        String studentId = scanner.nextLine();

        System.out.print("이름: ");
        String name = scanner.nextLine();

        System.out.print("전화번호: ");
        String phone = scanner.nextLine();

        System.out.print("학부: ");
        String dept = scanner.nextLine();

        int semesters = 1;

        Member member = new Member(studentId, name, phone, dept, semesters);
        boolean success = service.registerMember(member);
        if (success) {
            System.out.println(name + "(YB)(이/가) 등록되었습니다.");
        } else {
            System.out.println("이미 등록된 학번입니다: " + studentId);
        }
    }

    private static final String ROW_FORMAT_ALL    = "%-6s | %-10s | %-8s | %-13s | %-30s | %-8s | %-6s";
    private static final String ROW_FORMAT_ACTIVE = "%-6s | %-10s | %-8s | %-13s | %-30s | %-6s";

    // 2번. 전체 부원 명단 조회
    private void showAllMembersMenu(){
        List<Member> members = service.getAllMembers();

        if (members.isEmpty()) {
            System.out.println("등록된 부원이 없습니다.");
            return;
        }

        String separator = "-".repeat(String.format(ROW_FORMAT_ALL, "", "", "", "", "", "", "").length());

        System.out.println("총 " + members.size() + "명");
        System.out.println(separator);
        System.out.println(String.format(ROW_FORMAT_ALL, "번호", "학번", "이름", "전화번호", "학부", "상태", "구분"));
        System.out.println(separator);

        for (int i = 0; i < members.size(); i++) {
            Member m = members.get(i);
            String gubun = m.isOb() ? "OB" : "YB";
            System.out.println(String.format(ROW_FORMAT_ALL, "[" + (i + 1) + "]", m.getStudentId(), m.getName(),
                    m.getPhoneNumber(), m.getDepartment(), m.getStatus(), gubun));
        }
        System.out.println(separator);
    }

    // 3번. 이번 학기 활동 인원 조회
    private void showActiveMembersMenu(){
        List<Member> members = service.getActiveMembers();

        if (members.isEmpty()) {
            System.out.println("이번 학기(" + service.getCurrentSemester() + ") 활동 인원이 없습니다.");
            return;
        }

        String separator = "-".repeat(String.format(ROW_FORMAT_ACTIVE, "", "", "", "", "", "").length());

        System.out.println("\n----- [" + service.getCurrentSemester() + "] 이번 학기 활동 인원 -----");
        System.out.println("총 " + members.size() + "명");
        System.out.println(separator);
        System.out.println(String.format(ROW_FORMAT_ACTIVE, "번호", "학번", "이름", "전화번호", "학부", "구분"));
        System.out.println(separator);

        for (int i = 0; i < members.size(); i++) {
            Member m = members.get(i);
            String gubun = m.isOb() ? "OB" : "YB";
            System.out.println(String.format(ROW_FORMAT_ACTIVE, "[" + (i + 1) + "]", m.getStudentId(), m.getName(),
                    m.getPhoneNumber(), m.getDepartment(), gubun));
        }
        System.out.println(separator);
    }


    // 4번. 휴학 관리
    private void modifyMemberStatusMenu() {
        System.out.println("\n----- 부원 상태 변경 (휴학/복학) -----");

        System.out.print("상태를 변경할 부원의 학번 입력: ");
        String studentId = scanner.nextLine().trim();

        // 존재하지 않는 학번일 경우
        if (service.searchMemberByStudentId(studentId).isEmpty()) {
            System.out.println("존재하지 않는 학번입니다. 학번을 다시 확인해 주세요.");
            return;
        }

        System.out.println("변경할 상태를 선택해 주세요.");
        System.out.println("1. 휴학 (활동 -> 휴학 상태로 변경)");
        System.out.println("2. 복학 (휴학 -> 활동 상태로 변경 및 학기 수 +1)");
        System.out.print("번호를 입력해주세요: ");
        int type = readInt();

        if (type == 1) {
            // 휴학 처리 요청
            service.updateMemberStatus(studentId, "휴학");
            System.out.println("휴학 처리 되었습니다.(학기 수 유지)");
        } else if (type == 2) {
            // 복학 처리 요청
            service.updateMemberStatus(studentId, "활동");
            System.out.println("복학 처리되었습니다.");
        } else {
            System.out.println("잘못된 번호입니다.");
        }
    }

    // 5번. 부원 정보 수정 (이름/전화번호/학부, 학번은 PK라 여기서 수정 불가)
    private void editMemberInfoMenu() {
        System.out.println("\n----- 부원 정보 수정 -----");
        System.out.print("수정할 부원의 학번 입력: ");
        String studentId = scanner.nextLine().trim();

        Optional<Member> memberOpt = service.searchMemberByStudentId(studentId);
        if (memberOpt.isEmpty()) {
            System.out.println("존재하지 않는 학번입니다.");
            return;
        }

        Member m = memberOpt.get();
        System.out.println("바꿀 값만 입력하세요. (그대로 두려면 엔터)");

        System.out.print("이름 [" + m.getName() + "]: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) name = m.getName();

        System.out.print("전화번호 [" + m.getPhoneNumber() + "]: ");
        String phone = scanner.nextLine().trim();
        if (phone.isEmpty()) phone = m.getPhoneNumber();

        System.out.print("학부 [" + m.getDepartment() + "]: ");
        String dept = scanner.nextLine().trim();
        if (dept.isEmpty()) dept = m.getDepartment();

        service.updateMemberInfo(studentId, name, phone, dept);
        System.out.println("부원 정보가 수정되었습니다.");
    }

    // 6번. 부원 제명
    private void removeMemberMenu() {
        System.out.println("\n----- 부원 제명 -----");
        System.out.print("제명할 부원의 학번 입력: ");
        String studentId = scanner.nextLine().trim();

        if (service.searchMemberByStudentId(studentId).isPresent()) {
            service.removeMember(studentId);
            System.out.println("해당 부원이 제명되었습니다.");
        } else {
            System.out.println("존재하지 않는 학번입니다.");
        }
    }

    // 7번. 오운완 등록
    private void recordWorkoutMenu() {
        System.out.print("인증을 진행할 주차 입력(1-16): ");

        int week = readWeek();
        System.out.println("--- [" + week + "주차] 오운완 인증 기록 (종료하려면 q 입력) ---");

        while (true) {
            System.out.print("운동한 부원 이름: ");
            String name = scanner.nextLine().trim();

            // 종료
            if (name.equalsIgnoreCase("q")) {
                break;
            }

            // 이름이 비어있는 경우
            if (name.isEmpty()) {
                System.out.println("이름을 입력해 주세요.");
                continue;
            }

            //
            boolean success = service.recordWorkoutByName(name, week);
            if (success) {
                System.out.println("▶ " + name + ", " + week + "주차 오운완 횟수 +1");
            } else {
                System.out.println("존재하지 않는 이름입니다. 다시 확인해 주세요.");
            }
        }
    }

    // 8번. 정모 출석 등록
    private void recordMeetingMenu() {
        System.out.print("정모 주차 입력(1-16): ");

        int week = readWeek();
        System.out.println("--- [" + week + "주차] 정모 출석부 체크 (종료하려면 q 입력) ---");

        while (true) {
            System.out.print("출석한 부원 이름: ");

            String name = scanner.nextLine().trim();

            if (name.equalsIgnoreCase("q")) {
                break;
            }

            // 이름이 비어있는 경우
            if (name.isEmpty()) {
                System.out.println("이름을 입력해 주세요.");
                continue;
            }

            boolean success = service.recordMeetingAttendanceByName(name, week);
            if (success) {
                System.out.println("▶ " + name + ", " + week + "주차 정모 참석.");
            } else {
                System.out.println("존재하지 않는 이름입니다.");
            }
        }
    }

    // 9번. 활동 기록 삭제
    private void deleteAttendanceMenu() {
        System.out.println("\n----- 활동 기록 삭제 -----");

        System.out.print("삭제할 기록의 주차 입력(1-16): ");
        int week = readWeek();

        System.out.print("삭제할 부원 이름: ");
        String name = scanner.nextLine().trim();

        boolean success = service.deleteAttendanceRecord(name, week);
        if (success) {
            System.out.println("▶ " + name + "의 " + week + "주차의 전체 기록이 삭제되었습니다.");
        } else {
            System.out.println("해당 이름 또는 주차의 기록을 찾을 수 없습니다.");
        }
    }

    // 10번. 학기 마감 (다음 학기로 전환)
    private void closeSemesterMenu() {
        System.out.println("\n----- 학기 마감: " + service.getCurrentSemester() + " -----");

        List<Member> activeMembers = service.getActiveMembers();
        if (activeMembers.isEmpty()) {
            System.out.println("이번 학기 활동 인원이 없습니다.");
        } else {
            System.out.println("[현재 활동 인원]");
            for (Member m : activeMembers) {
                System.out.printf(" - %s (%s)\n", m.getName(), m.getStudentId());
            }
        }

        System.out.println("\n다음 학기에 계속하지 않는 인원의 학번을 한 명씩 입력해 주세요. (없으면 바로 q 입력)");

        List<String> leavingStudentIds = new ArrayList<>();
        while (true) {
            System.out.print("학번: ");
            String studentId = scanner.nextLine().trim();

            if (studentId.equalsIgnoreCase("q")) {
                break;
            }
            if (studentId.isEmpty()) {
                System.out.println("학번을 입력해 주세요.");
                continue;
            }
            if (service.searchMemberByStudentId(studentId).isEmpty()) {
                System.out.println("존재하지 않는 학번입니다. 다시 확인해 주세요.");
                continue;
            }

            leavingStudentIds.add(studentId);
        }

        System.out.print("다음 학기를 입력해 주세요 (예: 2026-2): ");
        String nextSemester = scanner.nextLine().trim();

        if (nextSemester.isEmpty()) {
            System.out.println("학기 입력이 없어 마감을 취소합니다.");
            return;
        }

        service.closeSemester(leavingStudentIds, nextSemester);
        System.out.println(leavingStudentIds.size() + "명이 휴학 처리되었고, 나머지 인원은 유지됩니다.");
        System.out.println("현재 학기가 " + nextSemester + "(으)로 전환되었습니다.");
    }

    // 11번. 인원 검색
    private void searchMemberMenu() {
        System.out.println("\n----- 인원 검색 -----");
        System.out.print("검색할 학번 또는 이름 입력: ");
        String keyword = scanner.nextLine().trim();

        // 학번으로 찾기
        Optional<Member> memberOpt = service.searchMemberByStudentId(keyword);

        // 학번 아닌 경우 이름인 경우
        if (memberOpt.isEmpty()) {
            memberOpt = service.searchMemberByName(keyword);
        }

        // 존재하지 않는 경우 바로 return
        if (memberOpt.isEmpty()) {
            System.out.println("존재하지 않는 학번 혹은 이름입니다. 학번이나 이름을 다시 확인해 주세요.");
            return;
        }

        Member m = memberOpt.get();
        String gubun = m.isOb() ? "OB" : "YB";
        System.out.printf("[검색 결과] 학번: %s | 이름: %s | 연락처: %s | 학부: %s | 상태: %s | 구분: %s\n", m.getStudentId(), m.getName(), m.getPhoneNumber(), m.getDepartment(), m.getStatus(), gubun);

        String semester = readSemesterOrDefault();
        AttendanceService.SemesterStats stats = service.getSemesterStats(m.getStudentId(), semester);

        // 활동 통계 출력
        System.out.printf("[활동 통계 - %s] 정모 결석: %d회 | 오운완 미달 주차: %d주 | 누적 벌금: %,d원\n", semester, stats.absenceCount(), stats.workoutFailCount(), stats.totalFine());
    }

    // 12번. 주차별 조회
    private void showSortedAttendanceMenu() {
        String semester = readSemesterOrDefault();

        System.out.print("조회할 주차 입력(1-16): ");
        int week = readWeek();
        List<Attendance> sorted = service.getSortedAttendance(semester, week);

        if (sorted.isEmpty()) {
            System.out.println("[" + semester + "] " + week + "주차에 등록된 활동 데이터가 없습니다.");
            return;
        }

        System.out.println("\n--- [" + semester + " / " + week + "주차] 종합 정렬 명단 ---");
        for (int i = 0; i < sorted.size(); i++) {
            Attendance a = sorted.get(i);

            // map(Member::getName) >> 회원이 있으면 getName 으로 들고오기
                // 회원이 없는 경우 "알 수 없음" 처리 -> 제명됐거나 잘못된 데이터
            String memberName = service.searchMemberByStudentId(a.getStudentId()).map(Member::getName).orElse("알 수 없음");

            int effectiveWorkout = a.getWorkoutCount() + (a.isAttendance() ? 1 : 0);
            String workoutDatesStr = formatWorkoutDates(a.getWorkoutDates());
            System.out.printf("[%d] 학번: %s | 이름: %s | 오운완: %d회(%d회) | 정모: %s | 이번 주 벌금: %,d원 | 인증일: %s\n", i + 1, a.getStudentId(), memberName, a.getWorkoutCount(), effectiveWorkout, a.isAttendance() ? "참석" : "불참", a.getFine(), workoutDatesStr);
        }
    }

    // 13번. 주차별 엑셀 내보내기 (학번, 이름, 오운완 인증 횟수, 정모 참여 여부, 벌금 / 운동 많이한 순 정렬)
    private void exportWeeklyExcelMenu() {
        String semester = readSemesterOrDefault();

        System.out.print("내보낼 주차 입력(1-16): ");
        int week = readWeek();

        List<Attendance> sorted = service.getSortedAttendance(semester, week);
        if (sorted.isEmpty()) {
            System.out.println("[" + semester + "] " + week + "주차에 내보낼 데이터가 없습니다.");
            return;
        }

        List<ExcelExporter.ExportRow> rows = new ArrayList<>();
        for (Attendance a : sorted) {
            String memberName = service.searchMemberByStudentId(a.getStudentId()).map(Member::getName).orElse("알 수 없음");
            String workoutDatesStr = formatWorkoutDates(a.getWorkoutDates());
            rows.add(new ExcelExporter.ExportRow(a.getStudentId(), memberName, a.getWorkoutCount(), a.isAttendance(), a.getFine(), workoutDatesStr));
        }

        String fileName = semester + "_" + week + "주차_출석부.xlsx";
        try {
            ExcelExporter.exportWeeklyAttendance(fileName, semester, week, rows);
            System.out.println("▶ 엑셀로 내보냈습니다: " + fileName);
        } catch (IOException e) {
            System.out.println("엑셀 내보내기 실패: " + e.getMessage());
        }
    }

    // 오운완 인증 날짜 목록
    private String formatWorkoutDates(List<LocalDate> dates) {
        // 오류로 인해 없는 경우
        if (dates.isEmpty()) return "없음";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd(E)", Locale.KOREAN);
        return dates.stream().map(formatter::format).collect(Collectors.joining(", "));
    }


    // 사용자 입력 (nextInt 대체)
    private int readInt(){
        while(true){
            try{
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e){
                System.out.print("숫자를 다시 입력해주세요: ");
            }
        }
    }

    // 조회할 학기 입력받기 (빈 입력 시 현재 학기)
    private String readSemesterOrDefault(){
        System.out.print("조회할 학기 입력 (ex. 2026-1, 현재 학기 [" + service.getCurrentSemester() + "]): ");
        String input = scanner.nextLine().trim();
        return input.isEmpty() ? service.getCurrentSemester() : input;
    }

    // 주차 읽을 때, 범위 오류 처리
    private int readWeek(){
        while(true){
            int week = readInt(); // 숫자 먼저 막고

            if ((week >= 1) && (week) <= 16){
                return week;
            }

            System.out.print("올바른 주차를 입력해주세요(1-16): ");
        }
    }

}
