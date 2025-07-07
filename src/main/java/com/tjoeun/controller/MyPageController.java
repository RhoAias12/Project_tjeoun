package com.tjoeun.controller;

import com.tjoeun.dto.*;
import com.tjoeun.entity.*;
import com.tjoeun.repository.*;
import com.tjoeun.service.MyPageService;
import com.tjoeun.service.RecruitmentService;
import com.tjoeun.service.UserService;
import com.tjoeun.util.PaginationUtil;
import com.tjoeun.util.PaginationUtil2;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

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
    private final ResumeContentRepository resumeContentRepository;

    private final RecruitmentService recruitmentService;

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
    @PostMapping("/member_delete")
    public String deleteMember(Principal principal, HttpServletRequest request) throws ServletException {
        String email = principal.getName();

        myPageService.deleteUserByEmail(email);
        request.logout();
        return "redirect:/";
    }

    @GetMapping("/react_list")
    public String reactList(@RequestParam(defaultValue = "1") int page,
                            @PageableDefault(size = 5) Pageable pageable,
                            Model model,
                            Principal principal) {
        String email = principal.getName();
        Users user = userRepository.findByUserEmail(email);
        Pageable correctedPageable = PageRequest.of(page - 1, pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "updatedAt"));

        Page<Resume> resumePage = myPageService.getPagedResumesByUserId(user.getUserIdx().longValue(), correctedPageable);

        model.addAttribute("resumes", resumePage.getContent());
        model.addAttribute("jobPage", resumePage);

        PaginationUtil.setPaging(model, resumePage, "/mypage/react_list");

        return "mypage/react_list";
    }




    // 이력서 저장
    @PostMapping("/react_write")
    public String saveResume(
      @ModelAttribute("resumeDto") @Valid ResumeDto resumeDto,
      @RequestParam("imgFile") MultipartFile imgFile,
      BindingResult bindingResult,
      Principal principal) {

        if (bindingResult.hasErrors()) {
            return "mypage/react_write"; // 에러가 있으면 작성 페이지로 다시 돌아감
        }

        // 로그인된 사용자 정보 가져오기
        String email = principal.getName();
        Users user = userRepository.findByUserEmail(email);

        if (user == null) {
            throw new IllegalArgumentException("로그인한 사용자를 찾을 수 없습니다.");
        }
        Timestamp now = new Timestamp(System.currentTimeMillis());

        // ResumeDto에서 Resume 엔티티로 변환하여 저장
        Resume resume = new Resume();
        resume.setUser(user);
        resume.setTitle(resumeDto.getTitle());
        resume.setContext(resumeDto.getContext());
        resume.setAddress(resumeDto.getAddress());
        resume.setPhoneNum(resumeDto.getPhoneNum());
        resume.setEducation(resumeDto.getEducation());
        resume.setAbility(resumeDto.getAbility());
        resume.setAntecedents(resumeDto.getAntecedents());
        resume.setAwards(resumeDto.getAwards());
        resume.setContext(resumeDto.getContext());
        resume.setCreatedAt(now);
        resume.setUpdatedAt(now);

        if (!imgFile.isEmpty()) {
            resume.setImg(imgFile.getOriginalFilename());
        }

        // 저장
        resumeRepository.save(resume);

        // ResumeContent 저장
        if (resumeDto.getResumeContents() != null) {
            for (ResumeContentDTO contentDto : resumeDto.getResumeContents()) {
                ResumeContent content = ResumeContent.builder()
                  .resume(resume)
                  .question(contentDto.getQuestion())
                  .context(contentDto.getContext())
                  .build();
                resumeContentRepository.save(content);
            }
        }

        // 저장 성공 시 react_list로 이동
        return "redirect:/mypage/react_list";
    }


    @GetMapping("/react_write")
    public String reactWrite(Model model, Principal principal) {
        Users user = userRepository.findByUserEmail(principal.getName());
        model.addAttribute("user", user);
        model.addAttribute("formDto", new ResumeDto());
        return "mypage/react_write";
    }

    @GetMapping("/react_detail/{id}")
    public String reactDetail(@PathVariable Long id,
                              @RequestParam(defaultValue = "1") int page,
                              @RequestParam(defaultValue = "false") boolean readonly,
                              @RequestParam(required = false) String prevUrl,
                              Model model) {
        Resume resume = resumeRepository.findById(id)
          .orElseThrow(() -> new IllegalArgumentException("해당 이력서를 찾을 수 없습니다."));

        model.addAttribute("resume", resume);
        model.addAttribute("page", page);
        model.addAttribute("readonly", readonly);
        model.addAttribute("prevUrl", prevUrl); // ← 이거 중요!
        return "mypage/react_detail";
    }

    @PostMapping("/react_delete/{id}")
    public String deleteResume(@PathVariable Long id,
                               @RequestParam(defaultValue = "1") int page) {
        resumeRepository.deleteById(id);
        return "redirect:/mypage/react_list?page=" + page;
    }

   @GetMapping("/react_modify/{id}")
   public String reactModify(@PathVariable Long id,
                             @RequestParam(defaultValue = "1") int page,
                             Model model) {
       Resume resume = resumeRepository.findById(id)
         .orElseThrow(() -> new IllegalArgumentException("해당 이력서를 찾을 수 없습니다."));
       model.addAttribute("resume", resume);
       model.addAttribute("page", page);
       return "mypage/react_modify";
   }

    @PostMapping("/react_modify/{id}")
    public String updateResume(
      @PathVariable Long id,
      @ModelAttribute ResumeDto resumeDto
    ) {
        myPageService.updateResume(id, resumeDto);
        return "redirect:/mypage/react_list";
    }


    @GetMapping("/apply_status")
    public String applyStatus(@RequestParam(defaultValue = "1") int page,
                              @RequestParam(required = false) String customSort,
                              @PageableDefault(size = 10) Pageable pageable,
                              Model model,
                              Principal principal) {

        String email = principal.getName();

        Pageable correctedPageable = PageRequest.of(
          page - 1,
          pageable.getPageSize(),
          Sort.by(Sort.Direction.DESC, "apply")
        );

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

        Pageable correctedPageable = PageRequest.of(
          page - 1,
          pageable.getPageSize(),
          Sort.by(Sort.Direction.DESC, "apply")
        );

        Page<FavoriteDTO> jobPage = myPageService.getPagedFavorites(email, correctedPageable);

//        System.out.println("로그인 유저: " + email);
//        System.out.println("스크랩 개수: " + jobPage.getTotalElements());
//        System.out.println("스크랩 리스트: " + jobPage.getContent());


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

    @GetMapping("/choice_list/{recruitmentIdx}")
    public String getResumeList(@PathVariable Long recruitmentIdx,
                                Model model,
                                @AuthenticationPrincipal UserDetails userDetails,
                                @RequestParam(defaultValue = "1") int page,
                                @PageableDefault(size = 5) Pageable pageable) {

        String email = userDetails.getUsername();
        System.out.println("로그인된 이메일: " + email);

        Users user = userService.findByUserEmail(email);
        System.out.println("조회된 userIdx: " + user.getUserIdx());

        Long userIdx = user.getUserIdx().longValue();
        List<Resume> resumes = myPageService.getResumesByUserId(userIdx);
        System.out.println("불러온 이력서 수: " + resumes.size());

        Pageable correctedPageable = PageRequest.of(page - 1, pageable.getPageSize(), pageable.getSort());
        Page<Resume> resumePage = myPageService.getPagedResumesByUserId(userIdx, correctedPageable);

        PaginationUtil.setPaging(model, resumePage, "/mypage/choice_list/" + recruitmentIdx);

        System.out.println("총 페이지 수: " + resumePage.getTotalPages());
        System.out.println("현재 페이지 데이터 수: " + resumePage.getContent().size());



        model.addAttribute("resumeList", resumePage.getContent());
        model.addAttribute("jobPage", resumePage);
        model.addAttribute("recruitmentIdx", recruitmentIdx);

        return "mypage/choice_list";
    }

    @PostMapping("/submit")
    public String submitApply(@RequestParam("recruitmentIdx") Long recruitmentId,
                              @RequestParam("resumeIdx") Long resumeIdx,
                              @AuthenticationPrincipal UserDetails userDetails,
                              RedirectAttributes redirectAttributes) {

        String email = userDetails.getUsername();
        Users user = userService.findByUserEmail(email);
        Recruitment recruitment = recruitmentService.getEntityById(recruitmentId);
        Resume resume = myPageService.getResumeById(resumeIdx);

        // 이미 지원한 이력 있는지 확인 (resume 없이)
        boolean alreadyApplied = applyHistoryRepository
                .existsByUser_UserIdxAndRecruitment_RecruitmentIdx(user.getUserIdx(), recruitment.getRecruitmentIdx());

        if (alreadyApplied) {
            redirectAttributes.addFlashAttribute("errorMessage", "이미 지원한 공고입니다. 중복 지원은 불가능합니다.");
            return "redirect:/empl/empl_detail/" + recruitmentId;
        }

        // 지원 이력 저장 (resume 없이)
        ApplyHistory history = ApplyHistory.builder()
                .user(user)
                .recruitment(recruitment)
                .resume(resume)
                .status(ApplyHistory.ApplyStatus.SUBMITTED)
                .apply(new Timestamp(System.currentTimeMillis()))
                .build();

        applyHistoryRepository.save(history);
        redirectAttributes.addFlashAttribute("successMessage", "정상적으로 지원 완료되었습니다.");
        return "redirect:/mypage/apply_status";
    }


    @GetMapping("/check-applied")
    @ResponseBody
    public ResponseEntity<Boolean> checkAlreadyApplied(@RequestParam Long recruitmentIdx,
                                                       @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        Users user = userService.findByUserEmail(email);

        boolean alreadyApplied = applyHistoryRepository
                .existsByUser_UserIdxAndRecruitment_RecruitmentIdx(user.getUserIdx(), recruitmentIdx);

        return ResponseEntity.ok(alreadyApplied);
    }

    @GetMapping("/check_applied/{recruitmentIdx}")
    @ResponseBody
    public ResponseEntity<?> checkApplied(@PathVariable Long recruitmentIdx, Principal principal) {
        String email = principal.getName();
        Users user = userRepository.findByUserEmail(email);
        boolean applied = applyHistoryRepository.existsByUserAndRecruitment(user,
          recruitmentRepository.findById(recruitmentIdx).orElseThrow());

        return ResponseEntity.ok().body(Map.of("applied", applied));
    }




}
