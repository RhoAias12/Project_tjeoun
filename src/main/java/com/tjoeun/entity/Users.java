package com.tjoeun.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Users {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer userIdx;

  @Column(nullable = false)
  private String userName;

  @Column(nullable = false, unique = true)
  private String userEmail;

  @Column(nullable = false)
  private String userPassword;

  @Column(nullable = false, unique = true)
  private String userNickname;

  @Column(nullable = false)
  private LocalDate userBirth;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private UserRole userRole;

  @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
  private java.sql.Timestamp userCreatedAt;

  public enum UserRole {
    USER, MANAGER
  }
}
