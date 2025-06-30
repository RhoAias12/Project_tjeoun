package com.tjoeun.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "favorite")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Favorite {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer favoriteIdx;

  @ManyToOne
  @JoinColumn(name = "user_idx", nullable = false)
  private Users user;

  @ManyToOne
  @JoinColumn(name = "recruitment_idx", nullable = false)
  private Recruitment recruitment;
}
