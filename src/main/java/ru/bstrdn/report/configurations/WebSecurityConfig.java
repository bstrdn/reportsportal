package ru.bstrdn.report.configurations;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@EnableWebSecurity
@Slf4j
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Qualifier("myUserDetailsService")
    @Autowired
    UserDetailsService userDetailsService;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                //для консоли h2
                .headers().frameOptions().disable()
                .and()
                .authorizeRequests()
                .antMatchers("/admin").hasAuthority("1")
                .antMatchers("/report_1", "/report_2").hasAnyAuthority("1", "2")
                .antMatchers("/report_cert").hasAnyAuthority("1", "3")
                .antMatchers("/login", "/error", "/js/**").permitAll()
                .antMatchers("/**").authenticated()
//                .antMatchers("/**").permitAll()
                .and()
                .formLogin()
                .and().logout()
                //кастомная страница авторизации
                .and().formLogin()
                .loginPage("/login");
    }

    @Bean
    public PasswordEncoder getPasswordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }
}

