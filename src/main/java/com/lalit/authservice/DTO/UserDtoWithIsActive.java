package com.lalit.authservice.DTO;

import com.lalit.authservice.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDtoWithIsActive {
    private Long id;
    private String email;
    private UserRole role;
    private Boolean isActive;
}