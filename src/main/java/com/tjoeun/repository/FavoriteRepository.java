package com.tjoeun.repository;

import com.tjoeun.entity.Favorite;
import com.tjoeun.entity.Recruitment;
import com.tjoeun.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.User;

import java.lang.reflect.Member;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    boolean existsByUserAndRecruitment(Users user, Recruitment recruitment);
    Optional<Favorite> findByUserAndRecruitment(Users user, Recruitment recruitment);
    void deleteByUserAndRecruitment(Users user, Recruitment recruitment);

}
