package com.web.config;

import com.web.domain.enums.SocialType;
import com.web.oauth.ClientResources;
import com.web.oauth.UserTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.filter.CompositeFilter;

import javax.servlet.Filter;
import java.util.ArrayList;
import java.util.List;

import static com.web.domain.enums.SocialType.FACEBOOK;
import static com.web.domain.enums.SocialType.GOOGLE;
import static com.web.domain.enums.SocialType.KAKAO;

//1. 소셜미디어 리소스 정보를 빈으로 등록
//2. 시큐리티 설정
//3. OAuth2 설정

@Configuration
@EnableWebSecurity //2. 웹에서 시큐리티 기능을 사용하겠다는 어노테이션
@EnableOAuth2Client //3. 웹에서 OAuth2 기능을 사용하겠다는 어노테이션
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    //3.
    @Autowired
    private OAuth2ClientContext oAuth2ClientContext;

//    2.
//    자동설정 그대로 사용할 수도 있지만
//    요청, 권한, 기타 설정에 대해서 필수적으로 최적화한 설정이 들어가야 한다.
//    WebSecurityConfigurerAdapter 를 상속받고 configure() 메서드를 오버라이드 하여 원하는 형식의 시큐리티 설정을 한다.
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        CharacterEncodingFilter filter = new CharacterEncodingFilter();
        http
                .authorizeRequests() //인증 메커니즘을 요청한 HttpServleetRequest 기반으로 설정한다.
                    .antMatchers("/",
                            "/login/**",
                            "/css/**",
                            "/images/**",
                            "/js/**",
                            "/console/**") //요청 패턴을 리스트 형식으로 설정한다.
                    .permitAll() //설정한 리퀘스트 패턴을 누구나 접근할 수 있도록 허용한다.
                    .anyRequest() //설정한 요청 이외의 리퀘스트 요청을 표현합니다.
                    .authenticated() //해당 요청은 인증된 사용자만 할 수 있다.
                .and()
                    .headers() //응답에 해당하는 header를 설정한다. (설정하지않으면 디폴드값)
                    .frameOptions().disable() //XFrameOptionsHeaderWriter의 최적화 설정을 허용하지 않는다.
                .and()
                    .exceptionHandling()
                    .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"))
                //authenticationEntryPoint() : 인증의 진입 지점. 인증되지 않은 사용자가 접근할 경우 '/login'으로 이동
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
                    .addFilterBefore(filter, CsrfFilter.class)
                //첫 번째 인자보다 먼저 시작될 필터를 등록, 문자 인코딩 필터(filter)보다 CsrfFilter를 먼저 실행하도록 설정
                    .addFilterBefore(oauth2Filter(), BasicAuthenticationFilter.class)
                //3.
                    .csrf().disable();
    }
//    3.
    @Bean
//    OAuth2 클라이언트용 시큐리티 필터인 OAuth2ClientContextFilter를 불러와서
//    올바른 순서로 필터가 동작하도록 설정. 스프링 시큐리티 필터가 실행되기 전에 충분히 낮은 순서로 필터를 등록
    public FilterRegistrationBean oauth2ClientFilterRegistration(OAuth2ClientContextFilter filter) {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(filter);
        registration.setOrder(-100);
        return registration;
    }
//    oauth2Filter()메서드는 오버로드 하여 두개가 정의
//    오버로드2. 각소셜 미디어 필터를 리스트 형식으로 한꺼번에 설정하여 반환
    private Filter oauth2Filter() {
        CompositeFilter filter = new CompositeFilter();
        List<Filter> filters = new ArrayList<>();
        filters.add(oauth2Filter(facebook(), "/login/facebook", FACEBOOK));
        filters.add(oauth2Filter(google(), "/login/google", GOOGLE));
        filters.add(oauth2Filter(kakao(), "/login/kakao", KAKAO));
        filter.setFilters(filters);
        return filter;
    }
//    오버로드1. 각 소셜 미디어 타입을 받아서 필터 설정을 할 수 있다.
    private Filter oauth2Filter(ClientResources client, String path, SocialType socialType) {
        //인증이 수행될 경로를 넣어 OAuth2 클라이언트용 인증 처리 필터를 생성
        OAuth2ClientAuthenticationProcessingFilter filter = new OAuth2ClientAuthenticationProcessingFilter(path);
        //권한 서버와의 통신을 위해 OAuth2RestTemplate를 생성한다.
        // 이를 생성하기 위해선 client 프로퍼티 정보와 OAuth2ClientContext가 필요하다
        OAuth2RestTemplate template = new OAuth2RestTemplate(client.getClient(), oAuth2ClientContext);
        filter.setRestTemplate(template);
        //User의 권한을 최적화해서 생성하고자 UserInfoTokenServices를 상속받은 UserTokenService를 생성한다.
        //OAuth2 AccessToken 검증을 위해 상성한 UserTokenService를 필터의 토큰 서비스로 등록한다.
        filter.setTokenServices(new UserTokenService(client, socialType));
        //인증이 성공적으로 이루어지면 필터에 리다이렉트될 URL을 설정
        filter.setAuthenticationSuccessHandler((request, response, authentication)
            -> response.sendRedirect("/" + socialType.getValue() + "/complete"));
        //인증이 실패하면 필터에 리다이렉트될 URL을 설정
        filter.setAuthenticationFailureHandler((request, response, exception) ->
                response.sendRedirect("/error"));
        return filter;
    }

//    1.
//    시큐리티 설정에서 리소스 정보를 사용하기때문에 빈으로 등록
//    @ConfigurationProperties접두사를 사용하여 바인딩
//    해당 어노테이션이 없으면 일일이 프로퍼티값을 불러와야한다
    @Bean
    @ConfigurationProperties("facebook")
    public ClientResources facebook() {
        return new ClientResources();
    }
    @Bean
    @ConfigurationProperties("google")
    public ClientResources google() {
        return new ClientResources();
    }
    @Bean
    @ConfigurationProperties("kakao")
    public ClientResources kakao() {
        return new ClientResources();
    }
}
