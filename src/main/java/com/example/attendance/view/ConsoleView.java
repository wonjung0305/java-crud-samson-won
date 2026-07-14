package com.example.attendance.view;

import com.example.attendance.model.Attendance;
import com.example.attendance.model.Member;
import com.example.attendance.service.AttendanceService;

import java.util.List;
import java.util.Scanner;
import java.util.Optional;
import java.util.ArrayList;

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
            System.out.println(" 5. 부원 제명");
            System.out.println();

            System.out.println("[활동 관리]");
            System.out.println(" 6. 오운완 등록");
            System.out.println(" 7. 정모 출석 등록");
            System.out.println(" 8. 활동 기록 삭제");
            System.out.println();

            System.out.println("[학기 관리]");
            System.out.println(" 9. 학기 마감 (다음 학기로 전환)");
            System.out.println();

            System.out.println("[조회 및 데이터 활용]");
            System.out.println(" 10. 인원 검색 (학번 또는 이름)");
            System.out.println(" 11. 주차별 조회");
            System.out.println(" 0. 프로그램 종료");

            System.out.print("▶ 메뉴를 선택해주세요: ");

            int choice = readInt();

            switch (choice){
                case 1: registerMemberMenu(); break;
                case 2: showAllMembersMenu(); break;
                case 3: showActiveMembersMenu(); break;
                case 4: modifyMemberStatusMenu(); break;
                case 5: removeMemberMenu(); break;
                case 6: recordWorkoutMenu(); break;
                case 7: recordMeetingMenu(); break;
                case 8: deleteAttendanceMenu(); break;
                case 9: closeSemesterMenu(); break;
                case 10: searchMemberMenu(); break;
                case 11: showSortedAttendanceMenu(); break;
                case 0:
                    System.out.println("프로그램을 종료합니다.");
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
        service.registerMember(member);
        System.out.println(name + "(YB)(이/가) 등록되었습니다.");
    }

    // 2번. 전체 부원 명단 조회
    private void showAllMembersMenu(){
        List<Member> members = service.getAllMembers();

        if (members.isEmpty()) {
            System.out.println("등록된 부원이 없습니다.");
            return;
        }

        System.out.println("------------------------------------------------------------");
        System.out.printf("%-10s | %-6s | %-13s | %-10s | %-6s | %-4s\n", "학번", "이름", "전화번호", "학부", "상태", "구분");
        System.out.println("------------------------------------------------------------");

        for (Member m : members) {
            String gubun = m.isOb() ? "OB" : "YB";
            System.out.printf("%-10s | %-6s | %-13s | %-10s | %-6s | %-4s\n", m.getStudentId(), m.getName(), m.getPhoneNumber(), m.getDepartment(), m.getStatus(), gubun);
        }
        System.out.println("------------------------------------------------------------");

    }

    // 3번. 이번 학기 활동 인원 조회
    private void showActiveMembersMenu(){
        List<Member> members = service.getActiveMembers();

        if (members.isEmpty()) {
            System.out.println("이번 학기(" + service.getCurrentSemester() + ") 활동 인원이 없습니다.");
            return;
        }

        System.out.println("\n----- [" + service.getCurrentSemester() + "] 이번 학기 활동 인원 -----");
        System.out.println("------------------------------------------------------------");
        System.out.printf("%-10s | %-6s | %-13s | %-10s | %-4s\n", "학번", "이름", "전화번호", "학부", "구분");
        System.out.println("------------------------------------------------------------");

        for (Member m : members) {
            String gubun = m.isOb() ? "OB" : "YB";
            System.out.printf("%-10s | %-6s | %-13s | %-10s | %-4s\n",
                    m.getStudentId(), m.getName(), m.getPhoneNumber(), m.getDepartment(), gubun);
        }
        System.out.println("------------------------------------------------------------");
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

    // 4번. 부원 제명
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

    // 6번. 오운완 등록
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

    // 7번. 정모 출석 등록
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

    // 8번. 활동 기록 삭제
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

    // 9번. 학기 마감 (다음 학기로 전환)
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

    // 10번. 인원 검색
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

        if (memberOpt.isPresent()) {
            Member m = memberOpt.get();
            String gubun = m.isOb() ? "OB" : "YB";
            System.out.printf("[검색 결과] 학번: %s | 이름: %s | 연락처: %s | 학부: %s | 상태: %s | 구분: %s\n", m.getStudentId(), m.getName(), m.getPhoneNumber(), m.getDepartment(), m.getStatus(), gubun);

            // 학번과 현재 학기 가져오기
            List<Attendance> history = service.getAttendanceRepository().findByStudentIdAndSemester(m.getStudentId(), service.getCurrentSemester());

            int workoutFailCount = 0;   // 오운완 미달 주차 수
            int absenceCount = 0;   // 정모 결석 횟수
            int totalFine = 0;   // 이번 학기 누적 벌금

            for(Attendance a : history){
                if((a.getWeek() != 8) && (a.getWeek() != 16)){
                    // 정모 불참 체크
                    if(!a.isAttendance()){
                        absenceCount++;
                    }

                    // 오운완 미달
                    int includeMeetingCount = a.getWorkoutCount() + (a.isAttendance() ? 1 : 0);
                    if (includeMeetingCount < 3) {
                        workoutFailCount++;
                    }
                }

                // 주차별 벌금 누적
                totalFine += a.getFine();

            }

            // 활동 통계 출력
            System.out.printf("[활동 통계] 이번 학기 정모 결석: %d회 | 오운완 미달 주차: %d주 | 현재 누적 벌금: %,d원\n", absenceCount, workoutFailCount, totalFine);

        } else {
            System.out.println("검색 결과가 없습니다.");
        }
    }

    // 11번. 주차별 조회
    private void showSortedAttendanceMenu() {
        System.out.print("조회할 주차 입력(1-16): ");
        int week = readWeek();
        List<com.example.attendance.model.Attendance> sorted = service.getSortedAttendance(week);

        if (sorted.isEmpty()) {
            System.out.println(week + "주차에 등록된 활동 데이터가 없습니다.");
            return;
        }

        System.out.println("\n--- [" + week + "주차] 종합 정렬 명단 ---");
        for (int i = 0; i < sorted.size(); i++) {
            com.example.attendance.model.Attendance a = sorted.get(i);

            // map(Member::getName) >> 회원이 있으면 getName 으로 들고오기
                // 회원이 없는 경우 "알 수 없음" 처리 -> 제명됐거나 잘못된 데이터
            String memberName = service.searchMemberByStudentId(a.getStudentId()).map(Member::getName).orElse("알 수 없음");

            int effectiveWorkout = a.getWorkoutCount() + (a.isAttendance() ? 1 : 0);
            System.out.printf("[%d] 학번: %s | 이름: %s | 오운완: %d회(%d회) | 정모: %s | 이번 주 벌금: %,d원 |\n", i + 1, a.getStudentId(), memberName, a.getWorkoutCount(), effectiveWorkout, a.isAttendance() ? "참석" : "불참", a.getFine());
        }
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
