package com.tjoeun.controller;

import com.tjoeun.dto.UserFormDto;
import com.tjoeun.dto.UserListDto;
import com.tjoeun.service.AdminService;
import jakarta.validation.Valid;

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

    //회원리스트+페이지네이션
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

    //회원상세페이지
    @GetMapping("/member_modify")
    public String memberModify(@RequestParam("userIdx") Integer userIdx, Model model) {
        UserFormDto dto = adminService.getUserFormDtoById(userIdx);
        model.addAttribute("userFormDto", dto);
        model.addAttribute("userIdx", userIdx);
        return "admin/ad_member_modify";
    }

    //회원상세수정
    @PostMapping("/member_modify/{userIdx}")
    public String updateMember(@PathVariable("userIdx") Integer userIdx,
                               @ModelAttribute("userFormDto") @Valid UserFormDto userFormDto,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        if (bindingResult.hasErrors()) {
            return "admin/ad_member_modify";
        }
        try {
            adminService.updateUserInfo(userIdx, userFormDto);
            redirectAttributes.addFlashAttribute("success", true);
            return "redirect:/admin/member_modify?userIdx=" + userIdx + "&success";
        } catch (IllegalStateException e) {
            model.addAttribute("errorMessage", e.getMessage());
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
}
