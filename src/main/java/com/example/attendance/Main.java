package com.example.attendance;

import com.example.attendance.repository.AttendanceArrayListRepository;
import com.example.attendance.repository.AttendanceRepository;
import com.example.attendance.repository.MemberArrayListRepository;
import com.example.attendance.repository.MemberRepository;
import com.example.attendance.service.AttendanceService;
import com.example.attendance.view.ConsoleView;

public class Main {
    public static void main(String[] args){
        MemberRepository memberRepository = new MemberArrayListRepository();
        AttendanceRepository attendanceRepository = new AttendanceArrayListRepository();

        
        AttendanceService service = new AttendanceService(attendanceRepository, memberRepository, "2026-1");

        ConsoleView view = new ConsoleView(service);
        view.start();
    }
}
