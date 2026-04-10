package com.lalit.authservice.DTO;


import com.lalit.authservice.entity.UserRole;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDto { // Added 'public'
    private Long id;
    private String email;
    private UserRole role;
}