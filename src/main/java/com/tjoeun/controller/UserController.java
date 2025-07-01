package com.tjoeun.controller;

import com.tjoeun.dto.UserFormDto;
import com.tjoeun.entity.Users;
import com.tjoeun.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;
  private final PasswordEncoder passwordEncoder;

  // 회원가입 폼
  @GetMapping("/signup")
  public String userForm(Model model) {
    model.addAttribute("userFormDto", new UserFormDto());
    return "user/signup";
  }

  // 회원가입 처리
  @PostMapping("/signup")
  public String newUser(@Valid @ModelAttribute("userFormDto") UserFormDto dto,
                        BindingResult bindingResult,
                        Model model) {

    if (bindingResult.hasErrors()) {
      return "user/signup";
    }

    try {
      Users user = Users.createUser(dto, passwordEncoder);
      userService.saveUser(user);
    } catch (IllegalStateException e) {
      model.addAttribute("errorMessage", e.getMessage());
      return "user/signup";
    }

    return "redirect:/";
  }

  // 로그인 폼
  @GetMapping("/login")
  public String loginForm() {
    return "user/login";
  }
    @GetMapping("/apply_status")
    public String applyStatusPage() {
        return "user/apply_status";
    }

  // 로그인 에러
  @GetMapping("/login/error")
  public String loginError(Model model) {
    model.addAttribute("loginErrorMsg", "이메일 또는 비밀번호를 확인해주세요.");
    return "user/login";
  }
    @GetMapping("/scrap")
    public String scrapPage() {
        return "user/scrap";
    }
}
