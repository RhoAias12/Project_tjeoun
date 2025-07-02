package com.tjoeun.service;

import com.tjoeun.dto.UserFormDto;
import com.tjoeun.dto.UserListDto;
import com.tjoeun.entity.Users;
import com.tjoeun.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserService userService;

  public List<UserListDto> getAllUsers() {
    List<Users> users = userRepository.findAll();
    return users.stream()
      .map(user -> new UserListDto(
        user.getUserIdx(),
        user.getUserEmail(),
        user.getUserNickname()
      ))
      .collect(Collectors.toList());
  }

  public Page<UserListDto> getPagedUsers(Pageable pageable) {
    Page<Users> userPage = userRepository.findAll(pageable);
    return userPage.map(user -> new UserListDto(
      user.getUserIdx(),
      user.getUserEmail(),
      user.getUserNickname()
    ));
  }

  public UserFormDto getUserFormDtoById(Integer userIdx) {
    Users user = userRepository.findById(userIdx)
      .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다."));

    UserFormDto dto = new UserFormDto();
    dto.setUserName(user.getUserName());
    dto.setUserEmail(user.getUserEmail());
    dto.setUserNickname(user.getUserNickname());
    dto.setUserBirth(user.getUserBirth());
    dto.setUserPassword("");

    return dto;
  }

  public void deleteUserById(Integer userIdx) {
    userRepository.deleteById(userIdx);
  }

  public void updateUser(Integer userIdx,
                         UserFormDto dto,
                         BindingResult bindingResult,
                         Model model) {

    Users user = userRepository.findById(userIdx)
      .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

    if (!dto.getUserNickname().equals(user.getUserNickname()) &&
      userService.nicknameExists(dto.getUserNickname())) {
      model.addAttribute("nicknameError", "이미 사용 중인 닉네임입니다.");
      throw new IllegalArgumentException("중복된 닉네임");
    }
    if (dto.getUserPassword() != null && !dto.getUserPassword().isBlank()) {
      String encodedPassword = passwordEncoder.encode(dto.getUserPassword());
      user.setUserPassword(encodedPassword);
    }
    user.setUserNickname(dto.getUserNickname());
    userRepository.save(user);
  }
}
