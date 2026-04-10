package com.lalit.authservice.DTO;

import com.lalit.authservice.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterRequest {
    //mail , password, role
    private String email;
    private String password;
    private UserRole role;
}
