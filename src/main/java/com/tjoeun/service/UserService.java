package com.tjoeun.service;

import com.tjoeun.elasticsearch.document.UserDocument;
import com.tjoeun.elasticsearch.repository.UserDocumentRepository;
import com.tjoeun.entity.Users;
import com.tjoeun.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;

import java.security.Principal;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

  private final UserRepository userRepository;
  private final UserDocumentRepository userDocumentRepository;

  public Users saveUser(Users user) {
    validateDuplicateUser(user);
    Users savedUser = userRepository.save(user);

    UserDocument userDoc = toUserDocument(savedUser);
    userDocumentRepository.save(userDoc);

    return savedUser;
  }

  private UserDocument toUserDocument(Users user) {
    return UserDocument.builder()
      .userIdx(user.getUserIdx())
      .userName(user.getUserName())
      .userEmail(user.getUserEmail())
      .userNickname(user.getUserNickname())
      .userBirth(user.getUserBirth().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
      .userRole(user.getUserRole().toString())
      .userCreatedAt(user.getUserCreatedAt())
      .build();
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

  public Users findByUserEmail(String email) {

    return userRepository.findByUserEmail(email);
  }

  @Transactional
  public void deleteById(Integer userIdx) {
    Users user = userRepository.findById(userIdx)
      .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
    userRepository.delete(user);

    // Elasticsearch 문서도 삭제
    userDocumentRepository.deleteById(userIdx);
  }


  public Integer getUserIdxFromPrincipal(Principal principal) {
    String email = principal.getName(); // Security에서 로그인 식별자로 email 반환
    Users user = userRepository.findByUserEmail(email);

    if (user == null) {
      throw new RuntimeException("사용자 없음");
    }

    return user.getUserIdx();
  }

}
