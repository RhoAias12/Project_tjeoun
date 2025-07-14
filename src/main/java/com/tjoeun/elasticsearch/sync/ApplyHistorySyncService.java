package com.tjoeun.elasticsearch.sync;

import com.tjoeun.elasticsearch.document.ApplyHistoryDocument;
import com.tjoeun.elasticsearch.repository.ApplyHistorySearchRepository;
import com.tjoeun.entity.ApplyHistory;
import com.tjoeun.entity.ApplyHistoryResume;
import com.tjoeun.entity.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class ApplyHistorySyncService {

  private final ApplyHistorySearchRepository applyHistorySearchRepository;

  public void save(ApplyHistory history) {
    if (history == null || history.getUser() == null || history.getRecruitment() == null) return;

    ApplyHistoryResume resume = history.getApplyHistoryResume();
    Users user = history.getUser();

    ApplyHistoryDocument document = ApplyHistoryDocument.builder()
      .applyHistoryId(history.getOptionalIdx())
      .status(history.getStatus().name())
      .statusDisplay(history.getStatus().getDisplay())

      // 공고 정보
      .recruitmentId(history.getRecruitment().getRecruitmentIdx())
      .recruitmentTitle(history.getRecruitment().getTitle())
      .recruitmentCompany(history.getRecruitment().getCompany())
      .recruitmentDeadline(
        history.getRecruitment().getDeadline()
          .atZone(ZoneId.systemDefault())
          .toInstant()
          .toEpochMilli()
      )
      .recruitmentLocation(history.getRecruitment().getLocation())
      .recruitmentLogoUrl(history.getRecruitment().getLogoUrl())

      // 사용자 정보
      .userId(user.getUserIdx())
      .userEmail(user.getUserEmail())
      .userNickname(user.getUserNickname())
      .userName(user.getUserName())
      .userBirth(user.getUserBirth() != null ?
        user.getUserBirth().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        : null)

      // 이력서 정보 (복사본 기반)
      .applyHistoryResumeId(resume != null ? resume.getApplyHistoryResumeId() : null)
      .resumeTitle(resume != null ? resume.getTitle() : null)
      .resumeImg(resume != null ? resume.getImg() : null)
      .resumeAddress(resume != null ? resume.getAddress() : null)
      .resumePhoneNum(resume != null ? resume.getPhoneNum() : null)
      .resumeEducation(resume != null ? resume.getEducation() : null)
      .resumeAbility(resume != null ? resume.getAbility() : null)
      .resumeAntecedents(resume != null ? resume.getAntecedents() : null)
      .resumeAwards(resume != null ? resume.getAwards() : null)
      .resumeContext(resume != null ? resume.getContext() : null)

      // 지원 시점
      .appliedAt(history.getApply().toLocalDateTime()
        .atZone(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli())
      .build();

    applyHistorySearchRepository.save(document);
  }
}
