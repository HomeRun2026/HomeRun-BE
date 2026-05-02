package com.HomeRun.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor // 롬복: 기본 생성자를 자동으로 만들어줌
@Table(name = "users")

public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // MySQL의 Auto Increment를 사용하여 ID를 1씩 자동 증가시킵니다.
    private Long id;

    @Column(nullable = false, unique = true) // 이메일은 필수이며, 중복X
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String role;

    @Builder
    public User(String email, String name, String role) {
        this.email = email;
        this.name = name;
        this.role = role;
    }

}
