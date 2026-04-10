package com.lalit.authservice.DTO;



import com.lalit.authservice.entity.UserRole; // Use the Enum we discussed
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterResponse {
    private Long id;
    private String email;
    private UserRole role;
    private boolean isActive;
    private LocalDateTime createdAt;
    private String message;

}