package com.tjoeun.entity;

import com.tjoeun.converter.ApplyStatusConverter;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import java.util.Arrays;

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

  @Convert(converter = ApplyStatusConverter.class)
  @Column(nullable = false)
  private ApplyStatus status;


  @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
  private Timestamp apply;

  public enum ApplyStatus {
    SUBMITTED("접수"),
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

    public String toDbValue() {
      return display;
    }

    public static ApplyStatus fromDbValue(String dbValue) {
      return fromDisplay(dbValue);
    }
  }

}
