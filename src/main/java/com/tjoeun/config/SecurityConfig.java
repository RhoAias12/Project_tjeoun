package com.tjoeun.config;

import com.tjoeun.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
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
                        .ignoringRequestMatchers("/api/chat")
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/static/**").permitAll()
                        .requestMatchers("/", "/user/signup", "/user/login", "/api/user/**", "/user/login/error").permitAll()
                        .requestMatchers("/empl/empl_main", "/empl/empl_detail/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/mypage/**").authenticated()
                        .requestMatchers("/api/chat").authenticated()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(handler -> handler
                        .authenticationEntryPoint((request, response, authException) -> {
                            // API 요청일 때는 403 + JSON 응답
                            String uri = request.getRequestURI();
                            if (uri.startsWith("/api/")) {
                                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                                response.setContentType("application/json;charset=UTF-8");
                                response.getWriter().write("{\"error\":\"로그인이 필요합니다.\"}");
                            } else {
                                // 나머지는 기존처럼 로그인 페이지로 리다이렉트
                                response.sendRedirect("/user/login");
                            }
                        })
                )
                .formLogin(form -> form
                        .loginPage("/user/login")
                        .defaultSuccessUrl("/", true)
                        .usernameParameter("userEmail")
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