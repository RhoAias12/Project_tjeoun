package com.tjoeun.util;

import org.springframework.data.domain.Page;
import org.springframework.ui.Model;

public class PaginationUtil2 {

    public static void setPaging(Model model, Page<?> page, String url, String customSort, int visiblePageCount) {
        int currentPage = page.getNumber();         // 0-based 현재 페이지
        int totalPages = page.getTotalPages();      // 전체 페이지 수

        int startPage = Math.max(0, currentPage - visiblePageCount / 2);
        int endPage = Math.min(totalPages - 1, startPage + visiblePageCount - 1);
        startPage = Math.max(0, endPage - visiblePageCount + 1); // 보정

        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("url", url);
        model.addAttribute("customSort", customSort);
    }

    public static void setPaging(Model model, Page<?> page, String url,
                                 String sortOrder, int visiblePageCount,
                                 String title, String content, String region, String company,
                                 String startDate, String endDate) {
        int currentPage = page.getNumber();         // 0-based 현재 페이지
        int totalPages = page.getTotalPages();      // 전체 페이지 수

        int startPage = Math.max(0, currentPage - visiblePageCount / 2);
        int endPage = Math.min(totalPages - 1, startPage + visiblePageCount - 1);
        startPage = Math.max(0, endPage - visiblePageCount + 1); // 보정

        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("url", url);
        model.addAttribute("sortOrder", sortOrder);

        // 검색조건도 추가로 넘김 (페이징 링크에 유지용)
        model.addAttribute("title", title);
        model.addAttribute("content", content);
        model.addAttribute("region", region);
        model.addAttribute("company", company);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
    }



}
