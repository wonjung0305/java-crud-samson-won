package com.example.attendance.service;

import com.example.attendance.model.Attendance;
import com.example.attendance.repository.AttendanceRepository;

import java.util.Comparator;
import java.util.List;

public class AttendanceService {
    private final AttendanceRepository repository;

    // 생성자 정의
    public AttendanceService(AttendanceRepository repository) {
        this.repository = repository;
    }

    // 앞으로 벌금 계산 및 정렬 로직 세워야 함

    // 1. 기록 등록 및 벌금 계산
    public void registerAttendance(Attendance attendance){
        calculateFine(attendance);   // 벌금 계산
        repository.save(attendance);   // 기록 저장
    }

    // 2. 전체 기록 조회
    public List<Attendance> getAllRecords(){
        return repository.findAll();
    }

    // 3. 기록 수정 (+벌금도 다시 계산)
    public void modifyAttendance(Attendance attendance){
        calculateFine(attendance);
        repository.update(attendance);
    }

    // 4. 기록 삭제
    public void removeRecord(Long id){
        repository.delete(id);
    }

    // --------------------------------------------------

    // 1. 벌금 계산 로직
        // 정모 불참: 2회 면제, 3회째 부터 5,000원 및 등비수열로 증가
        // 오운완 미달(주 3회 미만): 2주 면제, 3주째부터 5,000원 및 등비수열로 증가

    private void calculateFine(Attendance current){
        List<Attendance> history = repository.findByStudentId(current.getStudentId());

        int pastAbsence = 0;   // 지난 결석
        int pastWorkoutFail = 0;   // 지난 오운완 미인증

        // 모든 기록에서 하나씩 차례대로 반복
        for(Attendance h: history){

            if (h.getWeek() < current.getWeek()){

                // 8주차, 16주차 제외(시험 및 종강 주차)
                if ((h.getWeek() != 8) && (h.getWeek() != 16)){

                    // 정모 참여X -> 결석 횟수++
                    if (!h.isAttendance()){
                        pastAbsence++;
                    }

                    // 오운완 3회 미인증 -> 해당 주차 실패++
                    int includeMeetingCount = h.getWorkoutCount() + (h.isAttendance() ? 1:0);
                    if(includeMeetingCount < 3){
                        pastWorkoutFail++;
                    }

                }
            }
        }

        // ---------- 정모 벌금 계산(한 학기) ----------
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

        // ---------- 오운완 벌금 계산(매주) ----------
        int workoutFine = 0;

        // 8주차, 16주차 면제
        if((current.getWeek() != 8) && (current.getWeek() != 16)){
            int includeMeetingCount = current.getWorkoutCount() + (current.isAttendance() ? 1:0);

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

    }

    // 엑셀 출력용 정렬로직
    public List<Attendance> getSortedAttendance(int week){
        List<Attendance> records = repository.findByWeek(week);

        records.sort(new Comparator<Attendance>() {
            @Override
            public int compare(Attendance o1, Attendance o2) {

                int count1 = o1.getWorkoutCount() + (o1.isAttendance() ? 1:0); // 정모 포함 참여 횟수
                int count2 = o2.getWorkoutCount() + (o2.isAttendance() ? 1:0); // 정모 포함 참여 횟수

                // 오운완 인증 횟수 (내림차순)
                if(count1 != count2){
                    return Integer.compare(count2, count1);
                }

                // 학번 오름차순(고학번 우선)
                if(!o1.getStudentId().equals(o2.getStudentId())){
                    return o1.getStudentId().compareTo(o2.getStudentId());
                }

                // 이름 오름차순
                return o1.getName().compareTo(o2.getName());
            }
        });

        return records;
    }


}
