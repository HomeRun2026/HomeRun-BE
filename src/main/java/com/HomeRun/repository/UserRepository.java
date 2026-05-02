package com.HomeRun.repository;

import com.HomeRun.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// JpaRepository<다룰 엔티티 클래스, 그 엔티티의 ID 타입>
public interface UserRepository extends JpaRepository<User, Long> {

    // 이메일을 통해 이미 가입된 사용자인지 확인하는 마법의 메서드입니다.
    Optional<User> findByEmail(String email);
}