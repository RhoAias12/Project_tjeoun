package com.tjoeun.entity;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Table(name = "resumeContent")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeContent {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer resumeContentIdx;

  @ManyToOne
  @JoinColumn(name = "resume_idx", nullable = false)
  private Resume resume;

  @Column(columnDefinition = "TEXT")
  private String question;

  @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
  private Timestamp createdAt;

  @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
  private Timestamp updatedAt;

  @Column(columnDefinition = "TEXT")
  private String context;
}
