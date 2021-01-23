package ru.bstrdn.report.configurations;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ru.bstrdn.report.fireBird.model.User;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class MyUserDetails implements UserDetails {

    private String dname;
    private String dpasswrd;
    private List<GrantedAuthority> authorityDoctcodeList;

    public MyUserDetails(User user) {
        System.out.println(user.getDname() + user.getDpasswrd()
                + user.getDoctcode());
        this.dname = user.getDname();
        this.dpasswrd = user.getDpasswrd();
        this.authorityDoctcodeList = Arrays.stream(user.getDoctcode().trim().split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        System.out.println(authorityDoctcodeList);
        return authorityDoctcodeList;
    }

    @Override
    public String getPassword() {
        System.out.println(dpasswrd);
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
