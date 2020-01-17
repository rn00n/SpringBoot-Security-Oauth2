package com.web;

import com.web.repository.UserRepository;
import com.web.resolver.UserArgumentResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;

@SpringBootApplication
public class BootWebApplication extends WebMvcConfigurerAdapter {

	public static void main(String[] args) {
		SpringApplication.run(BootWebApplication.class, args);
	}

	//UserArgumentResolver 등록하기
	//UserArgumentResolver 클래스를 적용하려면 WebMvcConfigurerAdapter 를 상속받아야 한다.
	@Autowired
	private UserArgumentResolver userArgumentResolver;

	//WebMvcConfigurerAdapter 내부에 addArgumentResolvers() 메서드를 오버라이드하여 UserArgumentResolver를 추가한다.
	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
		argumentResolvers.add(userArgumentResolver);
	}

}
