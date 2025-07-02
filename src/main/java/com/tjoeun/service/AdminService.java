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

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  // 모든 회원 리스트 조회 (관리자용)
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

  // 회원 삭제
  public void deleteUserById(Integer userIdx) {
    userRepository.deleteById(userIdx);
  }

  // 회원 상세 조회 (필요 시)
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

  // 회원 정보 수정
  public void updateUserInfo(Integer userIdx, UserFormDto dto) {
    Users user = userRepository.findById(userIdx).orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

    user.setUserNickname(dto.getUserNickname());

    if (dto.getUserPassword() != null && !dto.getUserPassword().isBlank()) {
      user.setUserPassword(passwordEncoder.encode(dto.getUserPassword()));
    }

    userRepository.save(user);
  }
  public Page<UserListDto> getPagedUsers(Pageable pageable) {
    // Users -> UserListDto 변환해서 Page로 반환
    return userRepository.findAll(pageable)
      .map(user -> new UserListDto(
        user.getUserIdx(),
        user.getUserEmail(),
        user.getUserNickname()
      ));
  }
}
