package com.tjoeun.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Value("${upload.path}")
  private String uploadPath;

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // 기존 static 폴더 내 이미지 서빙 (내부 리소스)
    registry.addResourceHandler("/images/**")
      .addResourceLocations("classpath:/static/images/");

    // 외부 업로드 이미지 서빙
    String externalPath = uploadPath;
    if (!externalPath.endsWith("/")) {
      externalPath += "/";
    }
    registry.addResourceHandler("/uploads/**")
      .addResourceLocations("file:///" + externalPath);
  }
}