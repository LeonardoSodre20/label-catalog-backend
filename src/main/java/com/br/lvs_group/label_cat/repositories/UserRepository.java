package com.br.lvs_group.label_cat.repositories;

import com.br.lvs_group.label_cat.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
}
