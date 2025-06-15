package com.pfnd.UserService.repository;

import com.pfnd.UserService.model.postgresql.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByUsername(String email);

    Optional<User> findByEmail(String email);

    @Query(value = "SELECT k FROM User k")
    List<User> retrieveAll();

}
