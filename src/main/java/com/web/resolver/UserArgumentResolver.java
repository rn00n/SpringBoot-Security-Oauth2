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
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpSession;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static com.web.domain.enums.SocialType.FACEBOOK;
import static com.web.domain.enums.SocialType.GOOGLE;
import static com.web.domain.enums.SocialType.KAKAO;

//HandlerMethodArgumentResolver 인터페이스는 두가지 메서드를 제공한다.
//supportsParameter() : HandlerMethodArgumentResolver가 해당하는 파라미터를 지원할지 여부를 반환 (true => resolveArgument()메서드 실행)
//resolveArgument() : 파라미터의 인자값에 대한 정보를 바탕으로 실제 객체를 생성하여 해당 파라미터 객체에 바인딩한다.
@Component
public class UserArgumentResolver implements HandlerMethodArgumentResolver {
    //3.
    private UserRepository userRepository;
    public UserArgumentResolver(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean supportsParameter(MethodParameter parameter) {
        //2.
        return parameter.getParameterAnnotation(SocialUser.class) != null && parameter.getParameterType().equals(User.class);
    }

    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        //2.
        HttpSession session = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest().getSession();
        User user = (User) session.getAttribute("user");
        return getUser(user, session);
    }

    //3.
    private User getUser(User user, HttpSession session) {
        if (user == null) {
            try {
                OAuth2Authentication authentication = (OAuth2Authentication) SecurityContextHolder.getContext().getAuthentication();
                Map<String, String> map = (HashMap<String, String>) authentication.getUserAuthentication().getDetails();
                User convertUser = convertUser(String.valueOf(authentication.getAuthorities().toArray()[0]), map);

                user = userRepository.findByEmail(convertUser.getEmail());
                if (user == null) {
                    user = userRepository.save(convertUser);
                }

                setRoleIfNotSame(user, authentication, map);
                session.setAttribute("user", user);
            }catch (ClassCastException e) {
                return user;
            }
        }
        return user;
    }
    //3.
    private User convertUser(String authority, Map<String, String> map) {
        if(FACEBOOK.isEquals(authority)) return getModernUser(FACEBOOK, map);
        else if(GOOGLE.isEquals(authority)) return getModernUser(GOOGLE, map);
        else if(KAKAO.isEquals(authority)) return getKakaoUser(map);
        return null;
    }
    //3.
    private User getModernUser(SocialType socialType, Map<String, String> map) {
//        return User.builder()
//                .name(map.get("name"))
//                .email(map.get("email"))
//                .principal(map.get("id"))
//                .socialType(socialType)
//                .createdDate(LocalDateTime.now())
//                .build();
        return new User(map.get("name"), null, map.get("email"), map.get("id"), socialType, LocalDateTime.now(), null);
    }
    //3.
    private User getKakaoUser(Map<String, String> map) {
        HashMap<String, String> propertyMap = (HashMap<String, String>)(Object)map.get("properties");
//        return new User.builder()
//                .name(propertyMap.get("nickname"))
//                .email(map.get("kaccount_email"))
//                .principal(String.valueOf(map.get("id")))
//                .socialType(KAKAO)
//                .createdDate(LocalDateTime.now())
//                .build();
        return new User(propertyMap.get("nickname"), null, map.get("kaccount_email"), String.valueOf(map.get("id")), KAKAO, LocalDateTime.now(), null);
    }
    //3.
    private void setRoleIfNotSame(User user, OAuth2Authentication authentication, Map<String, String> map) {
        if(!authentication.getAuthorities().contains(new SimpleGrantedAuthority(user.getSocialType().getRoleType()))) {
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(map, "N/A",
                    AuthorityUtils.createAuthorityList(user.getSocialType().getRoleType())));
        }
    }
}
