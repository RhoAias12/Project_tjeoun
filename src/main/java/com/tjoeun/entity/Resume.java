package com.tjoeun.entity;

import jakarta.persistence.*;
import lombok.*;
import java.sql.Timestamp;

@Entity
@Table(name = "resume")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resume {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long resumeIdx;

  /** 연관 사용자 (필수) **/
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_idx", nullable = false)
  private Users user;

  /** 기본 필드 **/
  private String title;

  @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
  private Timestamp createdAt;

  @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
  private Timestamp updatedAt;

  @Column(columnDefinition = "TEXT")
  private String context;

  private String img;
  private String address;
  private String phoneNum;

  @Column(columnDefinition = "TEXT")
  private String education;

  @Column(columnDefinition = "TEXT")
  private String antecedents;

  @Column(columnDefinition = "TEXT")
  private String ability;

  @Column(columnDefinition = "TEXT")
  private String awards;

  /** 질문 / 답변 **/
  @Column(columnDefinition = "TEXT")
  private String question1;

  @Column(columnDefinition = "TEXT")
  private String answer1;

  @Column(columnDefinition = "TEXT")
  private String question2;

  @Column(columnDefinition = "TEXT")
  private String answer2;

  @Column(columnDefinition = "TEXT")
  private String question3;

  @Column(columnDefinition = "TEXT")
  private String answer3;

  @Column(columnDefinition = "TEXT")
  private String question4;

  @Column(columnDefinition = "TEXT")
  private String answer4;

  /** 생성/업데이트 자동 관리 **/
  @PrePersist
  public void onCreate() {
    this.createdAt = new Timestamp(System.currentTimeMillis());
    this.updatedAt = this.createdAt;
  }

  @PreUpdate
  public void onUpdate() {
    this.updatedAt = new Timestamp(System.currentTimeMillis());
  }
}
