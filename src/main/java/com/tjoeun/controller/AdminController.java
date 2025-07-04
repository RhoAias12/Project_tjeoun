package com.tjoeun.controller;

import com.tjoeun.dto.*;
import com.tjoeun.entity.ApplyHistory;
import com.tjoeun.repository.ApplyHistoryRepository;
import com.tjoeun.service.AdminService;

import com.tjoeun.service.RecruitmentService;
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

import java.util.List;


@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {
    private final AdminService adminService;
    private final RecruitmentService recruitmentService;

    @GetMapping("/member_list")
    public String memberList(@RequestParam(defaultValue = "1") int page,
                             @RequestParam(defaultValue = "latest") String sortBy,
                             @PageableDefault(size = 10) Pageable pageable,
                             Model model) {

        // 정렬은 서비스에 위임
        Page<UserListDto> userPage = adminService.getPagedUsers(page, pageable.getPageSize(), sortBy);

        PaginationUtil.setPaging(model, userPage, "/admin/member_list?sortBy=" + sortBy);

        model.addAttribute("userList", userPage.getContent());
        model.addAttribute("userPage", userPage);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("page", page);

        return "admin/member_list";
    }



    @PostMapping("/member_list/delete")
    public String deleteMember(@RequestParam("userIdx") Integer userIdx,
                               @RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "latest") String sortBy) {
        adminService.deleteUserById(userIdx);
        return "redirect:/admin/member_list?page=" + page + "&sortBy=" + sortBy;
    }


    @GetMapping("/member_modify")
    public String memberModify(@RequestParam("userIdx") Integer userIdx,
                               @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                               @RequestParam(value = "sortBy", required = false, defaultValue = "latest") String sortBy,
                               Model model) {
        UserFormDto dto = adminService.getUserFormDtoById(userIdx);
        model.addAttribute("userFormDto", dto);
        model.addAttribute("userIdx", userIdx);
        model.addAttribute("page", page);
        model.addAttribute("sortBy", sortBy); // <-- 이 부분 꼭 필요
        return "admin/ad_member_modify";
    }



    @PostMapping("/member_modify/{userIdx}")
    public String updateMember(
      @PathVariable("userIdx") Integer userIdx,
      @ModelAttribute("userFormDto") UserFormDto dto,
      BindingResult bindingResult,
      Model model,
      RedirectAttributes redirectAttributes,
      @RequestParam(value = "page", required = false, defaultValue = "1") int page,
      @RequestParam(value = "sortBy", required = false, defaultValue = "latest") String sortBy
    ) {
        try {
            adminService.updateUser(userIdx, dto, bindingResult, model);
            redirectAttributes.addFlashAttribute("successMessage", "회원정보가 성공적으로 수정되었습니다.");
            return "redirect:/admin/member_modify?userIdx=" + userIdx + "&page=" + page + "&sortBy=" + sortBy + "&success";
        } catch (IllegalStateException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("userIdx", userIdx);
            model.addAttribute("page", page);
            model.addAttribute("sortBy", sortBy);
            return "admin/ad_member_modify";
        }
    }



    @GetMapping("/recruit_list")
    public String recruitList(@RequestParam(defaultValue = "1") int page,
                              @RequestParam(defaultValue = "deadline_all") String deadlineSort,
                              @PageableDefault(size = 10) Pageable pageable,
                              Model model) {

        Pageable correctedPageable = PageRequest.of(page - 1, pageable.getPageSize(), pageable.getSort());

        Page<RecruitmentDTO> recruitmentPage =
          recruitmentService.getSortedPagedPosts(correctedPageable, deadlineSort);

        String urlWithParams = "/admin/recruit_list?deadlineSort=" + deadlineSort;
        PaginationUtil.setPaging(model, recruitmentPage, urlWithParams);

        model.addAttribute("recruitmentList", recruitmentPage.getContent());
        model.addAttribute("recruitmentPage", recruitmentPage);
        model.addAttribute("deadlineSort", deadlineSort);
        model.addAttribute("page", page);

        return "admin/recruit_list";
    }

    @GetMapping("/recruit_modify")
    public String recruitModify(@RequestParam("recruitmentIdx") Long recruitmentIdx, Model model) {
        RecruitmentDTO dto = recruitmentService.getPostById(recruitmentIdx);
        model.addAttribute("recruit", dto);
        return "admin/recruit_modify";
    }

    @PostMapping("/recruit_delete")
    public String deleteRecruit(@RequestParam("recruitmentIdx") Long recruitmentIdx,
                                @RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "deadline_all") String deadlineSort) {

        adminService.deleteRecruitmentById(recruitmentIdx);

        int totalCount = adminService.countRecruitments();
        int pageSize = 10;

        int totalPages = (int) Math.ceil((double) totalCount / pageSize);

        int finalPage = Math.min(page, Math.max(totalPages, 1));

        return "redirect:/admin/recruit_list?page=" + finalPage
          + "&deadlineSort=" + deadlineSort;
    }

    @GetMapping("/apply_list")
    public String applyListPage(@RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "10") int size,
                                @RequestParam(defaultValue = "all") String sortOption,
                                Model model) {
        Page<ApplyHistoryDTO> applyPage = adminService.getPagedApplyHistory(page - 1, size, sortOption);
        PaginationUtil.setPaging(model, applyPage, "/admin/apply_list");

        model.addAttribute("applyList", applyPage.getContent());
        model.addAttribute("applyPage", applyPage);
        model.addAttribute("sortOption", sortOption);  // 선택 상태 유지용
        return "admin/apply_list";
    }


    @GetMapping("/apply_detail/{applyHistoryId}")
    public String applyDetail(@PathVariable Integer applyHistoryId, Model model) {
        ApplyDetailDTO detailDTO = adminService.getApplyDetailById(applyHistoryId);
        model.addAttribute("applyDetail", detailDTO);
        model.addAttribute("applyDetailId", applyHistoryId);
        return "admin/apply_detail";
    }

    @PostMapping("/apply_detail/{applyHistoryId}/updateStatus")
    public String updateApplyStatus(@PathVariable Integer applyHistoryId,
                                    @RequestParam("newStatus") String newStatus,
                                    RedirectAttributes redirectAttributes) {


        adminService.updateApplyStatus(applyHistoryId, newStatus);

        String displayStatus = ApplyHistory.ApplyStatus.valueOf(newStatus).getDisplay();
        redirectAttributes.addAttribute("statusChanged", displayStatus);
        return "redirect:/admin/apply_detail/" + applyHistoryId;
    }


}
