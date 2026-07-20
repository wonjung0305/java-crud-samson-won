package com.example.attendance.service;

import com.example.attendance.model.Attendance;
import com.example.attendance.model.Member;
import com.example.attendance.repository.AttendanceRepository;
import com.example.attendance.repository.MemberRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final MemberRepository memberRepository;
    private String currentSemester;   // 현재 진행 중인 학기 (기본 == 2026-1)

    // 생성자
    public AttendanceService(AttendanceRepository attendanceRepository, MemberRepository memberRepository, String initialSemester) {
        this.attendanceRepository = attendanceRepository;
        this.memberRepository = memberRepository;
        this.currentSemester = initialSemester;
    }

    // 현재 학기 들고 오기
    public String getCurrentSemester() {
        return currentSemester;
    }

    // 프로그램 종료 시, 최종 저장
    public void saveAll() {
        memberRepository.saveAll();
        attendanceRepository.saveAll();
    }

    // 부원 등록
    public boolean registerMember(Member member) {

        // 학번 중복 제외
        if (memberRepository.findByStudentId(member.getStudentId()).isPresent()) {
            return false;
        }

        memberRepository.save(member);   // 부원 정보에 등록
        return true;
    }

    // 부원 전체 명단 조회
    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    // 이름으로 부원 검색
    public Optional<Member> searchMemberByName(String name) {
        return memberRepository.findByName(name);
    }

    // 학번으로 부원 검색
    public Optional<Member> searchMemberByStudentId(String studentId) {
        return memberRepository.findByStudentId(studentId);
    }

    // 부원 제명
    public void removeMember(String studentId) {
        memberRepository.deleteByStudentId(studentId);
    }

    // 부원 정보 수정 (이름/전화번호/학부) - 학번은 PK라 여기서는 안 바꿈
    public boolean updateMemberInfo(String studentId, String name, String phoneNumber, String department) {
        Optional<Member> memberOpt = memberRepository.findByStudentId(studentId);

        // 잘못된 학번인 경우
        if (memberOpt.isEmpty()) {
            return false;
        }

        // 정보 수정
        Member member = memberOpt.get();
        member.setName(name);
        member.setPhoneNumber(phoneNumber);
        member.setDepartment(department);

        // 정보 업데이트
        memberRepository.update(member);
        return true;
    }


    // 이번 학기 활동 인원만 조회
    public List<Member> getActiveMembers() {
        List<Member> result = new ArrayList<>();

        for (Member m : memberRepository.findAll()) {
            if (m.getStatus().equals("활동")) {
                result.add(m);
            }
        }

        return result;
    }

    // 학기 마감 및 다음 학기로 전환
        //    - leavingStudentIds: 다음 학기에 계속하지 않는 인원의 학번 목록 -> 휴학 처리
        //    - 나머지 활동 인원 자동으로 다음 학기도 활동 상태 유지
    public void closeSemester(List<String> leavingStudentIds, String nextSemester) {
        for (String studentId : leavingStudentIds) {

            // 휴학하는 학생의 학번 들고 와서
            Optional<Member> memberOpt = memberRepository.findByStudentId(studentId);

            if (memberOpt.isPresent()) {
                Member member = memberOpt.get();   // 객체 가져오고
                member.setStatus("휴학");   // 휴학으로 변경
                memberRepository.update(member);   // 업데이트
            }
        }
        this.currentSemester = nextSemester;
    }

