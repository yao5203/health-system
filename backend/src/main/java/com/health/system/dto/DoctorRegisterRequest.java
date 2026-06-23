package com.health.system.dto;

import lombok.Data;

@Data
public class DoctorRegisterRequest {

    private String username;

    private String password;

    private String secretCode;

    private String specialty;

    private String expertiseTags;

    private String introduction;
}
