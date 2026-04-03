package com.example.chatService.controller;

import com.example.chatService.dto.NicknameDto;
import com.example.chatService.dto.NicknameRequest;
import com.example.chatService.security.UserPrincipal;
import com.example.chatService.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/set-nickname")
    public ResponseEntity<?> setNickname(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody NicknameRequest request) {
            String trimmed = userService.setNickname(principal.getId(), request.nickname());
        return ResponseEntity.ok(new NicknameDto(trimmed));
    }
}