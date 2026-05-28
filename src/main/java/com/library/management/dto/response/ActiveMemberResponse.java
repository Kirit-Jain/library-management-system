package com.library.management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActiveMemberResponse {
    private Long memberId;
    private String membershipNumber;
    private String fullName;
    private String email;
    private Long borrowCount;
    private Integer rank;
}