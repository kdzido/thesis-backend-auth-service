package com.kdzido.thesis.auth.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * @author krzysztof.dzido@gmail.com
 */

@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    @Override
    public UserDetailsService userDetailsServiceBean() throws Exception {
        return super.userDetailsServiceBean();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
//        super.configure(http);
        http.authorizeRequests()
                .antMatchers("/user").permitAll()
                .anyRequest().authenticated();
    }

    @Override
    protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
        // TODO persistency
        auth.inMemoryAuthentication()
                .withUser("reader")
                .password("readerpassword")
                .roles("READER")
            .and()
                .withUser("admin")
                .password("adminpassword")
                .roles("USER", "ADMIN");
    }
}
