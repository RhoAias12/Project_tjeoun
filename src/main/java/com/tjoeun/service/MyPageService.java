package com.tjoeun.service;

import com.tjoeun.dto.*;
import com.tjoeun.entity.*;
import com.tjoeun.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MyPageService {

  private final FavoriteRepository favoriteRepository;
  private final RecruitmentRepository recruitmentRepository;
  private final UserRepository userRepository;
  private final ApplyHistoryRepository applyHistoryRepository;
  private final ResumeRepository resumeRepository;
  private final ResumeContentRepository resumeContentRepository;

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
                    .resumeId(history.getOptionalIdx())
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
      .resumeId(history.getOptionalIdx())
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
  public void updateResume(Long id, ResumeDto resumeDto) {
    Resume resume = resumeRepository.findById(id)
      .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없습니다."));

    // 기본 필드 업데이트
    resume.setTitle(resumeDto.getTitle());
    resume.setAddress(resumeDto.getAddress());
    resume.setPhoneNum(resumeDto.getPhoneNum());
    resume.setEducation(resumeDto.getEducation());
    resume.setAbility(resumeDto.getAbility());
    resume.setAntecedents(resumeDto.getAntecedents());
    resume.setAwards(resumeDto.getAwards());
    resume.setContext(resumeDto.getContext());

    // 기존 질문/답변 삭제 (orphanRemoval)
    resume.getResumeContents().clear();

    // 새 질문/답변 추가 (빈칸 필터링)
    if (resumeDto.getResumeContents() != null) {
      for (ResumeContentDTO dto : resumeDto.getResumeContents()) {
        boolean isQuestionEmpty = dto.getQuestion() == null || dto.getQuestion().trim().isEmpty();
        boolean isContextEmpty = dto.getContext() == null || dto.getContext().trim().isEmpty();
        if (isQuestionEmpty && isContextEmpty) continue;

        ResumeContent content = ResumeContent.builder()
          .question(dto.getQuestion())
          .context(dto.getContext())
          .build();

        // ✅ 양방향 연관관계 반드시 유지
        content.setResume(resume);              // 자식 → 부모
        resume.getResumeContents().add(content); // 부모 → 자식 리스트
      }
    }
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

    // Lazy 필드 강제 로딩
    resume.getUser().getUserName();

    return ResumeDto.builder()
      .title(resume.getTitle())
      .address(resume.getAddress())
      .phoneNum(resume.getPhoneNum())
      // 필요한 필드 다 넣기
      .build();
  }


}
