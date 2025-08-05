package com.gordan.luckydraw.repository;

import com.gordan.luckydraw.enums.ERole;
import com.gordan.luckydraw.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(ERole name);
}
