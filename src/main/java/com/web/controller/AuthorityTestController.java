package com.web.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

//권한에 따른 페이지 구성
//간단하게 RestController로 만들어서 경로만 받고 해당하는 경로의 소셜 미디어 타입값만 반환하도록 처리
@RestController
public class AuthorityTestController {

    @GetMapping("/facebook")
    public String facebook() {
        return "facebook";
    }

    @GetMapping("/google")
    public String google() {
        return "google";
    }

    @GetMapping("/kakao")
    public String kakao() {
        return "kakao";
    }
}
