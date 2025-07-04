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
import java.util.Optional;

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

  // 즐겨찾기 등록 or 해제 (토글 방식)
  @Transactional
  public boolean toggleFavorite(String userEmail, Long recruitmentIdx) {
    Users user = userRepository.findByUserEmail(userEmail);
    if (user == null) {
      throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
    }

    Recruitment recruitment = recruitmentRepository.findById(recruitmentIdx)
            .orElseThrow(() -> new IllegalArgumentException("공고를 찾을 수 없습니다."));

    Optional<Favorite> existing = favoriteRepository.findByUserAndRecruitment(user, recruitment);

    if (existing.isPresent()) {
      favoriteRepository.delete(existing.get());
      return false; // 해제됨
    } else {
      Favorite favorite = new Favorite();
      favorite.setUser(user);
      favorite.setRecruitment(recruitment);
      favoriteRepository.save(favorite);
      return true; // 등록됨
    }
  }

  // 해당 공고가 스크랩되었는지 여부
  public boolean isFavorited(String userEmail, Long recruitmentIdx) {
    Users user = userRepository.findByUserEmail(userEmail);
    if (user == null) {
      throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
    }

    Recruitment recruitment = recruitmentRepository.findById(recruitmentIdx)
            .orElseThrow(() -> new IllegalArgumentException("공고를 찾을 수 없습니다."));

    return favoriteRepository.existsByUserAndRecruitment(user, recruitment);
  }
}
