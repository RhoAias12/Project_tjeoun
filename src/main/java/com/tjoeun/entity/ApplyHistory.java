package com.tjoeun.entity;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

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
    접수, 마감, 합격, 불합격
  }
}

