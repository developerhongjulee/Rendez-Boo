package com.ssafy.a107.api.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JoinReq {
    private String email;
    private String password;
    private String city;
    private String birthday;
    private Boolean gender;
    private String phoneNumber;
    private String name;
    private String profileImagePath;
    private String mbti;
    private Boolean isAdmin;

    public void parsePhoneNumber() {
        if(this.phoneNumber.contains("-")) {
            this.phoneNumber.replaceAll("-", "");
        }
    }
}
