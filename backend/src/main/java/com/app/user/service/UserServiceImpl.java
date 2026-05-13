package com.app.user.service;

import com.app.common.AppException;
import com.app.common.ErrorCode;
import com.app.user.dto.UserProfileResponse;
import com.app.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(UUID userId) {
        return userRepository.findById(userId)
                .map(user -> UserProfileResponse.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .fullName(user.getFullName())
                        .provider(user.getProvider())
                        .createdAt(user.getCreatedAt())
                        .build())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }
}
