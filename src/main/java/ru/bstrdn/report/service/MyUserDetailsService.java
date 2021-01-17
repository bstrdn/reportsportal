package ru.bstrdn.report.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.bstrdn.report.config.MyUserDetails;
import ru.bstrdn.report.model.User;
import ru.bstrdn.report.repository.JpaUserRepository;

import java.util.Optional;

@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    JpaUserRepository jpaUserRepository;

    @Override
    public UserDetails loadUserByUsername(String dname) throws UsernameNotFoundException {
        Optional<User> user = jpaUserRepository.findByDname(dname);

        user.orElseThrow(() -> new UsernameNotFoundException("Not found: " + dname));

        return user.map(MyUserDetails::new).get();
    }
}