// --------------------------------------------------

    // 이름 입력 -> 오운완 인증
    public boolean recordWorkoutByName(String name, int week){
        Optional<Member> memberOpt = memberRepository.findByName(name);

        // 존재하지 않는 이름인 경우
        if(memberOpt.isEmpty()){
            return false;
        }

        // 학번 가져오고 -> 학번으로 해당 학생 운동 기록 확인
        String studentId = memberOpt.get().getStudentId();
        Optional<Attendance> attendanceOpt = attendanceRepository.findByStudentIdAndWeek(studentId, currentSemester, week);   // 주차 출석 기록이 있는지

        Attendance attendance;

        if(attendanceOpt.isPresent()){   // 이미 이번 주차 기록이 있다면, +1 (이미 객체가 있는 경우)
            attendance = attendanceOpt.get();
            attendance.recordWorkout(LocalDate.now());   // 횟수 +1, 인증 날짜 기록(오늘 날짜)
            attendanceRepository.update(attendance);   // 수정
        } else{   // 해당 주차 기록이 없는 경우
            // 새로운 객체 생성
            attendance = new Attendance(null, studentId, currentSemester, week, 0, false);
            attendance.recordWorkout(LocalDate.now());   // 횟수 1로, 인증 날짜 기록
            attendanceRepository.save(attendance);
        }

        // 벌금 재계산
        calculateFine(studentId, attendance);
        return true;
    }

    // 이름 입력 -> 정모 참여 기록
    public boolean recordMeetingAttendanceByName(String name, int week){
        // 존재하는 부원인지
        Optional<Member> memberOpt = memberRepository.findByName(name);
        if(memberOpt.isEmpty()){
            return false;
        }

        // 학번 -> 해당 주차 운동 기록
        String studentId = memberOpt.get().getStudentId();
        Optional<Attendance> attendanceOpt = attendanceRepository.findByStudentIdAndWeek(studentId, currentSemester, week);

        Attendance attendance;
        if(attendanceOpt.isPresent()){   // 이미 이번 주 기록이 있는 경우
            attendance = attendanceOpt.get();
            attendance.setAttendance(true);   // 참석 상태
            attendanceRepository.update(attendance);   // 기록 수정
        } else{
            // 오운완 기록 X, 일단 정모만 참여한 경우
            attendance = new Attendance(null, studentId, currentSemester, week, 0, true);
            attendanceRepository.save(attendance);   // 기록 생성, pk 증가
        }

        calculateFine(studentId, attendance);   // 벌금 재계산
        return true;

    }

    // 이름 입력 -> 잘못 입력한 활동 기록 삭제
    public boolean deleteAttendanceRecord(String name, int week) {
        Optional<Member> memberOpt = memberRepository.findByName(name);

        // 존재하지 않는 경우
        if (memberOpt.isEmpty()) {
            return false;
        }

        // 학번 -> 해당 학기 운동 기록
        String studentId = memberOpt.get().getStudentId();
        Optional<Attendance> attendanceOpt = attendanceRepository.findByStudentIdAndWeek(studentId, currentSemester, week);

        // 없는 경우, 그냥 넘어감
        if (attendanceOpt.isEmpty()) {
            return false;
        }

        // 있으면, 운동 기록 삭제
        attendanceRepository.delete(attendanceOpt.get().getId());
        return true;
    }

    // 부원 상태 변경 (휴학 또는 복학)
    public boolean updateMemberStatus(String studentId, String newStatus) {
        Optional<Member> memberOpt = memberRepository.findByStudentId(studentId);

        // 정보 있으면
        if (memberOpt.isPresent()) {
            Member member = memberOpt.get();

            // 휴학 or 활동 상태로 변경
            member.setStatus(newStatus);

            // 복학하는 경우, 학기 수 +1
            if (newStatus.equals("활동")) {
                member.setActiveSemester(member.getActiveSemester() + 1);
            }

            memberRepository.update(member);   // 변경 사항 정보에 반영
            return true;
        }
        return false; // 부원을 찾지 못한 경우
    }

// --------------------------------------------------
    // 검색/조회용 학기 통계 (정모 결석, 오운완 미달 주차, 누적 벌금)
    // 기록이 아예 없는 주차도 결석/미인증으로 반영 - calculateFine과 동일한 방식
    public record SemesterStats(int absenceCount, int workoutFailCount, int totalFine) {}

    // 한 학기 전체 정보
    public SemesterStats getSemesterStats(String studentId, String semester) {
        // 부원의 출석 정보
        List<Attendance> history = attendanceRepository.findByStudentIdAndSemester(studentId, semester);

        int maxWeek = 0;
        int totalFine = 0;

        // 출석정보를 바탕으로 현재까지 몇 주차인지
        for (Attendance a : history) {

            // 진행된 가장 마지막 주차 = maxWeek
            if (a.getWeek() > maxWeek){
                maxWeek = a.getWeek();
            }
            totalFine += a.getFine();   // 벌금 모두 합하기
        }

        int absenceCount = 0;
        int workoutFailCount = 0;

        // 1주차 - maxWeek까지 반복
        for (int week = 1; week <= maxWeek; week++) {
            // 시험 주차 제외
            if (week == 8 || week == 16) continue;

            Attendance h = findByWeek(history, week);
            boolean attended = (h != null) && h.isAttendance();   // null이면 결석, 아니면 실제 참여 했는지 기록 확인
            int workoutCount = (h != null) ? h.getWorkoutCount() : 0;   // null이면 0, 아니면 운동 횟수 들고 오기

            // 결석이라면 횟수 증가
            if (!attended) {
                absenceCount++;
            }
            int includeMeetingCount = workoutCount + (attended ? 1 : 0);   // 정모 횟수 포함

            // 오운완 횟수가 3보다 작으면 실패
            if (includeMeetingCount < 3) {
                workoutFailCount++;
            }
        }

        // 구한 정보를 record 클래스에 담기
        return new SemesterStats(absenceCount, workoutFailCount, totalFine);
    }

    // 특정 주차 기록 찾기 (없으면 null)
    private Attendance findByWeek(List<Attendance> history, int week) {
        for (Attendance a : history) {
            if (a.getWeek() == week) {
                return a;
            }
        }
        return null;
    }

