package com.yasinsez.library.mapper;

import com.yasinsez.library.dto.MemberResponseDTO;
import com.yasinsez.library.model.Member;

public class MemberMapper {

    public static MemberResponseDTO toResponseDTO(Member member) {
        if (member == null) {
            return null;
        }
        return new MemberResponseDTO(
                member.getId(),
                UserMapper.toResponseDTO(member.getUser()),
                member.getMembershipStartDate(),
                member.getMembershipEndDate(),
                member.getAddress(),
                member.getFineBalance()
        );
    }
}
