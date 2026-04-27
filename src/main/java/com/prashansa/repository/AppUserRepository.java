package com.prashansa.repository;

import com.prashansa.entity.AppUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, String> {

    Optional<AppUser> findByPhone(String phone);

    long countByRoleIn(java.util.List<String> roles);
}
