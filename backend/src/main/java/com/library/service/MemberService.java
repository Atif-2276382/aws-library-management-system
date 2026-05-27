package com.library.service;

import com.library.dto.CreateMemberRequest;
import com.library.dto.UpdateMemberRequest;
import com.library.entity.Member;

import java.util.List;

public interface MemberService {
    Member createMember(CreateMemberRequest request);
    List<Member> getAllMembers();
    Member getMemberById(Long id);
    Member updateMember(Long id,UpdateMemberRequest request);
    void deleteMember(Long id);
}