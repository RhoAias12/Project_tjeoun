package com.tjoeun.entity;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import java.util.List;

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

  @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ResumeContent> resumeContents;

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

}
