package com.user_auth_org.hng_internship.repository;

import com.user_auth_org.hng_internship.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrganizationRepository  extends JpaRepository<Organization, Integer> {
    List<Organization> findByUsersUserId(Long userId);
}
