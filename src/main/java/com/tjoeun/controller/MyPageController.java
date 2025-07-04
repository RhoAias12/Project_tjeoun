package com.tjoeun.controller;

import com.tjoeun.dto.ApplyHistoryDTO;
import com.tjoeun.dto.FavoriteDTO;
import com.tjoeun.dto.ResumeDto;
import com.tjoeun.dto.UserFormDto;
import com.tjoeun.entity.*;
import com.tjoeun.repository.*;
import com.tjoeun.service.MyPageService;
import com.tjoeun.service.UserService;
import com.tjoeun.util.PaginationUtil;
import com.tjoeun.util.PaginationUtil2;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
import org.springframework.web.multipart.MultipartFile;


import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/mypage")
@RequiredArgsConstructor
public class MyPageController {

    private final ApplyHistoryRepository applyHistoryRepository;
    private final UserRepository userRepository;
    private final MyPageService myPageService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final FavoriteRepository favoriteRepository;
    private final RecruitmentRepository recruitmentRepository;
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
    public String reactList(Model model, Principal principal) {
        String email = principal.getName();
        Users user = userRepository.findByUserEmail(email);

        // 로그인한 사용자에 해당하는 이력서만
        List<Resume> resumes = resumeRepository.findByUser(user);

        model.addAttribute("resumes", resumes);
        return "mypage/react_list";
    }

    // 이력서 저장
    @PostMapping("/react_write")
    public String saveResume(
      @ModelAttribute("resumeDto") @Valid ResumeDto resumeDto,
      @RequestParam("imgFile") MultipartFile imgFile,  // 이미지
      BindingResult bindingResult,
      Principal principal) {

        if (bindingResult.hasErrors()) {
            return "mypage/react_write";
        }

        String email = principal.getName();
        Users user = userRepository.findByUserEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("로그인 사용자가 없습니다.");
        }

        Resume resume = new Resume();
        resume.setUser(user);
        resume.setTitle(resumeDto.getTitle());
        resume.setAddress(resumeDto.getAddress());
        resume.setPhoneNum(resumeDto.getPhoneNum());
        resume.setEducation(resumeDto.getEducation());
        resume.setAbility(resumeDto.getAbility());
        resume.setAntecedents(resumeDto.getAntecedents());
        resume.setAwards(resumeDto.getAwards());
        resume.setContext(resumeDto.getContext());

        // 질문/답변
        resume.setQuestion1(resumeDto.getQuestion1());
        resume.setAnswer1(resumeDto.getAnswer1());
        resume.setQuestion2(resumeDto.getQuestion2());
        resume.setAnswer2(resumeDto.getAnswer2());
        resume.setQuestion3(resumeDto.getQuestion3());
        resume.setAnswer3(resumeDto.getAnswer3());
        resume.setQuestion4(resumeDto.getQuestion4());
        resume.setAnswer4(resumeDto.getAnswer4());

        // 이미지
        if (!imgFile.isEmpty()) {
            resume.setImg(imgFile.getOriginalFilename());
            // 실제 파일 저장은 필요하면 구현
        }

        resumeRepository.save(resume);

        return "redirect:/mypage/react_list";
    }


    @GetMapping("/react_write")
    public String reactWrite(Model model, Principal principal) {
        Users user = userRepository.findByUserEmail(principal.getName());
        model.addAttribute("user", user);
        model.addAttribute("formDto", new ResumeDto());
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
        Resume resume = resumeRepository.findById(id)
          .orElseThrow(() -> new IllegalArgumentException("해당 이력서를 찾을 수 없습니다."));
        model.addAttribute("resume", resume);
        return "mypage/react_detail";
    }

    //삭제
    @PostMapping("/react_delete/{id}")
    public String deleteResume(@PathVariable Long id) {
        resumeRepository.deleteById(id);
        return "redirect:/mypage/react_list";
    }
   // 수정
    @GetMapping("/react_modify/{id}")
    public String reactModify(@PathVariable Long id, Model model) {
        Resume resume = resumeRepository.findById(id)
          .orElseThrow(() -> new IllegalArgumentException("해당 이력서를 찾을 수 없습니다."));
        model.addAttribute("resume", resume);
        return "mypage/react_modify";
    }


    @GetMapping("/apply_status")
    public String applyStatus(@RequestParam(defaultValue = "1") int page,
                              @RequestParam(required = false) String customSort,
                              @PageableDefault(size = 10) Pageable pageable,
                              Model model,
                              Principal principal) {

        String email = principal.getName();

        Pageable correctedPageable = PageRequest.of(page - 1, pageable.getPageSize(), pageable.getSort());

        Page<ApplyHistoryDTO> historyPage = myPageService.getPagedApplyHistories(email, correctedPageable);

        PaginationUtil.setPaging(model, historyPage, "/mypage/apply_status");

        model.addAttribute("historyList", historyPage.getContent());
        model.addAttribute("jobPage", historyPage);

        return "mypage/apply_status";
    }

    @GetMapping("/scrap")
    public String scrapPage(@RequestParam(defaultValue = "1") int page,
                            @RequestParam(required = false) String customSort,
                            @PageableDefault(size = 8) Pageable pageable,
                            Model model,
                            Principal principal) {

        String email = principal.getName();
        Pageable correctedPageable = PageRequest.of(page - 1, pageable.getPageSize(), pageable.getSort());
        Page<FavoriteDTO> jobPage = myPageService.getPagedFavorites(email, correctedPageable);

        System.out.println("📌 로그인 유저: " + email);
        System.out.println("📌 스크랩 개수: " + jobPage.getTotalElements());
        System.out.println("📌 스크랩 리스트: " + jobPage.getContent());


        model.addAttribute("favorites", jobPage.getContent());
        model.addAttribute("jobPage", jobPage);
        model.addAttribute("customSort", customSort);
        PaginationUtil2.setPaging(model, jobPage, "/mypage/scrap", customSort, 5);

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
