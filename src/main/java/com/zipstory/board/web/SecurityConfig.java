package com.zipstory.board.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	
	@Override
	protected void configure(HttpSecurity httpSecurity) throws Exception {
//		httpSecurity.httpBasic().disable();
		httpSecurity.headers().frameOptions().sameOrigin();
		httpSecurity.cors().and();
		httpSecurity.csrf().disable();
	}
	

}
