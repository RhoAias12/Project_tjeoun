package com.tjoeun.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import java.io.IOException;

public class CsrfCookieFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {

    // 요청에서 CSRF 토큰을 꺼냄
    CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());

    if (csrfToken != null) {
      Cookie cookie = WebUtils.getCookie(request, "XSRF-TOKEN");
      String token = csrfToken.getToken();
      if (cookie == null || token != null && !token.equals(cookie.getValue())) {
        cookie = new Cookie("XSRF-TOKEN", token);
        cookie.setPath("/");
        cookie.setHttpOnly(false);  // JS에서 읽을 수 있도록 설정
        response.addCookie(cookie);
      }
    }

    filterChain.doFilter(request, response);
  }
}
