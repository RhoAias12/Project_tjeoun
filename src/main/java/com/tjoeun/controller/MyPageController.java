package com.tjoeun.controller;

import com.tjoeun.dto.*;
import com.tjoeun.entity.*;
import com.tjoeun.repository.*;
import com.tjoeun.service.MyPageService;
import com.tjoeun.service.RecruitmentService;
import com.tjoeun.service.UserService;
import com.tjoeun.util.PaginationUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    private final ApplyHistoryResumeRepository applyHistoryResumeRepository;

    @Value("${upload.path}")
    private String uploadRootPath;

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

        Pageable correctedPageable = PageRequest.of(
          page - 1,
          pageable.getPageSize(),
          Sort.by(Sort.Direction.DESC, "updatedAt")
        );
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
      Principal principal) throws IOException {

        if (resumeDto.getTitle() != null && resumeDto.getTitle().length() > 60) {
            bindingResult.rejectValue("title", "length", "제목은 최대 60자까지 입력할 수 있습니다.");
        }

        if (bindingResult.hasErrors()) {
            return "mypage/react_write";
        }

        // 로그인된 사용자 정보 가져오기
        String email = principal.getName();
        Users user = userRepository.findByUserEmail(email);

        if (user == null) {
            throw new IllegalArgumentException("로그인한 사용자를 찾을 수 없습니다.");
        }
        Timestamp now = new Timestamp(System.currentTimeMillis());

        // ResumeDto에서 Resume 엔티티로 변환
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
        resume.setCreatedAt(now);
        resume.setUpdatedAt(now);

        // 이미지 업로드 처리
        if (!imgFile.isEmpty()) {
            String fileName = imgFile.getOriginalFilename();

            // 공통 업로드 루트에 /resume 붙여서 사용
            String uploadDir = uploadRootPath + "/resume";

            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(fileName);
            imgFile.transferTo(filePath.toFile());

            // DB에는 URL 경로로 저장해야 함
            resume.setImg("/uploads/images/resume/" + fileName);
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
    @Transactional
    public String deleteResume(@PathVariable Long id,
                               @RequestParam(defaultValue = "1") int page) {
        resumeRepository.deleteById(id);

        long totalCount = resumeRepository.count();
        int pageSize = 5;
        int lastPage = (int) Math.ceil((double) totalCount / pageSize);

        if (lastPage < 1) {
            lastPage = 1;
        }
        if (page > lastPage) {
            page = lastPage;
        }
        if (page < 1) {
            page = 1;
        }
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
    public String updateResume(@PathVariable Long id,
                               @ModelAttribute @Valid ResumeDto resumeDto,
                               BindingResult bindingResult,
                               @RequestParam("imgFile") MultipartFile imgFile) throws IOException {

        // 서비스 레이어에 모든 로직 위임
        myPageService.updateResume(id, resumeDto, imgFile);

        return "redirect:/mypage/react_detail/" + id;
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

    @GetMapping("/apply_resume_detail/{id}")
    public String getApplyHistoryResumeDetail(@PathVariable Long id, Model model) {
        ApplyHistoryResume applyHistoryResume = applyHistoryResumeRepository.findById(id)
          .orElseThrow(() -> new IllegalArgumentException("복사된 이력서를 찾을 수 없습니다. id=" + id));

        ApplyHistoryResumeDTO dto = myPageService.toDto(applyHistoryResume);

        model.addAttribute("applyResume", dto);
        return "mypage/apply_resume_detail";
    }


    @GetMapping("/scrap")
    public String scrapPage(@RequestParam(defaultValue = "1") int page,
                            @RequestParam(required = false) String customSort,
                            @PageableDefault(size = 8) Pageable pageable,
                            Model model,
                            Principal principal) {

        String email = principal.getName();

        // 1. 우선 제대로 된 Pageable 만들기 (page-1)
        Pageable correctedPageable = PageRequest.of(
          Math.max(page - 1, 0), // 음수 방지
          pageable.getPageSize(),
          Sort.by(Sort.Direction.DESC, "apply")
        );

        Page<FavoriteDTO> jobPage = myPageService.getPagedFavorites(email, correctedPageable);

        // 2. 페이지 보정 - 요청 페이지가 마지막 페이지보다 크면 마지막 페이지로 보정
        int totalPages = jobPage.getTotalPages();
        if (totalPages > 0 && page > totalPages) {
            // 요청 페이지가 총 페이지 수보다 크면 마지막 페이지로 다시 redirect 처리
            return "redirect:/mypage/scrap?page=" + totalPages;
        }

        // 3. 정상 처리
        model.addAttribute("favorites", jobPage.getContent());
        model.addAttribute("jobPage", jobPage);
        model.addAttribute("customSort", customSort);
        PaginationUtil.setPaging(model, jobPage, "/mypage/scrap");

        return "mypage/scrap";
    }


    @DeleteMapping("/scrap")
    @ResponseBody
    @Transactional
    public ResponseEntity<?> deleteScrap(@RequestBody FavoriteDTO favoriteDTO,
                                         @RequestParam(required = false) Integer page,
                                         @RequestParam(required = false, defaultValue = "8") Integer size) {
        myPageService.deleteScrap(favoriteDTO);

        // 삭제 후 현재 페이지에 남은 스크랩 수 확인
        Users user = userRepository.findById(favoriteDTO.getUserIdx())
          .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size); // page는 0-based
        Page<Favorite> resultPage = favoriteRepository.findByUser(user, pageable);

        int currentPage = Math.max(page != null ? page : 1, 1);
        int newPage = resultPage.getTotalElements() == 0 && currentPage > 1
          ? currentPage - 1
          : currentPage;

        return ResponseEntity.ok().body(Map.of("newPage", newPage));
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

        ResumeDto resumeDto = myPageService.getResumeDetail(resumeIdx);

        boolean alreadyApplied = applyHistoryRepository
          .existsByUser_UserIdxAndRecruitment_RecruitmentIdx(user.getUserIdx(), recruitment.getRecruitmentIdx());

        if (alreadyApplied) {
            redirectAttributes.addFlashAttribute("errorMessage", "이미 지원한 공고입니다. 중복 지원은 불가능합니다.");
            return "redirect:/empl/empl_detail/" + recruitmentId;
        }

//        // 지원 이력 저장 (resume 없이)
//        ApplyHistory history = ApplyHistory.builder()
//          .user(user)
//          .recruitment(recruitment)
//          .resume(resume)
//          .status(ApplyHistory.ApplyStatus.SUBMITTED)
//          .apply(new Timestamp(System.currentTimeMillis()))
//          .build();

        myPageService.saveApplyHistoryWithResumeCopy(user, recruitment, resumeDto);

//        applyHistoryRepository.save(history);
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