// --------------------------------------------------
    // 1. 벌금 계산 로직
        // 정모 불참: 2회 면제, 3회째 부터 5,000원 및 등비수열로 증가
        // 오운완 미달(주 3회 미만): 2주 면제, 3주째부터 5,000원 및 등비수열

    private void calculateFine(String studentId, Attendance current){
        // 기록 가져오기 (이번 학기 기록만)
        List<Attendance> history = attendanceRepository.findByStudentIdAndSemester(studentId, current.getSemester());

        int pastAbsence = 0;   // 지난 결석
        int pastWorkoutFail = 0;   // 지난 오운완 미인증

        // 1주차부터 현재 주차 전까지 전부 확인 (기록이 아예 없는 주차는 결석/미인증으로 처리)
        for (int week = 1; week < current.getWeek(); week++) {

            // 8주차, 16주차 제외(시험 및 종강 주차)
            if (week == 8 || week == 16) continue;

            // 해당 주차 기록 찾기 (없으면 null -> 아무것도 안 한 것으로 간주)
            Attendance h = findByWeek(history, week);

            boolean attended = (h != null) && h.isAttendance();
            int workoutCount = (h != null) ? h.getWorkoutCount() : 0;

            // 정모 참여X -> 결석 횟수++
            if (!attended) {
                pastAbsence++;
            }

            // 오운완 3회 미인증 -> 해당 주차 실패++
            int includeMeetingCount = workoutCount + (attended ? 1 : 0);
            if (includeMeetingCount < 3) {
                pastWorkoutFail++;
            }
        }

        // 정모 벌금 계산(한 학기)
        int meetingFine = 0;

        // 8주차, 16주차 정모 없음
        if((current.getWeek() != 8) && (current.getWeek() != 16)){
            int currentAbsenceCount = pastAbsence + (current.isAttendance() ? 0 : 1); // 총 결석 횟수

            // 면제권 X, 또 불참
            if ((currentAbsenceCount > 2) && !current.isAttendance()){
                int n = currentAbsenceCount - 2; // 면제 제외 실제로 내야 하는 벌금 횟수
                meetingFine = 5000 * (int) Math.pow(2, n-1);
            }

        }

        // 오운완 벌금 계산(매주)
        int workoutFine = 0;

        // 8주차, 16주차 면제
        if((current.getWeek() != 8) && (current.getWeek() != 16)){

            boolean isWorkoutFailure = current.getWorkoutCount() < 3;
            int currentWorkoutFailureCount = pastWorkoutFail + (isWorkoutFailure ? 1:0);

            // 면제권 X, 또 미인증
            if((currentWorkoutFailureCount > 2) && isWorkoutFailure){
                int n = currentWorkoutFailureCount - 2; // 면제 제외 실제로 내야 하는 벌금 횟수
                workoutFine = 5000 * (int) Math.pow(2, n-1);
            }

        }

        // 해당 주차에 낼 총 벌금
        current.setFine(meetingFine + workoutFine);
        attendanceRepository.update(current);   // 업데이트 반영

    }

    // 엑셀 출력용 정렬로직 (학기 지정, 과거 학기 조회용)
    public List<Attendance> getSortedAttendance(String semester, int week){
        List<Attendance> records = attendanceRepository.findByWeek(semester, week);

        // 활동 인원인데 이번 주 기록이 아예 없는 사람도 0회/불참으로 채워서 포함
        for (Member m : getActiveMembers()) {   // 활동 멤버인데
            boolean hasRecord = false;
            for (Attendance a : records) {   // 해당 주차 기록에 있다
                if (a.getStudentId().equals(m.getStudentId())) {
                    hasRecord = true;
                    break;
                }
            }
            if (!hasRecord) {   // 근데 없는 사람은 정모 불참, 오운완 0회로 실제 기록을 만들고 벌금까지 확정
                Attendance blank = new Attendance(null, m.getStudentId(), semester, week, 0, false);
                attendanceRepository.save(blank);
                calculateFine(m.getStudentId(), blank);
                records.add(blank);
            }
        }

        records.sort(new Comparator<Attendance>() {
            @Override
            public int compare(Attendance o1, Attendance o2) {

                int count1 = o1.getWorkoutCount() + (o1.isAttendance() ? 1:0); // 정모 포함 참여 횟수
                int count2 = o2.getWorkoutCount() + (o2.isAttendance() ? 1:0); // 정모 포함 참여 횟수

                // 오운완 인증 횟수 (내림차순)
                if(count1 != count2){
                    return Integer.compare(count2, count1);
                }

                // 학번 오름차순(고학번 우선) - 학번은 유일값이라 여기서 정렬이 항상 끝남
                return o1.getStudentId().compareTo(o2.getStudentId());
            }
        });

        return records;
    }


}
