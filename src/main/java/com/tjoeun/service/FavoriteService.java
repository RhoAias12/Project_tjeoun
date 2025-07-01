package com.tjoeun.service;

import com.tjoeun.entity.Favorite;
import com.tjoeun.entity.Recruitment;
import com.tjoeun.entity.Users;
import com.tjoeun.repository.FavoriteRepository;
import com.tjoeun.repository.JobPostingRepository;
import com.tjoeun.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Member;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final UserRepository userRepository;
    private final JobPostingRepository jobPostingRepository; // ✅ 여기 주입
    private final FavoriteRepository favoriteRepository;

    public boolean toggleFavorite(Integer userId, Long recruitmentId) {
        Users user = userRepository.findById(userId).orElseThrow();
        Recruitment recruitment = jobPostingRepository.findById(recruitmentId).orElseThrow();

        Optional<Favorite> existing = favoriteRepository.findByUserAndRecruitment(user, recruitment);

        if (existing.isPresent()) {
            favoriteRepository.delete(existing.get());
            return false;
        } else {
            Favorite favorite = Favorite.builder()
                    .user(user)
                    .recruitment(recruitment)
                    .build();
            favoriteRepository.save(favorite);
            return true;
        }
    }
}

