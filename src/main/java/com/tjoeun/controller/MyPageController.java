package com.tjoeun.controller;

import com.tjoeun.dto.ApplyHistoryDTO;
import com.tjoeun.dto.FavoriteDTO;
import com.tjoeun.dto.UserFormDto;
import com.tjoeun.entity.*;
import com.tjoeun.repository.*;
import com.tjoeun.service.MyPageService;
import com.tjoeun.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/mypage")
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageService myPageService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final ResumeRepository resumeRepository;

    @GetMapping("/member_modify")
    public String showMemberModifyForm(Model model, Principal principal) {
        String email = principal.getName();
        UserFormDto dto = myPageService.getUserFormDto(email);
        model.addAttribute("userFormDto", dto);
        return "mypage/member_modify";
    }

    @PostMapping("/member_modify")
    public String updateMember(@ModelAttribute("userFormDto") UserFormDto dto,
                               BindingResult bindingResult,
                               Principal principal,
                               Model model) {
        String email = principal.getName();
        return myPageService.updateUser(dto, email, bindingResult, model, passwordEncoder, userService);
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

    @GetMapping("/react_detail/{id}")
    public String reactDetail(@PathVariable Long id, Model model) {
        Resume resume = resumeRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("해당 이력서를 찾을 수 없습니다."));
        model.addAttribute("resume", resume);
        return "mypage/react_detail";
    }



    @GetMapping("/apply_status")
    public String applyStatus(Model model, Principal principal) {
        List<ApplyHistoryDTO> historyList = myPageService.getApplyHistoryList(principal.getName());
        model.addAttribute("historyList", historyList);
        return "mypage/apply_status";
    }

    @GetMapping("/scrap")
    public String scrapPage(Model model, Principal principal) {
        List<FavoriteDTO> favorites = myPageService.getFavorites(principal.getName());
        model.addAttribute("favorites", favorites);
        return "mypage/scrap";
    }

    @DeleteMapping("/scrap")
    @ResponseBody
    @Transactional
    public ResponseEntity<?> deleteScrap(@RequestBody FavoriteDTO favoriteDTO) {
        myPageService.deleteScrap(favoriteDTO);
        return ResponseEntity.ok().build();
    }
}
