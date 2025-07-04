package com.tjoeun.service;

import com.tjoeun.dto.ApplyHistoryDTO;
import com.tjoeun.dto.UserFormDto;
import com.tjoeun.dto.UserListDto;
import com.tjoeun.entity.ApplyHistory;
import com.tjoeun.entity.Users;
import com.tjoeun.repository.ApplyHistoryRepository;
import com.tjoeun.repository.RecruitmentRepository;
import com.tjoeun.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final ApplyHistoryRepository applyHistoryRepository;
  private final RecruitmentRepository recruitmentRepository;

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
      user.getUserEmail(),
      user.getUserNickname()
    ));
  }



  // 회원 삭제
  public void deleteUserById(Integer userIdx) {
    userRepository.deleteById(userIdx);
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
  }

  public Page<ApplyHistoryDTO> getPagedApplyHistory(int page, int size, String applySort, String deadlineSort) {
    Sort sort = Sort.unsorted();

    if ("apply_latest".equals(applySort)) {
      sort = Sort.by(Sort.Direction.DESC, "apply");
    } else if ("apply_oldest".equals(applySort)) {
      sort = Sort.by(Sort.Direction.ASC, "apply");
    }

    if ("deadline_latest".equals(deadlineSort)) {
      Sort deadlineSortObj = Sort.by(Sort.Direction.DESC, "recruitment.deadline");
      sort = sort.isSorted() ? sort.and(deadlineSortObj) : deadlineSortObj;
    } else if ("deadline_oldest".equals(deadlineSort)) {
      Sort deadlineSortObj = Sort.by(Sort.Direction.ASC, "recruitment.deadline");
      sort = sort.isSorted() ? sort.and(deadlineSortObj) : deadlineSortObj;
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
  }

  public int countRecruitments() {
    return (int) recruitmentRepository.count();
  }

}
