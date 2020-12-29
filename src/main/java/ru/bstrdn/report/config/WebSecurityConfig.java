package ru.bstrdn.report.config;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import javax.servlet.http.Cookie;

@Configuration
@EnableWebSecurity
@Slf4j
@AllArgsConstructor
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        PasswordEncoder encoder =
                PasswordEncoderFactories.createDelegatingPasswordEncoder();
        auth
                .inMemoryAuthentication()
                .withUser("kononova")
                .password(encoder.encode("1"))
                .roles("USER")
                .and()
                .withUser("admin")
                .password(encoder.encode("1"))
                .roles("USER", "ADMIN");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeRequests()
//                .antMatchers("/rest/**").hasRole("ADMIN")
//                .antMatchers("/").authenticated()
                .antMatchers("/login.html").anonymous()
                .anyRequest()
                .authenticated()
                .and()
                .formLogin()
        .and().logout()
                .logoutUrl("/logout")
        //кастомная страница авторизации
//                .and().formLogin()
//                .loginPage("/login")
//                .permitAll();
        ;

    }

//    @Override
//    protected void configure(AuthenticationManagerBuilder auth)
//            throws Exception {
//        auth.inMemoryAuthentication()
//                .withUser("1")
//                .password("{noop}1")
//                .roles("USER");
//    }

//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        http.headers().frameOptions().disable();
//        http.authorizeRequests()
////                .antMatchers("/rest/admin/**").hasRole(Role.ADMIN.name())
////                .antMatchers("/rest/profile/register").anonymous()
////                .antMatchers("/**").authenticated()
//                .antMatchers("/**").anonymous()
////                .antMatchers("/rest/**").authenticated()
//                .and().httpBasic()
//                .and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//                .and().csrf().disable();
//
//    }
}

