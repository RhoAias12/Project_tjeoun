package com.tjoeun.entity;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import java.util.Arrays;

@Data
@Entity
@Table(name = "apply_history")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplyHistory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer optionalIdx;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "resume_idx")
  private Resume resume;

  @ManyToOne
  @JoinColumn(name = "user_idx", nullable = false)
  private Users user;

  @ManyToOne
  @JoinColumn(name = "recruitment_idx", nullable = false)
  private Recruitment recruitment;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ApplyStatus status;

  @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
  private Timestamp apply;

  public enum ApplyStatus {
    SUBMITTED("진행중"),
    FINALIZED("합격"),
    REJECTED("불합격");

    private final String display;

    ApplyStatus(String display) {
      this.display = display;
    }

    public String getDisplay() {
      return display;
    }

    public static ApplyStatus fromDisplay(String display) {
      return Arrays.stream(values())
              .filter(v -> v.display.equals(display))
              .findFirst()
              .orElseThrow(() -> new IllegalArgumentException("Unknown display value: " + display));
    }
  }
}