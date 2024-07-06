package com.user_auth_org.hng_internship.controller;

import com.user_auth_org.hng_internship.dto.OrgDto;
import com.user_auth_org.hng_internship.dto.OrgRequestDto;
import com.user_auth_org.hng_internship.dto.UserDto;
import com.user_auth_org.hng_internship.service.OrganizationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/organisations")
public class OrganizationController {
    @Autowired
    private OrganizationService organizationService;

    @GetMapping
    public ResponseEntity<?> getAllUserOrganizations() {
        return organizationService.getUserOrganizations();
    }

    @GetMapping("/{orgId}")
    public ResponseEntity<?> getSingleOrganization(@PathVariable Integer orgId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return organizationService.getSingleOrganizationData(orgId, authentication);
    }
    @PostMapping
    public ResponseEntity<?> createOrganization(@Valid @RequestBody OrgDto orgDto ){
        return organizationService.createNewOrganization(orgDto);
    }

    @PostMapping("/{orgId}/users")
    public ResponseEntity<?> addUserToOrganization(@PathVariable Integer orgId, @Valid @RequestBody OrgRequestDto orgRequestDto) {
        return organizationService.addUsersToOrganization(orgId, orgRequestDto.getUserId());
    }

}
