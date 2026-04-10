package com.lalit.authservice.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RefreshTokenResponse {

    private String accessToken;
    private String tokenType;
    private Integer expiresIn;
}
