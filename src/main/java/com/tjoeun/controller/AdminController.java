package com.tjoeun.controller;

import com.tjoeun.dto.*;
import com.tjoeun.entity.ApplyHistory;
import com.tjoeun.repository.ApplyHistoryRepository;
import com.tjoeun.service.AdminService;

import com.tjoeun.service.RecruitmentService;
import com.tjoeun.service.UserService;
import com.tjoeun.util.PaginationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.util.List;


@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {
    private final AdminService adminService;
    private final RecruitmentService recruitmentService;
    private final UserService userService;;

    @Value("${upload.path}")
    private String uploadRoot;

//    @GetMapping("/member_list")
//    public String memberList(@RequestParam(defaultValue = "1") int page,
//                             @RequestParam(defaultValue = "latest") String sortBy,
//                             @PageableDefault(size = 10) Pageable pageable,
//                             Model model) {
//
//        // 정렬은 서비스에 위임
//        Page<UserListDto> userPage = adminService.getPagedUsers(page, pageable.getPageSize(), sortBy);
//
//        PaginationUtil.setPaging(model, userPage, "/admin/member_list?sortBy=" + sortBy);
//
//        model.addAttribute("userList", userPage.getContent());
//        model.addAttribute("userPage", userPage);
//        model.addAttribute("sortBy", sortBy);
//        model.addAttribute("page", page);
//
//        return "admin/member_list";
//    }


    @GetMapping("/member_list")
    public String memberList(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "latest") String sortBy,
      @RequestParam(required = false) String email,
      @RequestParam(required = false) String nickname,
      @PageableDefault(size = 10) Pageable pageable,
      Model model) throws IOException {

        Page<UserListDto> userPage;

        if ((email != null && !email.isBlank()) || (nickname != null && !nickname.isBlank())) {
            userPage = adminService.getUsersBySearchFromES(email, nickname, sortBy, page, pageable.getPageSize());
        } else {
            userPage = adminService.getPagedUsers(page, pageable.getPageSize(), sortBy);
        }

        StringBuilder url = new StringBuilder("/admin/member_list?sortBy=" + sortBy);
        if (email != null && !email.isBlank()) url.append("&email=").append(email);
        if (nickname != null && !nickname.isBlank()) url.append("&nickname=").append(nickname);

        PaginationUtil.setPaging(model, userPage, url.toString());

        model.addAttribute("userList", userPage.getContent());
        model.addAttribute("userPage", userPage);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("page", page);

        model.addAttribute("email", email);
        model.addAttribute("nickname", nickname);

        return "admin/member_list";
    }





