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
                                 String scrapSort, String deadlineSort, int visiblePageCount) {
        int currentPage = page.getNumber();         // 0-based 현재 페이지
        int totalPages = page.getTotalPages();      // 전체 페이지 수

        int startPage = Math.max(0, currentPage - visiblePageCount / 2);
        int endPage = Math.min(totalPages - 1, startPage + visiblePageCount - 1);
        startPage = Math.max(0, endPage - visiblePageCount + 1); // 보정

        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("url", url);
        model.addAttribute("scrapSort", scrapSort);
        model.addAttribute("deadlineSort", deadlineSort);
    }


}
