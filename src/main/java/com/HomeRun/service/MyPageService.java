package com.HomeRun.service;

import com.HomeRun.dto.MyPageDto;
import com.HomeRun.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MyPageService {

    private final UserService userService;
    private final com.HomeRun.repository.NoticeRepository noticeRepository;

    @Value("${app.version:1.0.0}")
    private String appVersion;

    public MyPageDto.Response getMyPageInfo(String email) {
        // UserService를 통해 닉네임 자동 생성이 보장된 User 객체를 가져옵니다.
        User user = userService.getUserEnsureNickname(email);

        return MyPageDto.Response.builder()
                .nickname(user.getNickname())
                .email(user.getEmail())
                .appVersion(appVersion)
                .build();
    }

    public java.util.List<com.HomeRun.dto.mypage.NoticeListResponseDto> getAllNotices() {
        return noticeRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(com.HomeRun.dto.mypage.NoticeListResponseDto::from)
                .collect(java.util.stream.Collectors.toList());
    }

    @org.springframework.transaction.annotation.Transactional
    public com.HomeRun.dto.mypage.NoticeDetailResponseDto getNoticeDetail(Long id) {
        com.HomeRun.entity.Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new com.HomeRun.common.exception.GlobalException(com.HomeRun.common.error.ErrorCode.NOTICE_NOT_FOUND));
        
        notice.incrementViewCount();
        return com.HomeRun.dto.mypage.NoticeDetailResponseDto.from(notice);
    }
}
