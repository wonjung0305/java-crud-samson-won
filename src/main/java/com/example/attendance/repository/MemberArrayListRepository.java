package com.example.attendance.repository;

import com.example.attendance.model.Member;

import javax.swing.text.html.Option;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MemberArrayListRepository implements MemberRepository {

    private final List<Member> store = new ArrayList<>();

    @Override
    public void save(Member member) {
        store.add(member);
    }

    @Override
    public Optional<Member> findByStudentId(String studentId) {
        for(Member m : store){
            if(m.getStudentId().equals(studentId)){
                return Optional.of(m);
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Member> findByName(String name) {
        for(Member m : store){
            if(m.getName().equals(name)){
                return Optional.of(m);
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Member> findAll() {
        return new ArrayList<>(store);
    }

    @Override
    public void deleteByStudentId(String studentId) {
        store.removeIf(member -> member.getStudentId().equals(studentId));
    }
}
