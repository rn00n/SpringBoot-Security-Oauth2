package com.web.controller;

import com.web.annotation.SocialUser;
import com.web.domain.User;
import com.web.domain.enums.SocialType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Controller
public class LoginController {
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    /* 기존로직
    *  컨트롤러에 불필요한 로직이 많다
    *  페이스북 인증에만 쓸 수 있어서 구글, 카카오는 추가 코드 필요
    //인증이 성공적으로 처리된 이후에 리다이렉트되는 경로.
    //허용하는 요청의 URL 매핑을 /facebook/complete, /google/complete, /kakao/complete로 제한
    @GetMapping(value = "/{facebook|google|kakao}/complete")
    public String loginComplete(HttpSession session) {
        //SecurityContextHolder 에서 인증된 정보를 OAuth2Authentication 형태로 받아온다.
        //OAuth2Authentication 은 기본적인 인증에 대한 정보뿐만 아니라 OAuth2 인증과 관련된 정보도 함께 제공
        OAuth2Authentication authentication = (OAuth2Authentication)
                SecurityContextHolder.getContext().getAuthentication();
        //리소스 서버에서 받아온 개인정보를 getDetails()를 사용해 Map 타입으로 받을 수 있다.
        Map<String, String> map = (HashMap<String, String>)
                authentication.getUserAuthentication().getDetails();
        session.setAttribute("user", User.builder()
                .name(map.get("name"))
                .email(map.get("email"))
                .principal(map.get("id"))
                .socialType(SocialType.FACEBOOK)
                .createdDate(LocalDateTime.now())
                .build() //세션에 빌더를 사용하여 인증된 User 정보를 User 객체로 변환하여 저장
        );
        return "redirect:/board/list";
    }
     */

    //AOP를 이용하여 특정한 파라미터 형식을 취해 병렬적으로 User 객체이 인증된 정보를 가져온다
    //어노테이션을 추가하여 인증된 User 정보를 불러오는 기능을 구현
    @GetMapping(value = "/{facebook|google|kakao}/complete")
    public String loginComplete(@SocialUser User user) {
        return "redirect:/board/list";
    }
    //AOP를 구현하는 방법은 두가지이다
    //하나는 직접 AOP로직을 작성하는 방법
    //또는 스프링전략 인터페이스인 HandlerMethodArgumentResolver 를 사용하는 방법이다.
}
