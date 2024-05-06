package com.contact.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
public class MyConfig {
	
	@Bean
	public UserDetailsService getUserDetailService() {
		return new UserDetailsServiceImpl();
	}
	
	@Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
	
	@Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        
        daoAuthenticationProvider.setUserDetailsService(this.getUserDetailService());
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        
        return daoAuthenticationProvider;
    }
	
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
	    return httpSecurity
	        .csrf(AbstractHttpConfigurer::disable)
	        .authorizeHttpRequests(registry -> {
	            registry.requestMatchers("/admin/**").hasRole("ADMIN");
	            registry.requestMatchers("/user/**").hasAnyRole("USER", "ADMIN");
	            registry.requestMatchers("/**").permitAll(); // Allow access to all other URLs
	        })
	        
	        .formLogin(httpSecurityFormLoginConfigurer->{
                httpSecurityFormLoginConfigurer
                        .loginPage("/signin")
                        .successHandler(new AuthenticationSuccessHandler())
                        .permitAll();
            })
            .build();
	    
	       /* .formLogin(formLogin -> formLogin.permitAll()) // Permit all for login page
	        .build();*/
	}

	
	
	

}
