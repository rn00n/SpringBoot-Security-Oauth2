package com.web.config;

import com.web.oauth.CustomOAuth2Provider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.web.domain.enums.SocialType.FACEBOOK;
import static com.web.domain.enums.SocialType.GOOGLE;
import static com.web.domain.enums.SocialType.KAKAO;

//1. 소셜미디어 리소스 정보를 빈으로 등록
//2. 시큐리티 설정
//3. OAuth2 설정
//4. 페이지 권한 분리
//5. spring boot 1.5.x OAuth2설정 삭제
//6. spring boot 2.0 OAuth2 설정
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

//    2.
//    자동설정 그대로 사용할 수도 있지만
//    요청, 권한, 기타 설정에 대해서 필수적으로 최적화한 설정이 들어가야 한다.
//    WebSecurityConfigurerAdapter 를 상속받고 configure() 메서드를 오버라이드 하여 원하는 형식의 시큐리티 설정을 한다.
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        CharacterEncodingFilter filter = new CharacterEncodingFilter();
        http

            .authorizeRequests()//인증 메커니즘을 요청한 HttpServleetRequest 기반으로 설정한다.
                .antMatchers("/", "/oauth2/**", "/login/**",  "/css/**", "/images/**", "/js/**", "/console/**")//요청 패턴을 리스트 형식으로 설정한다.
                .permitAll()
                //4.
                //각 소셜 미디어용 경로를 지정한다
                //hasAuthority() 메서드의 파라키터로 원하는 권한을 전달하여 해당 권한을 지닌 사용자만 경로를 사용할 수 있도록 통제
                .antMatchers("/facebook").hasAuthority(FACEBOOK.getRoleType())
                .antMatchers("/google").hasAuthority(GOOGLE.getRoleType())
                .antMatchers("/kakao").hasAuthority(KAKAO.getRoleType())
                .anyRequest() //설정한 요청 이외의 리퀘스트 요청을 표현합니다.
                .authenticated() //해당 요청은 인증된 사용자만 할 수 있다.
            .and() //6.
                .oauth2Login()
                .defaultSuccessUrl("/loginSuccess")
                .failureUrl("/loginFailure")
            .and()
                .headers() //응답에 해당하는 header를 설정한다. (설정하지않으면 디폴드값)
                .frameOptions().disable() //XFrameOptionsHeaderWriter의 최적화 설정을 허용하지 않는다.
            .and()
                .exceptionHandling()
                //authenticationEntryPoint() : 인증의 진입 지점. 인증되지 않은 사용자가 접근할 경우 '/login'으로 이동
                .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"))
            .and()
                .formLogin()
                .successForwardUrl("/board/list") //로그인에 성공하면 설정도니 경로로 포워딩된다.
            .and()
                .logout() //로그아웃에 대한 설정
                .logoutUrl("/logout") //로그아웃이 수행될 URL
                .logoutSuccessUrl("/") //성공시 포워딩될 URL
                .deleteCookies("JSESSIONID") //로그아웃 성공시 쿠키 삭제
                .invalidateHttpSession(true) //설정된 세션 무효화
            .and()
                //첫 번째 인자보다 먼저 시작될 필터를 등록, 문자 인코딩 필터(filter)보다 CsrfFilter를 먼저 실행하도록 설정
                .addFilterBefore(filter, CsrfFilter.class)
                .csrf().disable();
    }

    //6.
    @Bean
    public ClientRegistrationRepository clientRegistrationRepository(OAuth2ClientProperties oAuth2ClientProperties, @Value("${custom.oauth2.kakao.client-id}") String kakaoClientId) {
        List<ClientRegistration> registrations = oAuth2ClientProperties.getRegistration().keySet().stream()
                .map(client -> getRegistration(oAuth2ClientProperties, client))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        registrations.add(CustomOAuth2Provider.KAKAO.getBuilder("kakao")
                .clientId(kakaoClientId)
                .clientSecret("test") //필요없는 값인데 null이면 실행이 안되도록 설정되어 있음
                .jwkSetUri("test") //필요없는 값인데 null이면 실행이 안되도록 설정되어 있음
                .build());

        return new InMemoryClientRegistrationRepository(registrations);
    }

    private ClientRegistration getRegistration(OAuth2ClientProperties clientProperties, String client) {
        if ("google".equals(client)) {
            OAuth2ClientProperties.Registration registration = clientProperties.getRegistration().get("google");
            return CommonOAuth2Provider.GOOGLE.getBuilder(client)
                    .clientId(registration.getClientId())
                    .clientSecret(registration.getClientSecret())
                    .scope("email", "profile")
                    .build();
        }
        if ("facebook".equals(client)) {
            OAuth2ClientProperties.Registration registration = clientProperties.getRegistration().get("facebook");
            return CommonOAuth2Provider.FACEBOOK.getBuilder(client)
                    .clientId(registration.getClientId())
                    .clientSecret(registration.getClientSecret())
                    .userInfoUri("https://graph.facebook.com/me?fields=id,name,email,link")
                    .scope("email")
                    .build();
        }
        return null;
    }
}