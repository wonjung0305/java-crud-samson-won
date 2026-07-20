package com.example.attendance.repository;

import com.example.attendance.model.Member;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MemberFileRepository implements MemberRepository {
    private static final String FILE_PATH = "members.csv";
    private final List<Member> store = new ArrayList<>();

    public MemberFileRepository() {
        loadFromFile();   // 파일에서 데이터 복원
    }

    // 파일에서 데이터를 한 줄씩 읽어와 리스트에 담는 로직
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

                // 순서: studentId, name, phoneNumber, department, status, activeSemester
                Member member = new Member(data[0], data[1], data[2], data[3], Integer.parseInt(data[5]));
                member.setStatus(data[4]);   // 실제 값으로 덮어쓰기 (기본 == 활동)
                store.add(member);
            }
        } catch (IOException e) {
            System.out.println("회원 파일 로드 실패: " + e.getMessage());
        }
    }

    // 현재 메모리에(store) 있는 리스트를 파일에 통째로 덮어쓰는 로직
    private void saveToFile() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (Member m : store) {
                bw.write(m.toCsvString());
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("회원 파일 저장 실패: " + e.getMessage());
        }
    }

    // 부원 등록
    @Override
    public void save(Member member) {
        store.add(member); // store에 더하고
        saveToFile();   // 파일에 저장
    }

    // 학번 -> 부원 정보
    @Override
    public Optional<Member> findByStudentId(String studentId) {
        for (Member m : store) {
            if (m.getStudentId().equals(studentId)) {
                return Optional.of(m);
            }
        }
        return Optional.empty();
    }

    // 이름 -> 부원 정보
    @Override
    public Optional<Member> findByName(String name) {
        for (Member m : store) {
            if (m.getName().equals(name)) {
                return Optional.of(m);
            }
        }
        return Optional.empty();
    }

    // 전체 부원 정보
    @Override
    public List<Member> findAll() {
        return new ArrayList<>(store);
    }

    // 부원 정보 수정(파일에 업데이트)
    @Override
    public void update(Member member) {
        for (int i = 0; i < store.size(); i++) {
            if (store.get(i).getStudentId().equals(member.getStudentId())) {
                store.set(i, member);
                saveToFile();
                return;
            }
        }
    }

    // 학번 -> 부원 정보 삭제(파일에 업데이트)
    @Override
    public void deleteByStudentId(String studentId) {
        store.removeIf(member -> member.getStudentId().equals(studentId));
        saveToFile();
    }

    // 전체 저장 (프로그램 종료 시 사용)
    @Override
    public void saveAll() {
        saveToFile();
    }
}
