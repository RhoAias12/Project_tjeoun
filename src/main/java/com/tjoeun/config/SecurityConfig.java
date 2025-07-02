package com.tjoeun.config;

import com.tjoeun.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final UserService userService;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http
      .csrf(csrf -> csrf
        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
      )
      .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class)
      .authorizeHttpRequests(auth -> auth
        .requestMatchers("/css/**", "/js/**", "/images/**", "/static/**").permitAll()
        .requestMatchers("/", "/user/signup", "/user/login", "/api/user/**", "/user/login/error").permitAll()
        .requestMatchers("/empl/empl_main", "/empl/empl_detail/**").permitAll()
        .requestMatchers("/admin/**").hasRole("ADMIN")
        .anyRequest().authenticated()
      )
      .formLogin(form -> form
        .loginPage("/user/login")
        .defaultSuccessUrl("/", true)
        .usernameParameter("userEmail") // <- dto에 맞춰서
        .failureHandler(new FormLoginAuthenticationFailureHandler())
      )
      .logout(logout -> logout
        .logoutRequestMatcher(new AntPathRequestMatcher("/user/logout"))
        .logoutSuccessUrl("/")
      )
      .build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
