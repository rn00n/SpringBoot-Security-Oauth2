package com.web;

import com.web.resolver.UserArgumentResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;

//1.
//UserArgumentResolver 등록하기
//UserArgumentResolver 클래스를 적용하려면 WebMvcConfigurerAdapter 를 상속받아야 한다.
@SpringBootApplication
public class BootWebApplication extends WebMvcConfigurerAdapter { //1. 상속

	public static void main(String[] args) {
		SpringApplication.run(BootWebApplication.class, args);
	}

	//1.
	//UserArgumentResolver 등록하기
	@Autowired
	private UserArgumentResolver userArgumentResolver;

	//1.
	//WebMvcConfigurerAdapter 내부에 addArgumentResolvers() 메서드를 오버라이드하여 UserArgumentResolver를 추가한다.
	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
		argumentResolvers.add(userArgumentResolver);
	}

}
