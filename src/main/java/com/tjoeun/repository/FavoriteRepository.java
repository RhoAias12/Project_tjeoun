package com.tjoeun.repository;

import com.tjoeun.entity.Favorite;
import com.tjoeun.entity.Recruitment;
import com.tjoeun.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface FavoriteRepository extends JpaRepository<Favorite, Integer> {
  List<Favorite> findByUser(Users user);
  void deleteByUserAndRecruitment(Users user, Recruitment recruitment);
}

