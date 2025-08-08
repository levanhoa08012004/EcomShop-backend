package com.example.webmuasam.repository;

import com.example.webmuasam.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> , JpaSpecificationExecutor<Role> {
    Page<Role> findAll(Specification<Role> spec, Pageable pageable);
    boolean existsByName(String name);
    Role findByName(String name);
}
