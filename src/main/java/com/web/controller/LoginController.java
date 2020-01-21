package com.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Created by KimYJ on 2017-09-13.
 */
@Controller
public class LoginController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    //AOP를 이용하여 특정한 파라미터 형식을 취해 병렬적으로 User 객체이 인증된 정보를 가져온다
    //어노테이션을 추가하여 인증된 User 정보를 불러오는 기능을 구현
    @GetMapping("/loginSuccess")
    public String loginComplete() {
        return "redirect:/board/list";
    }
}

