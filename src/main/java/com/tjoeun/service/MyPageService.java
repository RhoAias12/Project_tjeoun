package com.tjoeun.service;

import com.tjoeun.dto.*;
import com.tjoeun.entity.*;
import com.tjoeun.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MyPageService {

  @Value("${upload.path}")
  private String uploadRootPath;

  private final FavoriteRepository favoriteRepository;
  private final RecruitmentRepository recruitmentRepository;
  private final UserRepository userRepository;
  private final ApplyHistoryRepository applyHistoryRepository;
  private final ResumeRepository resumeRepository;
  private final ApplyHistoryResumeRepository applyHistoryResumeRepository;

  public List<ApplyHistoryDTO> getApplyHistoryList(String email) {
    Users user = userRepository.findByUserEmail(email);
    if (user == null) {
      throw new IllegalArgumentException("로그인한 사용자를 찾을 수 없습니다.");
    }

    List<ApplyHistory> historyList = applyHistoryRepository.findByUser_UserIdx(user.getUserIdx());

    return historyList.stream().map(history -> ApplyHistoryDTO.builder()
                    .applyHistoryId(history.getOptionalIdx())
                    .statusDisplay(history.getStatus().getDisplay())
                    .recruitmentTitle(history.getRecruitment().getTitle())
                    .recruitmentCompany(history.getRecruitment().getCompany())
                    .applyHistoryResumeId(history.getApplyHistoryResume().getApplyHistoryResumeId())
                    .build())
            .toList();
  }

  public Page<ApplyHistoryDTO> getPagedApplyHistories(String email, Pageable pageable) {
    Users user = userRepository.findByUserEmail(email);
    if (user == null) {
      throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
    }

    Page<ApplyHistory> page = applyHistoryRepository.findByUser(user, pageable);

    return page.map(history -> ApplyHistoryDTO.builder()
            .applyHistoryId(history.getOptionalIdx())
            .statusDisplay(history.getStatus().getDisplay())
            .recruitmentTitle(history.getRecruitment().getTitle())
            .recruitmentCompany(history.getRecruitment().getCompany())
            .recruitmentId(history.getRecruitment().getRecruitmentIdx())
            .applyHistoryResumeId(history.getApplyHistoryResume().getApplyHistoryResumeId())
            .build());
  }

  @Transactional(readOnly = true)
  public UserFormDto getUserFormDto(String email) {
    Users user = userRepository.findByUserEmail(email);
    if (user == null) {
      throw new IllegalArgumentException("로그인한 사용자를 찾을 수 없습니다.");
    }

    UserFormDto dto = new UserFormDto();
    dto.setUserName(user.getUserName());
    dto.setUserEmail(user.getUserEmail());
    dto.setUserNickname(user.getUserNickname());
    dto.setUserBirth(user.getUserBirth());
    dto.setUserPassword("");
    return dto;
  }

  @Transactional
  public String updateUser(UserFormDto dto, String email, BindingResult bindingResult, Model model, PasswordEncoder passwordEncoder, UserService userService) {
    Users user = userRepository.findByUserEmail(email);
    if (user == null) {
      throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
    }
    if (!dto.getUserNickname().equals(user.getUserNickname()) &&
            userService.nicknameExists(dto.getUserNickname())) {
      model.addAttribute("nicknameError", "이미 사용 중인 닉네임입니다.");
      return "mypage/member_modify";
    }
    if (dto.getUserPassword() != null && !dto.getUserPassword().isBlank()) {
      user.setUserPassword(passwordEncoder.encode(dto.getUserPassword()));
    }
    user.setUserNickname(dto.getUserNickname());
    userRepository.save(user);
    return "redirect:/mypage/member_modify?success";
  }



  @Transactional
  public void deleteUserByEmail(String email) {
    Users user = userRepository.findByUserEmail(email);
    if (user != null) {
      userRepository.delete(user);
    } else {
      throw new IllegalArgumentException("해당 이메일의 사용자가 존재하지 않습니다.");
    }
  }




  @Transactional
  public void updateResume(Long id, ResumeDto dto, MultipartFile imgFile) throws IOException {
    Resume resume = resumeRepository.findById(id)
      .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없습니다."));

    resume.setTitle(dto.getTitle());
    resume.setAddress(dto.getAddress());
    resume.setPhoneNum(dto.getPhoneNum());
    resume.setEducation(dto.getEducation());
    resume.setAbility(dto.getAbility());
    resume.setAntecedents(dto.getAntecedents());
    resume.setAwards(dto.getAwards());
    resume.setContext(dto.getContext());
    resume.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

    // 이미지 업로드 처리
    if (imgFile != null && !imgFile.isEmpty()) {
      String fileName = imgFile.getOriginalFilename();
      String uploadDir = uploadRootPath + "/resume";
      Path uploadPath = Paths.get(uploadDir);
      if (!Files.exists(uploadPath)) {
        Files.createDirectories(uploadPath);
      }
      Path filePath = uploadPath.resolve(fileName);
      imgFile.transferTo(filePath.toFile());
      resume.setImg("/uploads/images/resume/" + fileName);
    }

    // ResumeContents 갱신
    if (resume.getResumeContents() != null) {
      resume.getResumeContents().clear();
    }

    if (dto.getResumeContents() != null) {
      for (ResumeContentDTO contentDto : dto.getResumeContents()) {
        ResumeContent content = ResumeContent.builder()
          .resume(resume)
          .question(contentDto.getQuestion())
          .context(contentDto.getContext())
          .build();
        resume.getResumeContents().add(content);
      }
    }

    resumeRepository.save(resume);
  }


  @Transactional
  public void deleteScrap(FavoriteDTO favoriteDTO) {
    Users user = userRepository.findById(favoriteDTO.getUserIdx())
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

    Recruitment recruitment = recruitmentRepository.findById(favoriteDTO.getRecruitmentIdx())
            .orElseThrow(() -> new IllegalArgumentException("공고를 찾을 수 없습니다."));

    favoriteRepository.deleteByUserAndRecruitment(user, recruitment);
  }

  public List<FavoriteDTO> getFavorites(String userEmail) {
    Users user = userRepository.findByUserEmail(userEmail);
    if (user == null) {
      throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
    }

    List<Favorite> favorites = favoriteRepository.findByUser(user);

    return favorites.stream()
            .map(favorite -> {
              Recruitment r = favorite.getRecruitment();
              if (r == null) return null;
              return FavoriteDTO.builder()
                      .favoriteIdx(favorite.getFavoriteIdx())
                      .userIdx(favorite.getUser().getUserIdx())
                      .recruitmentIdx(r.getRecruitmentIdx())
                      .title(r.getTitle() != null ? r.getTitle() : "제목 없음")
                      .company(r.getCompany() != null ? r.getCompany() : "회사 없음")
                      .deadline(r.getDeadline())
                      .scrapCount(favoriteRepository.countByRecruitment(r))
                      .build();
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
  }

  public Page<FavoriteDTO> getPagedFavorites(String userEmail, Pageable pageable) {
    Users user = userRepository.findByUserEmail(userEmail);
    if (user == null) {
      throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
    }

    Page<Favorite> rawPage = favoriteRepository.findByUser(user, pageable);

    List<FavoriteDTO> dtoList = rawPage.stream()
            .map(favorite -> {
              Recruitment r = favorite.getRecruitment();
              if (r == null) return null;
              return FavoriteDTO.builder()
                      .favoriteIdx(favorite.getFavoriteIdx())
                      .userIdx(favorite.getUser().getUserIdx())
                      .recruitmentIdx(r.getRecruitmentIdx())
                      .title(r.getTitle())
                      .company(r.getCompany())
                      .deadline(r.getDeadline())
                      .scrapCount(favoriteRepository.countByRecruitment(r))
                      .build();
            })
            .filter(Objects::nonNull)
            .toList();

    return new PageImpl<>(dtoList, pageable, rawPage.getTotalElements());
  }

  public List<Resume> getResumesByUserId(Long userIdx) {
    return resumeRepository.findByUser_UserIdx(userIdx);
  }

  public Resume getResumeById(Long resumeIdx) {
    return resumeRepository.findById(resumeIdx)
            .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없습니다."));
  }

  public Page<Resume> getPagedResumesByUserId(Long userIdx, Pageable pageable) {
    return resumeRepository.findByUser_UserIdx(userIdx, pageable);
  }



  @Transactional(readOnly = true)
  public ResumeDto getResumeDetail(Long id) {
    Resume resume = resumeRepository.findById(id)
      .orElseThrow(() -> new IllegalArgumentException("이력서 없음"));

    resume.getUser().getUserName(); // Lazy 로딩 강제

    List<ResumeContentDTO> resumeContentDtos = resume.getResumeContents().stream()
      .map(rc -> ResumeContentDTO.builder()
        .question(rc.getQuestion())
        .context(rc.getContext())
        .build())
      .toList();

    return ResumeDto.builder()
      .title(resume.getTitle())
      .address(resume.getAddress())
      .phoneNum(resume.getPhoneNum())
      .context(resume.getContext())
      .education(resume.getEducation())
      .antecedents(resume.getAntecedents())
      .ability(resume.getAbility())
      .awards(resume.getAwards())
      .img(resume.getImg())
      .createdAt(resume.getCreatedAt())
      .updatedAt(resume.getUpdatedAt())
      .resumeContents(resumeContentDtos)
      .build();
  }


  @Transactional
  public void saveApplyHistoryWithResumeCopy(Users user, Recruitment recruitment, ResumeDto resumeDto) {

    ApplyHistory applyHistory = ApplyHistory.builder()
      .user(user)
      .recruitment(recruitment)
      .status(ApplyHistory.ApplyStatus.SUBMITTED)
      .apply(new Timestamp(System.currentTimeMillis()))
      .build();

    applyHistoryRepository.save(applyHistory);

    ApplyHistoryResume applyHistoryResume = ApplyHistoryResume.builder()
      .applyHistory(applyHistory)
      .user(user)
      .recruitment(recruitment)
      .title(resumeDto.getTitle())
      .context(resumeDto.getContext())
      .img(resumeDto.getImg())
      .address(resumeDto.getAddress())
      .phoneNum(resumeDto.getPhoneNum())
      .education(resumeDto.getEducation())
      .antecedents(resumeDto.getAntecedents())
      .ability(resumeDto.getAbility())
      .awards(resumeDto.getAwards())
      .createdAt(resumeDto.getCreatedAt())
      .updatedAt(resumeDto.getUpdatedAt())
      .build();

    applyHistoryResume.setContents(new ArrayList<>());

    if (resumeDto.getResumeContents() != null) {
      for (ResumeContentDTO contentDto : resumeDto.getResumeContents()) {
        ApplyHistoryResumeContent content = ApplyHistoryResumeContent.builder()
          .applyHistoryResume(applyHistoryResume)
          .question(contentDto.getQuestion())
          .context(contentDto.getContext())
          .build();
        applyHistoryResume.addContent(content);
        for (ApplyHistoryResumeContent c : applyHistoryResume.getContents()) {
          System.out.println("Content question: " + c.getQuestion() + ", applyHistoryResume: " + c.getApplyHistoryResume());
        }
      }
    }

    // *** 핵심: applyHistoryResumeRepository.save 호출 반드시 필요 ***
    applyHistoryResumeRepository.save(applyHistoryResume);

    applyHistory.setApplyHistoryResume(applyHistoryResume);

    // save applyHistory는 선택사항이지만 안정성을 위해 해도 됨
    applyHistoryRepository.save(applyHistory);
  }



  public ApplyHistoryResumeDTO toDto(ApplyHistoryResume entity) {
    if (entity == null) {
      return null;
    }

    Users user = entity.getUser();

    UserListDto userDTO = null;
    if (user != null) {
      userDTO = UserListDto.builder()
        .userName(user.getUserName())
        .userEmail(user.getUserEmail())
        .userBirth(user.getUserBirth())
        .build();
    }

    return ApplyHistoryResumeDTO.builder()
      .id(entity.getApplyHistoryResumeId())
      .title(entity.getTitle())
      .context(entity.getContext())
      .img(entity.getImg())
      .address(entity.getAddress())
      .phoneNum(entity.getPhoneNum())
      .education(entity.getEducation())
      .antecedents(entity.getAntecedents())
      .ability(entity.getAbility())
      .awards(entity.getAwards())
      .createdAt(entity.getCreatedAt())
      .updatedAt(entity.getUpdatedAt())
      .user(userDTO)
      .resumeContents(entity.getContents() != null
        ? entity.getContents().stream()
        .map(content -> ApplyHistoryResumeContentDTO.builder()
          .id(content.getId())
          .question(content.getQuestion())
          .context(content.getContext())
          .build())
        .collect(Collectors.toList())
        : List.of())
      .build();
  }



}
