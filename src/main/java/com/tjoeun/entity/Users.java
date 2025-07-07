package com.tjoeun.entity;

import com.tjoeun.constant.UserRole;
import com.tjoeun.dto.UserFormDto;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

  public static Users createUser(UserFormDto dto, PasswordEncoder passwordEncoder) {
    return Users.builder()
      .userName(dto.getUserName())
      .userEmail(dto.getUserEmail())
      .userPassword(passwordEncoder.encode(dto.getUserPassword()))
      .userNickname(dto.getUserNickname())
      .userBirth(dto.getUserBirth())
      .userCreatedAt(new Timestamp(System.currentTimeMillis()))
      .userRole(UserRole.USER)
      .build();
  }

  @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
  private List<Favorite> favorites = new ArrayList<>();

  @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
  private List<Resume> resumes = new ArrayList<>();
}
