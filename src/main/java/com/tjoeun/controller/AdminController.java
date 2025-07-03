package com.tjoeun.controller;

import com.tjoeun.dto.ApplyHistoryDTO;
import com.tjoeun.dto.UserFormDto;
import com.tjoeun.dto.UserListDto;
import com.tjoeun.service.AdminService;

import com.tjoeun.util.PaginationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {
    private final AdminService adminService;

    @GetMapping("/member_list")
    public String memberList(@RequestParam(defaultValue = "1") int page,
                             @PageableDefault(size = 10) Pageable pageable,
                             Model model) {

        Pageable correctedPageable = PageRequest.of(page - 1, pageable.getPageSize(), pageable.getSort());
        Page<UserListDto> userPage = adminService.getPagedUsers(correctedPageable);

        com.tjoeun.util.PaginationUtil.setPaging(model, userPage, "/admin/member_list");

        model.addAttribute("userList", userPage.getContent());
        model.addAttribute("userPage", userPage);
        return "admin/member_list";
    }


    @PostMapping("/member_list/delete")
    public String deleteMember(@RequestParam("userIdx") Integer userIdx) {
        adminService.deleteUserById(userIdx);
        return "redirect:/admin/member_list";
    }


    @GetMapping("/member_modify")
    public String memberModify(@RequestParam("userIdx") Integer userIdx,
                               @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                               Model model) {
        UserFormDto dto = adminService.getUserFormDtoById(userIdx);
        model.addAttribute("userFormDto", dto);
        model.addAttribute("userIdx", userIdx);
        model.addAttribute("page", page);
        return "admin/ad_member_modify";
    }


    @PostMapping("/member_modify/{userIdx}")
    public String updateMember(@PathVariable("userIdx") Integer userIdx,
                               @ModelAttribute("userFormDto") UserFormDto dto,
                               BindingResult bindingResult,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        try {
            adminService.updateUser(userIdx, dto, bindingResult, model);
            redirectAttributes.addFlashAttribute("successMessage", "회원정보가 성공적으로 수정되었습니다.");
            return "redirect:/admin/member_modify?userIdx=" + userIdx + "&success";
        } catch (IllegalStateException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("userIdx", userIdx);
            return "admin/ad_member_modify";
        }
    }



    @GetMapping("/recruit_list")
    public String recruitList() {
        return "admin/recruit_list";
    }

    @GetMapping("/recruit_modify")
    public String recruitModify() {
        return "admin/recruit_modify";
    }

    @GetMapping("/apply_list")
    public String applyList(@RequestParam(defaultValue = "1") int page,
                            @RequestParam(defaultValue = "all") String applySort,
                            @RequestParam(defaultValue = "all") String deadlineSort,
                            @PageableDefault(size = 10) Pageable pageable,
                            Model model) {

        Pageable correctedPageable = PageRequest.of(page - 1, pageable.getPageSize(), pageable.getSort());

        Page<ApplyHistoryDTO> applyPage = adminService.getPagedApplyHistory(
          page - 1,
          pageable.getPageSize(),
          applySort,
          deadlineSort
        );

        // 페이징 정보 설정
        PaginationUtil.setPaging(model, applyPage, "/admin/apply_list");

        // 리스트 전달
        model.addAttribute("applyList", applyPage.getContent());
        model.addAttribute("applyPage", applyPage); // 현재 페이지 객체
        return "admin/apply_list";
    }


    @GetMapping("/apply_detail")
    public String applyDetail() {
        return "admin/apply_detail";
    }
}
