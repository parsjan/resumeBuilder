package com.app.user.dto;

import com.app.user.model.User;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class UserProfileResponse {

    private UUID id;
    private String email;
    private String fullName;
    private User.Provider provider;
    private Instant createdAt;
}
