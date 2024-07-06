package com.user_auth_org.hng_internship.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrgDto {
    @NotBlank(message = "Name is mandatory")
    private String name;
    private String description;
}
