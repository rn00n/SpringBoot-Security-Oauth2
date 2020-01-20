package com.web.oauth;

import com.web.domain.enums.SocialType;
import org.springframework.boot.autoconfigure.security.oauth2.resource.AuthoritiesExtractor;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import java.util.List;
import java.util.Map;

//UserInfoTokenServices을 클래스 커스터마이징
//UserInfoTokenServices은 OAuth2에서 제공하는 클래스이며 User 정보를 얻어오기 위해 소셜 서버와 통신하는 역할을 수행
//이때 URI와 clientId정보가 필요하다
public class UserTokenService extends UserInfoTokenServices {
    //생성자에서 super()를 사용하여 각각의 소셜미디어 정보를 주입한다.
    public UserTokenService(ClientResources resources, SocialType socialType) {
        super(resources.getResource().getUserInfoUri(), resources.getClient().getClientId());
        setAuthoritiesExtractor(new OAuth2AuthoritiesExtractor(socialType));
    }
    //권한 네이밍을 알아서 일괄적으로 처리하도록 설정
    public static class OAuth2AuthoritiesExtractor implements AuthoritiesExtractor {
        private  String socialType;

        public OAuth2AuthoritiesExtractor(SocialType socialType) {
            //권한 생성 방식을 'ROLE_FACEBOOK'으로 하기 위해 SocialType의 getRoleType()메서드를 사용
            this.socialType = socialType.getRoleType();
        }

        @Override
        //AuthoritiesExtractor인터페이스 오버라이드
        //권한을 리스트 형식으로 생성하여 반환하도록 한다.
        public List<GrantedAuthority> extractAuthorities(Map<String, Object> map) {
            return AuthorityUtils.createAuthorityList(this.socialType);
        }
    }
}
