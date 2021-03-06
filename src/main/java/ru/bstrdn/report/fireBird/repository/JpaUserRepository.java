package ru.bstrdn.report.fireBird.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.bstrdn.report.fireBird.model.User;

import java.util.Optional;

public interface JpaUserRepository extends JpaRepository<User, Integer> {
Optional<User> findByDname(String dname);
}