package ru.bstrdn.report.configurations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.bstrdn.report.fireBird.model.User;
import ru.bstrdn.report.fireBird.repository.JpaUserRepository;
import ru.bstrdn.report.postgres.model.Log;
import ru.bstrdn.report.postgres.repository.LogsRepository;

import java.util.Optional;

@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    JpaUserRepository jpaUserRepository;

//    @Autowired
//    LogsRepository logsRepository;

    @Override
    public UserDetails loadUserByUsername(String dname) throws UsernameNotFoundException {
        Optional<User> user = jpaUserRepository.findByDname(dname);
        user.orElseThrow(() -> new UsernameNotFoundException("Not found: " + dname));

//        logsRepository.save(new Log(dname, "Авторизация"));

        return user.map(MyUserDetails::new).get();
    }
}
