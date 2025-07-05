package com.tjoeun.controller;

import com.tjoeun.dto.*;
import com.tjoeun.entity.ApplyHistory;
import com.tjoeun.repository.ApplyHistoryRepository;
import com.tjoeun.service.AdminService;

import com.tjoeun.service.RecruitmentService;
import com.tjoeun.util.PaginationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.security.web.csrf.CsrfToken;
import jakarta.servlet.http.HttpServletRequest;

import java.io.File;
import java.io.IOException;


@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {
    private final AdminService adminService;
    private final RecruitmentService recruitmentService;

    @Value("${upload.path}")
    private String uploadRoot;

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

    @GetMapping("/recruit_modify/{recruitmentIdx}")
    public String recruitModify(@PathVariable("recruitmentIdx") Long recruitmentIdx,
                                @RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "deadline_all") String deadlineSort,
                                Model model) {
        RecruitmentDTO dto = recruitmentService.getPostById(recruitmentIdx);
        model.addAttribute("recruit", dto);
        model.addAttribute("page", page);
        model.addAttribute("deadlineSort", deadlineSort);
        return "admin/recruit_modify";
    }

    @PostMapping("/recruit_modify")
    public String modifyRecruit(@ModelAttribute RecruitmentDTO dto,
                                @RequestParam("logo") MultipartFile logoFile,
                                @RequestParam("page") int page,
                                @RequestParam("deadlineSort") String deadlineSort) throws IOException {

        if (!logoFile.isEmpty()) {
            String filename = dto.getCompany() + ".jpg";
            String logoPath = "/logos/" + filename;
            File saveFile = new File(uploadRoot + logoPath);

            saveFile.getParentFile().mkdirs();
            logoFile.transferTo(saveFile);

            // 업로드한 이미지는 /uploads/ 경로로 URL 설정
            dto.setLogoUrl("/uploads" + logoPath);
        }

        adminService.updateRecruitment(dto);

        return "redirect:/admin/recruit_modify/" + dto.getRecruitmentIdx()
          + "?page=" + page + "&deadlineSort=" + deadlineSort;
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
        int pageIndex = (page < 1) ? 0 : (page - 1);

        Page<ApplyHistoryDTO> applyPage = adminService.getPagedApplyHistory(pageIndex, size, sortOption);
        PaginationUtil.setPaging(model, applyPage, "/admin/apply_list");

        model.addAttribute("applyList", applyPage.getContent());
        model.addAttribute("applyPage", applyPage);
        model.addAttribute("sortOption", sortOption);

        return "admin/apply_list";
    }



    // 상세 페이지 - applyDetail 메서드
    @GetMapping("/apply_detail/{applyHistoryId}")
    public String applyDetail(
      @PathVariable Integer applyHistoryId,
      @RequestParam(value = "page", defaultValue = "1") int page,
      @RequestParam(value = "sortOption", defaultValue = "all") String sortOption,
      Model model
    ) {
        ApplyDetailDTO applyDetail = adminService.getApplyDetailById(applyHistoryId);
        model.addAttribute("applyDetail", applyDetail);
        model.addAttribute("applyHistoryId", applyHistoryId);
        model.addAttribute("page", page);
        model.addAttribute("sortOption", sortOption);
        return "admin/apply_detail";
    }

    // 상태 변경 후 리다이렉트 메서드도 파라미터 이름 맞춤
    @PostMapping("/apply_detail/{applyHistoryId}/updateStatus")
    public String updateApplyStatus(@PathVariable Integer applyHistoryId,
                                    @RequestParam("newStatus") String newStatus,
                                    @RequestParam(value = "page", defaultValue = "1") int page,
                                    @RequestParam(value = "sortOption", defaultValue = "apply_latest") String sortOption,
                                    RedirectAttributes redirectAttributes) {
        adminService.updateApplyStatus(applyHistoryId, newStatus);
        redirectAttributes.addAttribute("statusChanged", newStatus);
        redirectAttributes.addAttribute("page", page);
        redirectAttributes.addAttribute("sortOption", sortOption);  // 여기 이름 맞춤
        return "redirect:/admin/apply_detail/" + applyHistoryId;
    }


}
