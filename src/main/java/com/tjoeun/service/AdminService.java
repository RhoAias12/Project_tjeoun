package com.tjoeun.service;

import com.tjoeun.dto.*;
import com.tjoeun.elasticsearch.document.ApplyHistoryDocument;
import com.tjoeun.elasticsearch.document.RecruitmentDocument;
import com.tjoeun.elasticsearch.document.UserDocument;
import com.tjoeun.elasticsearch.repository.UserDocumentRepository;
import com.tjoeun.entity.*;
import com.tjoeun.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import com.tjoeun.specification.RecruitmentSpecification;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final ApplyHistoryRepository applyHistoryRepository;
  private final RecruitmentRepository recruitmentRepository;
  private final RecruitmentSearchService recruitmentSearchService;
  private final UserSearchService userSearchService;
  private final UserDocumentRepository userDocumentRepository;
  private final ApplyHistorySearchService applyHistorySearchService;

  DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

  // 모든 회원 리스트 조회
  public Page<UserListDto> getPagedUsers(int page, int size, String sortBy) {
    Sort sort;
    switch (sortBy) {
      case "latest":
        sort = Sort.by(Sort.Direction.DESC, "userIdx");
        break;
      case "oldest":
        sort = Sort.by(Sort.Direction.ASC, "userIdx");
        break;
      case "email":
        sort = Sort.by(Sort.Direction.ASC, "userEmail");
        break;
      case "emailDesc":
        sort = Sort.by(Sort.Direction.DESC, "userEmail");
        break;
      case "nickname":
        sort = Sort.by(Sort.Direction.ASC, "userNickname");
        break;
      case "nicknameDesc":
        sort = Sort.by(Sort.Direction.DESC, "userNickname");
        break;
      default:
        sort = Sort.by(Sort.Direction.DESC, "userIdx");
        break;
    }
    Pageable pageable = PageRequest.of(page - 1, size, sort);
    Page<Users> userPage = userRepository.findAll(pageable);
    return userPage.map(user -> new UserListDto(
      user.getUserIdx(),
      user.getUserName(),
      user.getUserEmail(),
      user.getUserBirth(),
      user.getUserNickname()
    ));
  }


  // 회원 삭제
  @Transactional
  public void deleteUserById(Integer userIdx) {
    Users user = userRepository.findById(userIdx)
      .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
    userRepository.delete(user);
    userDocumentRepository.deleteById(userIdx);
  }
  public int getTotalUserCount() {
    return (int) userRepository.count();
  }

  // 회원 상세 조회
  public UserFormDto getUserFormDtoById(Integer userIdx) {
    Users user = userRepository.findById(userIdx)
      .orElseThrow(() -> new IllegalArgumentException("해당 회원을 찾을 수 없습니다."));
    UserFormDto dto = new UserFormDto();

    dto.setUserName(user.getUserName());
    dto.setUserEmail(user.getUserEmail());
    dto.setUserNickname(user.getUserNickname());
    dto.setUserBirth(user.getUserBirth());
    dto.setUserRole(user.getUserRole());
    dto.setUserPassword("");
    return dto;
  }


  @Transactional
  public void updateUser(Integer userIdx, UserFormDto dto, BindingResult bindingResult, Model model) {
    Users user = userRepository.findById(userIdx)
      .orElseThrow(() -> new IllegalArgumentException("해당 회원을 찾을 수 없습니다."));

    if (!user.getUserNickname().equals(dto.getUserNickname())) {
      if (userRepository.findByUserNickname(dto.getUserNickname()) != null) {
        model.addAttribute("nicknameError", "이미 사용 중인 닉네임입니다.");
        throw new IllegalStateException("이미 사용 중인 닉네임입니다.");
      }
      user.setUserNickname(dto.getUserNickname());
    }

    if (dto.getUserPassword() != null && !dto.getUserPassword().isBlank()) {
      String pw = dto.getUserPassword();
      if (pw.length() < 8 || pw.length() > 16 || !pw.matches("^[a-zA-Z0-9]*$")) {
        model.addAttribute("errorMessage", "비밀번호는 8~16자의 영문+숫자 조합이어야 하며 특수문자와 한글은 안 됩니다.");
        throw new IllegalStateException("비밀번호 유효성 검사 실패");
      }
      user.setUserPassword(passwordEncoder.encode(pw));
    }

    if (dto.getUserRole() != null) {
      user.setUserRole(dto.getUserRole());
    }

    userRepository.save(user);

    UserDocument userDoc = UserDocument.builder()
      .userIdx(user.getUserIdx())
      .userName(user.getUserName())
      .userEmail(user.getUserEmail())
      .userNickname(user.getUserNickname())
      .userBirth(user.getUserBirth().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
      .userRole(user.getUserRole().toString())
      .userCreatedAt(user.getUserCreatedAt())
      .build();

    userDocumentRepository.save(userDoc);
  }

  public Page<ApplyHistoryDTO> getPagedApplyHistory(int page, int size, String sortOption) {
    Sort sort = Sort.unsorted();

    switch (sortOption) {
      case "apply_latest":
        sort = Sort.by(Sort.Direction.DESC, "apply");
        break;
      case "apply_oldest":
        sort = Sort.by(Sort.Direction.ASC, "apply");
        break;
      case "deadline_latest":
        sort = Sort.by(Sort.Direction.DESC, "recruitment.deadline");
        break;
      case "deadline_oldest":
        sort = Sort.by(Sort.Direction.ASC, "recruitment.deadline");
        break;
    }

    Pageable pageable = PageRequest.of(page, size, sort);
    Page<ApplyHistory> applyHistoryPage = applyHistoryRepository.findAll(pageable);

    return applyHistoryPage.map(applyHistory -> ApplyHistoryDTO.builder()
      .applyHistoryId(applyHistory.getOptionalIdx())
      .statusDisplay(applyHistory.getStatus().getDisplay())
      .recruitmentTitle(applyHistory.getRecruitment().getTitle())
      .recruitmentCompany(applyHistory.getRecruitment().getCompany())
      .recruitmentId(applyHistory.getRecruitment().getRecruitmentIdx())
      .userEmail(applyHistory.getUser().getUserEmail())
      .userNickname(applyHistory.getUser().getUserNickname())
      .build());
  }

  public void deleteRecruitmentById(Long recruitmentIdx) {
    recruitmentRepository.deleteById(recruitmentIdx);
    try {
      recruitmentSearchService.deleteById(recruitmentIdx);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public int countRecruitments() {
    return (int) recruitmentRepository.count();
  }



  @Transactional(readOnly = true)
  public ApplyDetailDTO getApplyDetailById(Integer applyHistoryId) {
    ApplyHistory applyHistory = applyHistoryRepository.findById(applyHistoryId)
      .orElseThrow(() -> new IllegalArgumentException("해당 지원 기록을 찾을 수 없습니다. id=" + applyHistoryId));

    Recruitment recruitment = applyHistory.getRecruitment();
    ApplyHistoryResume resume = applyHistory.getApplyHistoryResume();
    Users user = applyHistory.getUser();

    List<ResumeContentDTO> resumeContentList = resume.getContents().stream()
      .map(content -> ResumeContentDTO.builder()
        .question(content.getQuestion())
        .context(content.getContext())
        .build())
      .collect(Collectors.toList());

    return ApplyDetailDTO.builder()
      .recruitmentTitle(recruitment.getTitle())
      .recruitmentCompany(recruitment.getCompany())
      .recruitmentDeadline(LocalDate.from(recruitment.getDeadline()))
      .recruitmentLocation(recruitment.getLocation())
      .recruitmentLogoUrl(recruitment.getLogoUrl())

      .resumeTitle(resume.getTitle())
      .resumeImg(resume.getImg())
      .resumeAddress(resume.getAddress())
      .resumePhoneNum(resume.getPhoneNum())
      .resumeEducation(resume.getEducation())
      .resumeAbility(resume.getAbility())
      .resumeAntecedents(resume.getAntecedents())
      .resumeAwards(resume.getAwards())
      .resumeContext(resume.getContext())

      .userName(user.getUserName())
      .userEmail(user.getUserEmail())
      .userBirth(user.getUserBirth())

      .resumeContentList(resumeContentList)
      .statusDisplay(applyHistory.getStatus().getDisplay())
      .build();
  }



  @Transactional
  public void updateApplyStatus(Integer applyHistoryId, String newStatus) {
    ApplyHistory applyHistory = applyHistoryRepository.findById(applyHistoryId)
      .orElseThrow(() -> new IllegalArgumentException("지원 내역을 찾을 수 없습니다."));

    ApplyHistory.ApplyStatus status = ApplyHistory.ApplyStatus.valueOf(newStatus);
    applyHistory.setStatus(status);
    applyHistoryRepository.save(applyHistory);
    applyHistorySearchService.updateStatusInES(applyHistoryId, status);
  }


  @Transactional
  public void updateRecruitment(RecruitmentDTO dto) {
    Recruitment entity = recruitmentRepository.findById(dto.getRecruitmentIdx())
      .orElseThrow(() -> new IllegalArgumentException("해당 공고가 존재하지 않습니다."));

    entity.setTitle(dto.getTitle());
    entity.setCompany(dto.getCompany());
    entity.setDeadline(dto.getDeadline());
    entity.setQualifications(dto.getQualifications());
    entity.setLogoUrl(dto.getLogoUrl());
    entity.setResponsibilities(dto.getResponsibilities());
    entity.setPreferred(dto.getPreferred());
    entity.setBenefits(dto.getBenefits());
    entity.setLocation(dto.getLocation());
    entity.setSalary(dto.getSalary());
    entity.setEmploymentType(dto.getEmploymentType());

    recruitmentRepository.save(entity);

    RecruitmentDocument doc = RecruitmentDocument.builder()
      .recruitmentIdx(entity.getRecruitmentIdx())
      .title(entity.getTitle())
      .company(entity.getCompany())
      .deadline(entity.getDeadline() != null ? entity.getDeadline().format(formatter) : null)
      .qualifications(entity.getQualifications())
      .logoUrl(entity.getLogoUrl())
      .responsibilities(entity.getResponsibilities())
      .preferred(entity.getPreferred())
      .benefits(entity.getBenefits())
      .location(entity.getLocation())
      .salary(entity.getSalary())
      .employmentType(entity.getEmploymentType())
      .build();

    try {
      recruitmentSearchService.saveOrUpdate(doc);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public String getStatusDisplayName(String status) {
    return switch (status) {
      case "SUBMITTED" -> "진행중";
      case "FINALIZED" -> "합격";
      case "REJECTED"  -> "불합격";
      default -> "알 수 없음";
    };
  }

  @Transactional(readOnly = true)
  public List<RecruitmentDTO> searchAndSort(String keyword, String deadlineSort, Long userIdx) throws IOException {
    List<RecruitmentDocument> documents = recruitmentSearchService.search(keyword, userIdx);

    // RecruitmentDocument → RecruitmentDTO 변환
    List<RecruitmentDTO> dtoList = documents.stream()
      .map(doc -> RecruitmentDTO.builder()
        .recruitmentIdx(doc.getRecruitmentIdx())
        .title(doc.getTitle())
        .company(doc.getCompany())
        .deadline(doc.getDeadline())
        .location(doc.getLocation())
        .logoUrl(doc.getLogoUrl())
        .build())
      .collect(Collectors.toList());

    // 정렬 로직
    return sortRecruitmentDTOList(dtoList, deadlineSort);
  }

  @Transactional(readOnly = true)
  public List<RecruitmentDTO> getSortedRecruitments(String deadlineSort) {
    List<Recruitment> recruitments = recruitmentRepository.findAll();

    List<RecruitmentDTO> dtoList = recruitments.stream()
      .map(r -> RecruitmentDTO.builder()
        .recruitmentIdx(r.getRecruitmentIdx())
        .title(r.getTitle())
        .company(r.getCompany())
        .deadline(r.getDeadline())
        .location(r.getLocation())
        .logoUrl(r.getLogoUrl())
        .build())
      .collect(Collectors.toList());

    return sortRecruitmentDTOList(dtoList, deadlineSort);
  }

  private List<RecruitmentDTO> sortRecruitmentDTOList(List<RecruitmentDTO> list, String deadlineSort) {
    return switch (deadlineSort) {
      case "deadline_latest" -> list.stream()
        .sorted((a, b) -> b.getDeadline().compareTo(a.getDeadline()))
        .collect(Collectors.toList());

      case "deadline_oldest" -> list.stream()
        .sorted((a, b) -> a.getDeadline().compareTo(b.getDeadline()))
        .collect(Collectors.toList());

      default -> list;
    };
  }

  public Page<RecruitmentDTO> searchAndSortWithFilter(
    String title,
    String content,
    String region,
    String company,
    String startDate,
    String endDate,
    String deadlineSort,
    int page,
    int size
  ) {
    Specification<Recruitment> spec = RecruitmentSpecification.searchWithFilter(title, content, region, company, startDate, endDate);

    Sort sort;
    if ("deadline_desc".equals(deadlineSort)) {
      sort = Sort.by(Sort.Direction.DESC, "deadline");
    } else if ("deadline_asc".equals(deadlineSort)) {
      sort = Sort.by(Sort.Direction.ASC, "deadline");
    } else {
      sort = Sort.unsorted();
    }

    int currentPage = Math.max(page - 1, 0);
    Pageable pageable = PageRequest.of(currentPage, size, sort);

    Page<Recruitment> recruitmentPage = recruitmentRepository.findAll(spec, pageable);

    return recruitmentPage.map(RecruitmentDTO::new);
  }

  public Page<RecruitmentDTO> getFilteredRecruitmentsByEs(
    String title,
    String content,
    String region,
    String company,
    String startDate,
    String endDate,
    String deadlineSort,
    int page,
    int size) throws IOException {

    // recruitmentSearchService.searchJobs 메서드가 이미 있음
    Page<RecruitmentDocument> esPage = recruitmentSearchService.searchJobs(
      title, content, region, company, startDate, endDate, deadlineSort, page, size);

    List<RecruitmentDTO> dtoList = esPage.getContent().stream()
      .map(doc -> RecruitmentDTO.builder()
        .recruitmentIdx(doc.getRecruitmentIdx())
        .title(doc.getTitle())
        .company(doc.getCompany())
        .deadline(doc.getDeadline() != null ? LocalDateTime.parse(doc.getDeadline(), formatter) : null)
        .scrapCount(doc.getScrapCount() != null ? doc.getScrapCount() : 0)
        .logoUrl(doc.getLogoUrl())
        .build())
      .toList();

    return new PageImpl<>(dtoList, esPage.getPageable(), esPage.getTotalElements());
  }


  @Transactional(readOnly = true)
  public Page<UserListDto> getUsersBySearchFromES(
    String email,
    String nickname,
    String sortBy,
    int page,
    int size
  ) throws IOException {

    Page<UserListDto> esPage = userSearchService.searchUsers(email, nickname, sortBy, page, size);

    List<UserListDto> dtoList = esPage.getContent().stream()
      .map(doc -> UserListDto.builder()
        .userIdx(doc.getUserIdx())
        .userName(doc.getUserName())
        .userEmail(doc.getUserEmail())
        .userNickname(doc.getUserNickname())
        .userBirth(doc.getUserBirth())
        .build())
      .toList();

    return new PageImpl<>(dtoList, esPage.getPageable(), esPage.getTotalElements());
  }

  @Transactional(readOnly = true)
  public Page<ApplyHistoryDTO> searchApplyHistoriesFromES(
    String recruitmentTitle,
    String recruitmentCompany,
    String userEmail,
    String userNickname,
    String status,
    String startDate,
    String endDate,
    int page,
    int size,
    String sortOption
  ) throws IOException {

    Page<ApplyHistoryDocument> esPage = applyHistorySearchService.searchApplyHistories(
      recruitmentTitle, recruitmentCompany, userEmail, userNickname, status, startDate, endDate, page, size, sortOption
    );

    List<ApplyHistoryDTO> dtoList = esPage.getContent().stream()
      .map(doc -> ApplyHistoryDTO.builder()
        .applyHistoryId(doc.getApplyHistoryId())
        .statusDisplay(getStatusDisplayName(doc.getStatus()))
        .status(doc.getStatus())
        .recruitmentTitle(doc.getRecruitmentTitle())
        .recruitmentCompany(doc.getRecruitmentCompany())
        .userEmail(doc.getUserEmail())
        .userNickname(doc.getUserNickname())
        .build())
      .collect(Collectors.toList());

    return new PageImpl<>(dtoList, esPage.getPageable(), esPage.getTotalElements());
  }



}
