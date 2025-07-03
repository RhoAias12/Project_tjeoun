package com.tjoeun.util;

import org.springframework.data.domain.Page;
import org.springframework.ui.Model;

public class PaginationUtil {

    public static void setPaging(Model model, Page<?> page, String url, String customSort) {
        int currentPage = page.getNumber();         // 0-based 현재 페이지
        int totalPages = page.getTotalPages();      // 전체 페이지 수

        // 페이지 번호 범위 (10개 단위)
        int startPage = (currentPage / 10) * 10;
        int endPage = Math.min(startPage + 9, totalPages - 1);

        // model에 속성 등록
        model.addAttribute("startPage", startPage);
        model.addAttribute("customSort", customSort);
        model.addAttribute("endPage", endPage);
        model.addAttribute("url", url);  // 페이징 링크에 사용할 base URL
    }
}
