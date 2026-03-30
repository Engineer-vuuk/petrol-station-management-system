package com.sadop.energymanagement.repository;

import com.sadop.energymanagement.model.User;
import com.sadop.energymanagement.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // ✅ Find user by email (used for authentication and context)
    Optional<User> findByEmail(String email);

    // ✅ Find all users with role ATTENDANT in a specific branch
    List<User> findByRoleAndBranch_Id(Role role, Long branchId);

    // ✅ Check if a user exists by email (used in registration)
    boolean existsByEmail(String email);

    // ✅ Check if a user exists by phone (used in registration)
    boolean existsByPhone(String phone);
}