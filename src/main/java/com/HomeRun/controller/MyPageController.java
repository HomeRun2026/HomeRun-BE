package com.HomeRun.controller;

import com.HomeRun.dto.MyPageDto;
import com.HomeRun.service.MyPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageService myPageService;

    @GetMapping
    public MyPageDto.Response getMyPageInfo(Principal principal) {
        if (principal == null) {
            throw new IllegalArgumentException("인증 정보가 없습니다.");
        }
        String email = principal.getName();
        return myPageService.getMyPageInfo(email);
    }

    @GetMapping("/notices")
    public com.HomeRun.common.response.ApiResponse<java.util.List<com.HomeRun.dto.mypage.NoticeListResponseDto>> getNoticeList() {
        return com.HomeRun.common.response.ApiResponse.success(myPageService.getAllNotices());
    }

    @GetMapping("/notices/{id}")
    public com.HomeRun.common.response.ApiResponse<com.HomeRun.dto.mypage.NoticeDetailResponseDto> getNoticeDetail(
            @org.springframework.web.bind.annotation.PathVariable Long id) {
        return com.HomeRun.common.response.ApiResponse.success(myPageService.getNoticeDetail(id));
    }
}
