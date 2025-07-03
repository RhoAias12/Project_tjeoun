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

    String uri = request.getRequestURI();

    // ✅ 정적 리소스 요청이면 쿠키 설정 스킵
    if (isStaticResource(uri)) {
      filterChain.doFilter(request, response);
      return;
    }

    // ✅ 일반 요청에 대해서만 CSRF 토큰을 쿠키로 설정
    CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());

    if (csrfToken != null) {
      String token = csrfToken.getToken();
      Cookie cookie = WebUtils.getCookie(request, "XSRF-TOKEN");

      if (cookie == null || (token != null && !token.equals(cookie.getValue()))) {
        cookie = new Cookie("XSRF-TOKEN", token);
        cookie.setPath("/");
        cookie.setHttpOnly(false); // JS에서 읽을 수 있도록
        response.addCookie(cookie);
      }
    }

    filterChain.doFilter(request, response);
  }

  // ✅ 정적 리소스 판별 메서드
  private boolean isStaticResource(String uri) {
    return uri.startsWith("/css")
            || uri.startsWith("/js")
            || uri.startsWith("/images")
            || uri.startsWith("/static")
            || uri.endsWith(".css")
            || uri.endsWith(".js")
            || uri.endsWith(".png")
            || uri.endsWith(".jpg")
            || uri.endsWith(".jpeg")
            || uri.endsWith(".gif")
            || uri.endsWith(".svg");
  }
}
