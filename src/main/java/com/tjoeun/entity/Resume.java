package com.tjoeun.entity;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Table(name = "resume")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resume {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long resumeIdx;

  @ManyToOne
  @JoinColumn(name = "user_idx", nullable = false)
  private Users user;

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

