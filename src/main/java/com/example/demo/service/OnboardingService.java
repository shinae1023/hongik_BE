package com.example.demo.service;

import com.example.demo.dto.request.OnboardingRequestDto;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;

@Service
public class OnboardingService {

    private final UserRepository userRepository;

    public OnboardingService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void saveOnboardingInfo(User principalUser, OnboardingRequestDto request) {
        User user = userRepository.findByUserId(principalUser.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + principalUser.getUserId()));

        user.updateOnboardingInfo(
                request.getPreferredDong(),
                new HashSet<>(request.getThemes())
        );

        userRepository.save(user);
    }
}
