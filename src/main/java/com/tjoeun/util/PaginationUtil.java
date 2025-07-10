package com.tjoeun.util;

import org.springframework.data.domain.Page;
import org.springframework.ui.Model;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class PaginationUtil {

    // 1. 기본 10개 단위 고정 블록 방식 (기존 PaginationUtil 기능 유지)
    public static void setPaging(Model model, Page<?> page, String url) {
        int currentPage = page.getNumber();         // 0-based 현재 페이지
        int totalPages = page.getTotalPages();      // 전체 페이지 수

        int startPage = (currentPage / 10) * 10;
        int endPage = Math.min(startPage + 9, totalPages - 1);

        model.addAttribute("jobPage", page);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("url", url);
    }

    // 3. 검색조건이 포함된 페이징 (기존 PaginationUtil2 유지)
    public static void setPaging(Model model, Page<?> page, String url,
                                 String sortOrder, int visiblePageCount,
                                 String title, String content, String region, String company,
                                 String startDate, String endDate) {
        int currentPage = page.getNumber();         // 0-based 현재 페이지
        int totalPages = page.getTotalPages();      // 전체 페이지 수

        // visiblePageCount는 10 정도를 넘지 않는 선에서 조절 가능
        int startPage = Math.max(0, currentPage - visiblePageCount / 2);
        int endPage = Math.min(totalPages - 1, startPage + visiblePageCount - 1);
        startPage = Math.max(0, endPage - visiblePageCount + 1); // 보정

        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("url", url);
        model.addAttribute("sortOrder", sortOrder);

        model.addAttribute("title", title);
        model.addAttribute("content", content);
        model.addAttribute("region", region);
        model.addAttribute("company", company);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        model.addAttribute("queryParams", buildQueryParams(sortOrder, title, content, region, company, startDate, endDate));
    }

    private static String buildQueryParams(String sortOrder, String title, String content,
                                           String region, String company, String startDate, String endDate) {
        StringBuilder sb = new StringBuilder();
        if (sortOrder != null && !sortOrder.isBlank()) sb.append("&sortOrder=").append(encode(sortOrder));
        if (title != null && !title.isBlank()) sb.append("&title=").append(encode(title));
        if (content != null && !content.isBlank()) sb.append("&content=").append(encode(content));
        if (region != null && !region.isBlank()) sb.append("&region=").append(encode(region));
        if (company != null && !company.isBlank()) sb.append("&company=").append(encode(company));
        if (startDate != null && !startDate.isBlank()) sb.append("&startDate=").append(encode(startDate));
        if (endDate != null && !endDate.isBlank()) sb.append("&endDate=").append(encode(endDate));
        return sb.toString();
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }


}