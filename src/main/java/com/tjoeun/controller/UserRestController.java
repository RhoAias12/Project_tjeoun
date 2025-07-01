package com.tjoeun.controller;

import com.tjoeun.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserRestController {

  private final UserService userService;

  // 이메일 중복 확인
  @GetMapping("/check-email")
  public ResponseEntity<?> checkEmail(@RequestParam String email) {
    boolean exists = userService.emailExists(email);
    return ResponseEntity.ok(Collections.singletonMap("exists", exists));
  }

  // 닉네임 중복 확인
  @GetMapping("/check-nickname")
  public ResponseEntity<?> checkNickname(@RequestParam String nickname) {
    boolean exists = userService.nicknameExists(nickname);
    return ResponseEntity.ok(Collections.singletonMap("exists", exists));
  }
}
