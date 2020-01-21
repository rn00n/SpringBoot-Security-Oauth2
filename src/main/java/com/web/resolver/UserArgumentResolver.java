package com.web.resolver;

import com.web.annotation.SocialUser;
import com.web.domain.User;
import com.web.domain.enums.SocialType;
import com.web.repository.UserRepository;

import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import static com.web.domain.enums.SocialType.FACEBOOK;
import static com.web.domain.enums.SocialType.GOOGLE;
import static com.web.domain.enums.SocialType.KAKAO;

//1.
//HandlerMethodArgumentResolver 인터페이스를 구현한 UserArgumentResolver 클래스 생성
//HandlerMethodArgumentResolver 인터페이스는 두가지 메서드를 제공한다.
//supportsParameter() : HandlerMethodArgumentResolver가 해당하는 파라미터를 지원할지 여부를 반환 (true => resolveArgument()메서드 실행)
//resolveArgument() : 파라미터의 인자값에 대한 정보를 바탕으로 실제 객체를 생성하여 해당 파라미터 객체에 바인딩한다.
//2.
//@SocialUser.class 를 명시
//supportsParameter() 메서드에 해당하는 어노테이션 타입이 명시되어잇는지 확인하는 로직 추가
//3.
//세션에서 User 객체를 가져오는 resolveArgument() 메서드 구현
//4.
//인증된 소셜 미디어 회원의 정보를 가져와 User 객체 만들기
@Component
public class UserArgumentResolver implements HandlerMethodArgumentResolver {
    //.
    private UserRepository userRepository;

    public UserArgumentResolver(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    //2.
    //MethodParameter 로 파라미터의 정보를 받는다
    //처음 한 번 체크된 부분은 캐시되어 이후의 동일호출 시에 체크되지 않고 캐시된 결과값을 바로 반환
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        //2.
        //@SocialUser.class 를 명시
        //파라미터에 @SocialUser 어노테이션이 있고 타입이 User인 파라미터만 true를 반환
        return parameter.getParameterAnnotation(SocialUser.class) != null && parameter.getParameterType().equals(User.class);
    }

    //3.
    //resolveArgument() 메서드는 검증이 완료된 파라미터 정보를 받는다
    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        //3.
        //이미 검증이 되어 세션에 해당 User 객체가 있으면 User 객체를 구성하는 로직을 수행하지 않도록 세션을 먼저 확인하는 로직
        //세션은 RequestContextholder 를 사용해서 가져올 수 있다.
        //---
        //세션에 인증된 User 객체를 가져온다
        HttpSession session = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest().getSession();
        User user = (User) session.getAttribute("user");
        return getUser(user, session);
    }

    //4.
    //인증된 User 객체를 만드는 메인 메서드
    //세션에서 가져온 User 객체가 없으면 새로생성하고 있으면 바로 사용하도록 반환한다.
    //getUser() 메서드는 인증되 User 객체를 만들어 권한까지 부여하는 코드이므로 (3.)과 분리
    //또한 각 소셜 미디어마다 다른 네이밍 방식을 취하고 있다.
    private User getUser(User user, HttpSession session) {
        //4. 세션에 User 객체가 없을 때 만 로직이 수행
        if(user == null) {
            try {
                //SecurityContextHolder 를 사용해 인증된 OAuth2Authentication 객체를 가져온다
                OAuth2AuthenticationToken authentication = (OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
                //가져온 OAuth2Authentication 객체에서 getDetails() 메서드를 사용해 개인정보를 Map 타입으로 매핑한다.
                Map<String, Object> map = authentication.getPrincipal().getAttributes();
                //convertUser() 메서드에 어떤 소셜 미디어로 인증을 받았는지
                //=> String.valueOf(authentication.getAuthorities().toArray()[0]) 로 불러온다.
                //*이 예제 에서는 이전에 넣어주었던 권한이 하나뿐이라서 배열의 첫 번째 값만 불러오도록 작성하였다
                User convertUser = convertUser(authentication.getAuthorizedClientRegistrationId(), map);

                //여기서는 소셜에 항상 이메일 정보를 제공한다는 조건하에 작성한다.
                user = userRepository.findByEmail(convertUser.getEmail());
                //이메일을 사용해 이미 DB에 저장된 사용자라면 바로 User 객체를 반환하고
                if (user == null) {
                    //, 저장되지 않은 사용자라면 User 테이블에 저장하는 로직을 수행
                    user = userRepository.save(convertUser);
                }

                setRoleIfNotSame(user, authentication, map);
                session.setAttribute("user", user);
            } catch (ClassCastException e) {
                return user;
            }
        }
        return user;
    }

    //4.
    //사용자의 인증된 소셜 미디어 타입에 따라 빌더를 사요하여 User 객체를 만들어 주는 가교 역할을 한다.
    //카카오톡은 별도의 메서드를 사용
    private User convertUser(String authority, Map<String, Object> map) {
        if(FACEBOOK.isEquals(authority)) return getModernUser(FACEBOOK, map);
        else if(GOOGLE.isEquals(authority)) return getModernUser(GOOGLE, map);
        else if(KAKAO.isEquals(authority)) return getKaKaoUser(map);
        return null;
    }

    //4.
    //페이스북과 구글의 명명규칙을 가진 그룹을 User 객체로 매핑
    private User getModernUser(SocialType socialType, Map<String, Object> map) {
        return User.builder()
                .name(String.valueOf(map.get("name")))
                .email(String.valueOf(map.get("email")))
                .pincipal(String.valueOf(map.get("id")))
                .socialType(socialType)
                .createdDate(LocalDateTime.now())
                .build();
    }

    //4.
    //명명규칙이 다른 카카오톡을 User 객체로 매핑
    private User getKaKaoUser(Map<String, Object> map) {
        Map<String, String> propertyMap = (HashMap<String, String>) map.get("properties");
        return User.builder()
                .name(propertyMap.get("nickname"))
                .email(String.valueOf(map.get("kaccount_email")))
                .pincipal(String.valueOf(map.get("id")))
                .socialType(KAKAO)
                .createdDate(LocalDateTime.now())
                .build();
    }

    //4.
    //인증된 authentication 이 권한을 갖고 있는지 체크하는 용도로 쓰인다.
    //만약 저장된 User 권한이 없으면 SecurityContextHolder 를 사용하여 해당 소셜 미디어 타입으로 권한을 저장
    private void setRoleIfNotSame(User user, OAuth2AuthenticationToken authentication, Map<String, Object> map) {
        if(!authentication.getAuthorities().contains(new SimpleGrantedAuthority(user.getSocialType().getRoleType()))) {
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(map, "N/A", AuthorityUtils.createAuthorityList(user.getSocialType().getRoleType())));
        }
    }
}