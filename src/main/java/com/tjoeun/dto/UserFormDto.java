package com.tjoeun.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter
public class UserFormDto {

  @NotBlank(message = "이름은 필수 입력 값입니다.")
  private String userName;

  @NotEmpty(message = "이메일은 필수 입력 값입니다.")
  @Email(message = "이메일 형식으로 입력해주세요.")
  private String userEmail;

  @NotEmpty(message = "비밀번호는 필수 입력 값입니다.")
  @Size(min=8, max=16, message = "비밀번호는 8자 이상, 16자 이하로 입력해주세요.")
  private String userPassword;

  @NotEmpty(message = "닉네임은 필수 입력 값입니다.")
  private String userNickname;

  @NotNull(message = "생년월일은 필수 입력 값입니다.")
  private LocalDate userBirth;
}
