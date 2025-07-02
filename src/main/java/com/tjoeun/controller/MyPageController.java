package com.tjoeun.controller;

import com.tjoeun.entity.ApplyHistory;
import com.tjoeun.entity.Favorite;
import com.tjoeun.entity.Recruitment;
import com.tjoeun.entity.Users;
import com.tjoeun.repository.ApplyHistoryRepository;
import com.tjoeun.repository.FavoriteRepository;
import com.tjoeun.repository.RecruitmentRepository;
import com.tjoeun.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/mypage")
@RequiredArgsConstructor
public class MyPageController {

    private final ApplyHistoryRepository applyHistoryRepository;
    private final UserRepository userRepository;
    private final FavoriteRepository favoriteRepository;
    private final RecruitmentRepository recruitmentRepository;

    @GetMapping("/member_modify")
    public String memberModifyPage() {
        return "mypage/member_modify";
    }

    @GetMapping("/react_list")
    public String reactList() {
        return "mypage/react_list";
    }

    @GetMapping("/react_write")
    public String reactWrite() {
        return "mypage/react_write";
    }

    @GetMapping("/react_modify")
    public String reactModify() {
        return "mypage/react_modify";
    }

    @GetMapping("/react_detail")
    public String reactDetail() {
        return "mypage/react_detail";
    }

    @GetMapping("/apply_status")
    public String applyStatus(Model model, Principal principal) {
        String email = principal.getName();
        Users user = userRepository.findByUserEmail(email);

        if (user == null) {
            throw new IllegalArgumentException("로그인한 사용자를 찾을 수 없습니다.");
        }

        List<ApplyHistory> historyList = applyHistoryRepository.findByUser_UserIdx(user.getUserIdx());

        model.addAttribute("historyList", historyList);
        return "mypage/apply_status";
    }

    @GetMapping("/scrap")
    public String scrapPage(Model model, Principal principal) {
        String email = principal.getName();
        Users user = userRepository.findByUserEmail(email);

        if (user == null) {
            throw new IllegalArgumentException("로그인한 사용자를 찾을 수 없습니다.");
        }

        List<Favorite> favorites = favoriteRepository.findByUser(user);
        model.addAttribute("favorites", favorites);

        return "mypage/scrap";
    }

    @DeleteMapping("/scrap/{recruitmentIdx}")
    @ResponseBody
    @Transactional
    public ResponseEntity<?> deleteScrap(@PathVariable Long recruitmentIdx, Principal principal) {
        String email = principal.getName();
        Users user = userRepository.findByUserEmail(email);

        Recruitment recruitment = recruitmentRepository.findById(recruitmentIdx)
          .orElseThrow(() -> new IllegalArgumentException("공고를 찾을 수 없습니다."));

        favoriteRepository.deleteByUserAndRecruitment(user, recruitment);

        return ResponseEntity.ok().build();
    }
}
