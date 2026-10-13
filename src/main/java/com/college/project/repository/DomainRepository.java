package com.college.project.repository;

import com.college.project.model.Domain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for Domain entity operations.
 */
@Repository
public interface DomainRepository extends JpaRepository<Domain, Long> {

    Optional<Domain> findByName(String name);

    boolean existsByName(String name);

    List<Domain> findByIsActiveTrue();
}
