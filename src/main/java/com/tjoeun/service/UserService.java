package com.tjoeun.service;

import com.tjoeun.entity.Users;
import com.tjoeun.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

  private final UserRepository userRepository;

  public Users saveUser(Users user) {
    validateDuplicateUser(user);
    return userRepository.save(user);
  }

  private void validateDuplicateUser(Users user) {
    if (userRepository.findByUserEmail(user.getUserEmail()) != null) {
      throw new IllegalStateException("이미 가입된 이메일입니다.");
    }
    if (userRepository.findByUserNickname(user.getUserNickname()) != null) {
      throw new IllegalStateException("이미 사용 중인 닉네임입니다.");
    }
  }

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    Users user = userRepository.findByUserEmail(email);
    if (user == null) {
      throw new UsernameNotFoundException(email);
    }

    return User.builder()
      .username(user.getUserEmail())
      .password(user.getUserPassword())
      .roles(user.getUserRole().toString())
      .build();
  }

  public boolean emailExists(String email) {
    return userRepository.findByUserEmail(email) != null;
  }

  public boolean nicknameExists(String nickname) {
    return userRepository.findByUserNickname(nickname) != null;
  }
}
