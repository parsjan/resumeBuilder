package com.app.user.service;

import com.app.user.dto.UserProfileResponse;

import java.util.UUID;

public interface UserService {

    UserProfileResponse getProfile(UUID userId);
}