//    @PostMapping("/member_list/delete")
//    public String deleteMember(@RequestParam("userIdx") Integer userIdx,
//                               @RequestParam(defaultValue = "1") int page,
//                               @RequestParam(defaultValue = "latest") String sortBy) {
//        adminService.deleteUserById(userIdx);
//
//        int totalUsers = adminService.getTotalUserCount();
//        int pageSize = 10;
//        int maxPage = (int) Math.ceil((double) totalUsers / pageSize);
//
//        if (page > maxPage && maxPage > 0) {
//            page = maxPage;
//        }
//
//        return "redirect:/admin/member_list?page=" + page + "&sortBy=" + sortBy;
//    }

    @PostMapping("/member_list/delete")
    public String deleteMember(
      @RequestParam("userIdx") Integer userIdx,
      @RequestParam("page") int page,
      @RequestParam("sortBy") String sortBy,
      @RequestParam(required = false) String email,
      @RequestParam(required = false) String nickname
    ) {
        userService.deleteById(userIdx);

        return "redirect:/admin/member_list?page=" + page +
          "&sortBy=" + sortBy +
          (email != null ? "&email=" + email : "") +
          (nickname != null ? "&nickname=" + nickname : "");
    }


    @GetMapping("/member_modify")
    public String memberModify(@RequestParam("userIdx") Integer userIdx,
                               @RequestParam(value = "page", defaultValue = "1") int page,
                               @RequestParam(value = "sortBy", defaultValue = "latest") String sortBy,
                               @RequestParam(value = "email", required = false) String email,
                               @RequestParam(value = "nickname", required = false) String nickname,
                               Model model) {
        System.out.println("email = " + email);
        System.out.println("nickname = " + nickname);
        UserFormDto dto = adminService.getUserFormDtoById(userIdx);
        model.addAttribute("userFormDto", dto);
        model.addAttribute("userIdx", userIdx);
        model.addAttribute("page", page);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("email", email);
        model.addAttribute("nickname", nickname);
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
      @RequestParam(value = "sortBy", required = false, defaultValue = "latest") String sortBy,
      @RequestParam(value = "email", required = false) String email,
      @RequestParam(value = "nickname", required = false) String nickname
    ) {
        try {
            adminService.updateUser(userIdx, dto, bindingResult, model);
            redirectAttributes.addFlashAttribute("successMessage", "회원정보가 성공적으로 수정되었습니다.");
            return "redirect:/admin/member_modify?userIdx=" + userIdx +
              "&page=" + page +
              "&sortBy=" + sortBy +
              (email != null ? "&email=" + email : "") +
              (nickname != null ? "&nickname=" + nickname : "") +
              "&success";
        } catch (IllegalStateException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("userIdx", userIdx);
            model.addAttribute("page", page);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("email", email);
            model.addAttribute("nickname", nickname);
            return "admin/ad_member_modify";
        }
    }




    @GetMapping("/recruit_list")
    public String recruitList(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "deadline_all") String deadlineSort,
      @RequestParam(required = false) String title,
      @RequestParam(required = false) String content,
      @RequestParam(required = false) String region,
      @RequestParam(required = false) String company,
      @RequestParam(required = false) String startDate,
      @RequestParam(required = false) String endDate,
      @PageableDefault(size = 10) Pageable pageable,
      Model model) throws IOException {

        // ES 기반 검색 호출
        Page<RecruitmentDTO> recruitmentPage = adminService.getFilteredRecruitmentsByEs(
          title, content, region, company, startDate, endDate, deadlineSort, page, pageable.getPageSize()
        );

        StringBuilder urlWithParams = new StringBuilder("/admin/recruit_list?deadlineSort=" + deadlineSort);
        if (title != null && !title.isBlank()) urlWithParams.append("&title=").append(title);
        if (content != null && !content.isBlank()) urlWithParams.append("&content=").append(content);
        if (region != null && !region.isBlank()) urlWithParams.append("&region=").append(region);
        if (company != null && !company.isBlank()) urlWithParams.append("&company=").append(company);
        if (startDate != null && !startDate.isBlank()) urlWithParams.append("&startDate=").append(startDate);
        if (endDate != null && !endDate.isBlank()) urlWithParams.append("&endDate=").append(endDate);

        PaginationUtil.setPaging(model, recruitmentPage, urlWithParams.toString());

        model.addAttribute("recruitmentList", recruitmentPage.getContent());
        model.addAttribute("recruitmentPage", recruitmentPage);
        model.addAttribute("deadlineSort", deadlineSort);
        model.addAttribute("page", page);

        model.addAttribute("title", title);
        model.addAttribute("content", content);
        model.addAttribute("region", region);
        model.addAttribute("company", company);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

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
    public String applyListPage(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "all") String sortOption,
      @RequestParam(required = false) String title,
      @RequestParam(required = false) String company,
      @RequestParam(required = false) String email,
      @RequestParam(required = false) String nickname,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String startDate,
      @RequestParam(required = false) String endDate,
      Model model
    ) throws IOException {
        System.out.println("status: " + status);

        int pageIndex = (page < 1) ? 0 : (page - 1);

        Page<ApplyHistoryDTO> applyPage;

        if ((title != null && !title.isBlank()) ||
          (company != null && !company.isBlank()) ||
          (email != null && !email.isBlank()) ||
          (nickname != null && !nickname.isBlank()) ||
          (status != null && !status.isBlank()) ||
          (startDate != null && !startDate.isBlank()) ||
          (endDate != null && !endDate.isBlank())) {

            applyPage = adminService.searchApplyHistoriesFromES(
              title, company, email, nickname, status, startDate, endDate, pageIndex, size, sortOption
            );

        } else {
            applyPage = adminService.getPagedApplyHistory(pageIndex, size, sortOption);
        }

        StringBuilder url = new StringBuilder("/admin/apply_list?sortOption=" + sortOption);
        if (title != null && !title.isBlank()) url.append("&title=").append(title);
        if (company != null && !company.isBlank()) url.append("&company=").append(company);
        if (email != null && !email.isBlank()) url.append("&email=").append(email);
        if (nickname != null && !nickname.isBlank()) url.append("&nickname=").append(nickname);
        if (status != null && !status.isBlank()) url.append("&status=").append(status);
        if (startDate != null && !startDate.isBlank()) url.append("&startDate=").append(startDate);
        if (endDate != null && !endDate.isBlank()) url.append("&endDate=").append(endDate);

        PaginationUtil.setPaging(model, applyPage, url.toString());

        model.addAttribute("applyList", applyPage.getContent());
        model.addAttribute("applyPage", applyPage);
        model.addAttribute("sortOption", sortOption);

        model.addAttribute("title", title);
        model.addAttribute("company", company);
        model.addAttribute("email", email);
        model.addAttribute("nickname", nickname);
        model.addAttribute("status", status);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "admin/apply_list";
    }


    @GetMapping("/apply_detail/{applyHistoryId}")
    public String applyDetail(@PathVariable Integer applyHistoryId,
                              @RequestParam(value = "page", defaultValue = "1") int page,
                              @RequestParam(value = "sortOption", defaultValue = "all") String sortOption,
                              @RequestParam(required = false) String title,
                              @RequestParam(required = false) String company,
                              @RequestParam(required = false) String email,
                              @RequestParam(required = false) String nickname,
                              @RequestParam(required = false) String status,
                              @RequestParam(required = false) String startDate,
                              @RequestParam(required = false) String endDate,
                              Model model) {

        ApplyDetailDTO applyDetail = adminService.getApplyDetailById(applyHistoryId);
        model.addAttribute("applyDetail", applyDetail);
        model.addAttribute("applyHistoryId", applyHistoryId);
        model.addAttribute("page", page);
        model.addAttribute("sortOption", sortOption);
        model.addAttribute("title", title);
        model.addAttribute("company", company);
        model.addAttribute("email", email);
        model.addAttribute("nickname", nickname);
        model.addAttribute("status", status);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        return "admin/apply_detail";
    }


    @PostMapping("/apply_detail/{applyHistoryId}/updateStatus")
    public String updateApplyStatus(@PathVariable Integer applyHistoryId,
                                    @RequestParam("newStatus") String newStatus,
                                    @RequestParam(value = "page", defaultValue = "1") int page,
                                    @RequestParam(value = "sortOption", defaultValue = "apply_latest") String sortOption,
                                    @RequestParam(required = false) String title,
                                    @RequestParam(required = false) String company,
                                    @RequestParam(required = false) String email,
                                    @RequestParam(required = false) String nickname,
                                    @RequestParam(required = false) String status,
                                    @RequestParam(required = false) String startDate,
                                    @RequestParam(required = false) String endDate,
                                    RedirectAttributes redirectAttributes) throws IOException {

        // 1) 상태 변경 처리
        adminService.updateApplyStatus(applyHistoryId, newStatus);

        // 2) 상태명 가져오기
        String statusDisplay = adminService.getStatusDisplayName(newStatus);

        // 3) 변경 후 다시 현재 조건으로 조회해서 페이징 체크
        int size = 10; // 페이지 사이즈, 실제 사용하는 값과 동일하게 설정 필요
        int pageIndex = page - 1 < 0 ? 0 : page - 1;

        Page<ApplyHistoryDTO> applyPage;

        if ((title != null && !title.isBlank()) ||
          (company != null && !company.isBlank()) ||
          (email != null && !email.isBlank()) ||
          (nickname != null && !nickname.isBlank()) ||
          (status != null && !status.isBlank()) ||
          (startDate != null && !startDate.isBlank()) ||
          (endDate != null && !endDate.isBlank())) {

            applyPage = adminService.searchApplyHistoriesFromES(
              title, company, email, nickname, status, startDate, endDate, pageIndex, size, sortOption
            );
        } else {
            applyPage = adminService.getPagedApplyHistory(pageIndex, size, sortOption);
        }

        // 4) 현재 페이지에 데이터가 없고, page > 1 이면 page를 1 감소
        if (applyPage.isEmpty() && page > 1) {
            page = page - 1;
        }

        // 5) 리다이렉트 시 파라미터 설정
        redirectAttributes.addAttribute("statusChanged", statusDisplay);
        redirectAttributes.addAttribute("page", page);
        redirectAttributes.addAttribute("sortOption", sortOption);

        if (title != null) redirectAttributes.addAttribute("title", title);
        if (company != null) redirectAttributes.addAttribute("company", company);
        if (email != null) redirectAttributes.addAttribute("email", email);
        if (nickname != null) redirectAttributes.addAttribute("nickname", nickname);
        if (status != null) redirectAttributes.addAttribute("status", status);
        if (startDate != null) redirectAttributes.addAttribute("startDate", startDate);
        if (endDate != null) redirectAttributes.addAttribute("endDate", endDate);

        return "redirect:/admin/apply_detail/" + applyHistoryId;
    }


}
