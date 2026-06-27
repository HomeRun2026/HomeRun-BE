package com.HomeRun.service;

import com.HomeRun.entity.User;
import com.HomeRun.repository.UserRepository;
import com.HomeRun.util.NicknameGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public User getUserEnsureNickname(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 닉네임이 없으면 새로 생성 후 저장
        if (user.getNickname() == null || user.getNickname().trim().isEmpty()) {
            user.updateNickname(NicknameGenerator.generate());
            // @Transactional 덕분에 자동 감지(더티 체킹)되어 DB에 업데이트됩니다.
        }

        return user;
    }

    @Transactional
    public void deleteUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        userRepository.delete(user);
    }
}
