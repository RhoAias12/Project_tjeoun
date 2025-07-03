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
    //여기부터 추가

    @GetMapping("/ad_member_modify/{userIdx}")
    public String memberModify(@RequestParam("userIdx") Integer userIdx, Model model) {
        UserFormDto dto = adminService.getUserFormDtoById(userIdx);
        model.addAttribute("userFormDto", dto);
        model.addAttribute("userIdx", userIdx);
        return "admin/ad_member_modify";
    }

    @PostMapping("/ad_member_modify/{userIdx}")
    public String updateMember(@PathVariable("userIdx") Integer userIdx,
                               @ModelAttribute("userFormDto") UserFormDto dto,
                               BindingResult bindingResult,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        try {
            adminService.updateUser(userIdx, dto, bindingResult, model);
            return "redirect:/admin/ad_member_modify?userIdx=" + userIdx + "&success";
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
    public String applyList() {
        return "admin/apply_list";
    }

    @GetMapping("/apply_detail")
    public String applyDetail() {
        return "admin/apply_detail";
    }
}
