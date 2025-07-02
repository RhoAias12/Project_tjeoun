package com.tjoeun.service;

import com.tjoeun.entity.Favorite;
import com.tjoeun.entity.Recruitment;
import com.tjoeun.entity.Users;
import com.tjoeun.repository.FavoriteRepository;
import com.tjoeun.repository.RecruitmentRepository;
import com.tjoeun.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteService {

  private final FavoriteRepository favoriteRepository;
  private final RecruitmentRepository recruitmentRepository;
  private final UserRepository userRepository;

  @Transactional
  public void deleteScrap(String userEmail, Long recruitmentIdx) {
    Users user = userRepository.findByUserEmail(userEmail);
    if (user == null) {
      throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
    }

    Recruitment recruitment = recruitmentRepository.findById(recruitmentIdx)
      .orElseThrow(() -> new IllegalArgumentException("공고를 찾을 수 없습니다."));

    favoriteRepository.deleteByUserAndRecruitment(user, recruitment);
  }

  public List<Favorite> getFavorites(String userEmail) {
    Users user = userRepository.findByUserEmail(userEmail);
    if (user == null) {
      throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
    }
    return favoriteRepository.findByUser(user);
  }
}
