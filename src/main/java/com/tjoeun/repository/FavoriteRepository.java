package com.tjoeun.repository;

import com.tjoeun.entity.Favorite;
import com.tjoeun.entity.Recruitment;
import com.tjoeun.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
  boolean existsByUserAndRecruitment(Users user, Recruitment recruitment);

  Optional<Favorite> findByUserAndRecruitment(Users user, Recruitment recruitment);

  List<Favorite> findByUser(Users user);
  void deleteByUserAndRecruitment(Users user, Recruitment recruitment);
  Page<Favorite> findByUser(Users user, Pageable pageable);

  int countByRecruitment(Recruitment recruitment);
}

