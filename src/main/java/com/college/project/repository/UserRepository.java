package com.college.project.repository;

import com.college.project.model.Role;
import com.college.project.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for User entity operations.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @org.springframework.data.jpa.repository.Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.rollOrEmpId = :rollOrEmpId")
    boolean existsByRollOrEmpId(@org.springframework.data.repository.query.Param("rollOrEmpId") String rollOrEmpId);

    List<User> findByRole(Role role);

    List<User> findByDepartment(String department);
}
