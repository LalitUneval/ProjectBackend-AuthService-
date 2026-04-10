    package com.lalit.authservice.DTO;

    import lombok.AllArgsConstructor;
    import lombok.Builder;
    import lombok.Data;
    import lombok.NoArgsConstructor;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public class RefreshTokenRequest {
        // This field must match the key in your JSON body
        private String refreshToken;
    }