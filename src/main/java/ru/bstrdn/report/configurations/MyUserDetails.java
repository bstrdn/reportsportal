package ru.bstrdn.report.configurations;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ru.bstrdn.report.fireBird.model.User;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class MyUserDetails implements UserDetails {

    private String dname;
    private String dpasswrd;
    @Getter
    private int dcode;
    private List<GrantedAuthority> authorityDoctcodeList;

    public MyUserDetails(User user) {
        this.dcode = user.getDcode();
        this.dname = user.getDname();
        this.dpasswrd = user.getDpasswrd();
        this.authorityDoctcodeList = Arrays.stream(user.getDoctcode().trim().split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorityDoctcodeList;
    }

    @Override
    public String getPassword() {
        return dpasswrd;
    }

    @Override
    public String getUsername() {
        return dname;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
