package com.tjoeun.service;

import com.tjoeun.dto.FavoriteDTO;
import com.tjoeun.dto.UserFormDto;
import com.tjoeun.entity.ApplyHistory;
import com.tjoeun.entity.Favorite;
import com.tjoeun.entity.Recruitment;
import com.tjoeun.entity.Users;
import com.tjoeun.repository.ApplyHistoryRepository;
import com.tjoeun.repository.FavoriteRepository;
import com.tjoeun.repository.RecruitmentRepository;
import com.tjoeun.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MyPageService {

  private final FavoriteRepository favoriteRepository;
  private final RecruitmentRepository recruitmentRepository;
  private final UserRepository userRepository;
  private final ApplyHistoryRepository applyHistoryRepository;

  public List<ApplyHistory> getApplyHistoryList(String email) {
    Users user = userRepository.findByUserEmail(email);
    if (user == null) {
      throw new IllegalArgumentException("로그인한 사용자를 찾을 수 없습니다.");
    }
    return applyHistoryRepository.findByUser_UserIdx(user.getUserIdx());
  }

  @Transactional(readOnly = true)
  public UserFormDto getUserFormDto(String email) {
    Users user = userRepository.findByUserEmail(email);
    if (user == null) {
      throw new IllegalArgumentException("로그인한 사용자를 찾을 수 없습니다.");
    }

    UserFormDto dto = new UserFormDto();
    dto.setUserName(user.getUserName());
    dto.setUserEmail(user.getUserEmail());
    dto.setUserNickname(user.getUserNickname());
    dto.setUserBirth(user.getUserBirth());
    dto.setUserPassword("");
    return dto;
  }

  @Transactional
  public String updateUser(UserFormDto dto, String email, BindingResult bindingResult, Model model, PasswordEncoder passwordEncoder, UserService userService) {
    Users user = userRepository.findByUserEmail(email);
    if (user == null) {
      throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
    }
    if (!dto.getUserNickname().equals(user.getUserNickname()) &&
      userService.nicknameExists(dto.getUserNickname())) {
      model.addAttribute("nicknameError", "이미 사용 중인 닉네임입니다.");
      return "mypage/member_modify";
    }
    if (dto.getUserPassword() != null && !dto.getUserPassword().isBlank()) {
      user.setUserPassword(passwordEncoder.encode(dto.getUserPassword()));
    }
    user.setUserNickname(dto.getUserNickname());
    userRepository.save(user);
    return "redirect:/mypage/member_modify?success";
  }


  @Transactional
  public void deleteScrap(FavoriteDTO favoriteDTO) {
    Users user = userRepository.findById(favoriteDTO.getUserIdx())
      .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

    Recruitment recruitment = recruitmentRepository.findById(favoriteDTO.getRecruitmentIdx())
      .orElseThrow(() -> new IllegalArgumentException("공고를 찾을 수 없습니다."));

    favoriteRepository.deleteByUserAndRecruitment(user, recruitment);
  }

  public List<FavoriteDTO> getFavorites(String userEmail) {
    Users user = userRepository.findByUserEmail(userEmail);
    if (user == null) {
      throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
    }

    List<Favorite> favorites = favoriteRepository.findByUser(user);

    return favorites.stream().map(favorite -> FavoriteDTO.builder()
      .favoriteIdx(favorite.getFavoriteIdx())
      .userIdx(favorite.getUser().getUserIdx())
      .recruitmentIdx(favorite.getRecruitment().getRecruitmentIdx())
      .title(favorite.getRecruitment().getTitle())
      .company(favorite.getRecruitment().getCompany())
      .deadline(favorite.getRecruitment().getDeadline())
      .build()
    ).toList();
  }
}
