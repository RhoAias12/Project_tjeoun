package com.tjoeun.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter
public class UserFormDto {

  @NotBlank(message = "이름은 필수 입력 값입니다.")
  @Size(min = 2, max = 10, message = "이름은 2~10글자 사이로 입력해주세요.")
  @Pattern(regexp = "^[가-힣a-zA-Z]+$", message = "이름은 특수문자와 숫자를 포함할 수 없습니다.")
  private String userName;

  @NotBlank(message = "이메일은 필수 입력 값입니다.")
  @Email(message = "이메일 형식으로 입력해주세요.")
  private String userEmail;

  @NotBlank(message = "닉네임은 필수 입력 값입니다.")
  @Size(min = 2, max = 20, message = "닉네임은 2~20글자 사이로 입력해주세요.")
  private String userNickname;

  @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
  @Size(min = 4, max = 16, message = "비밀번호는 4~16글자 사이로 입력해주세요.")
  @Pattern(regexp = "^[a-zA-Z0-9]*$", message = "비밀번호는 한글과 특수문자를 포함할 수 없습니다.")
  private String userPassword;

  @NotNull(message = "생년월일은 필수 입력 값입니다.")
  private LocalDate userBirth;
}
