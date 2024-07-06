package com.user_auth_org.hng_internship.service;

import com.user_auth_org.hng_internship.dto.OrgDto;
import com.user_auth_org.hng_internship.model.Organization;
import com.user_auth_org.hng_internship.model.User;
import com.user_auth_org.hng_internship.repository.OrganizationRepository;
import com.user_auth_org.hng_internship.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrganizationService {

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private UserRepository userRepository;

    public ResponseEntity<?> getUserOrganizations() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            User user = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));

            List<Map<String, String>> orgList = user.getOrganizations().stream()
                    .map(org -> {
                        Map<String, String> orgMap = new HashMap<>();
                        orgMap.put("orgId", org.getOrgId().toString());
                        orgMap.put("name", org.getName());
                        orgMap.put("description", org.getDescription());
                        return orgMap;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Organizations retrieved successfully");
            response.put("data", Map.of("organisations", orgList));

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "An unexpected error occurred");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<?> getSingleOrganizationData(int orgId, Authentication authentication){
        try {
            User user = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));

            Organization org = user.getOrganizations().stream()
                    .filter(o -> o.getOrgId().equals(orgId))
                    .findFirst()
                    .orElseThrow(() -> new EntityNotFoundException("Organization not found or user not authorized"));

            Map<String, Object> orgData = new HashMap<>();
            orgData.put("orgId", org.getOrgId().toString());
            orgData.put("name", org.getName());
            orgData.put("description", org.getDescription());

            Map<String, Object> response = new HashMap<>();
            response.put("data", orgData);
            response.put("status", "success");
            response.put("message", "Organization retrieved successfully");

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "An unexpected error occurred");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public ResponseEntity<?> createNewOrganization(OrgDto orgDto){
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            System.out.println(authentication);

            User user = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));

            System.out.println(user);
            Organization newOrg = new Organization();
            newOrg.setName(orgDto.getName());
            newOrg.setDescription(orgDto.getDescription());

            /*newOrg = organizationRepository.save(newOrg);

            // Manage bidirectional relationship
            newOrg.addUser(user);

            // organizationRepository.save(newOrg);
            user.addOrganization(newOrg);
            userRepository.save(user);*/

            // Manage bidirectional relationship
            newOrg.addUser(user);
            user.addOrganization(newOrg);

            // Save the new organization
            newOrg = organizationRepository.save(newOrg);

            System.out.println(newOrg);

            Map<String, Object> orgData = new HashMap<>();
            orgData.put("orgId", newOrg.getOrgId().toString());
            orgData.put("name", newOrg.getName());
            orgData.put("description", newOrg.getDescription());

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Organisation created successfully");
            response.put("data", orgData);

            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (EntityNotFoundException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "client error");
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "An unexpected error occurred");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<?> addUsersToOrganization(int orgId, String userId){
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            User currentUser = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));

            Organization org = organizationRepository.findById(orgId)
                    .orElseThrow(() -> new EntityNotFoundException("Organization not found"));

            if (!org.getUsers().contains(currentUser)) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("status", "error");
                errorResponse.put("message", "You don't have permission to add users to this organization");
                return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
            }

            User userToAdd = userRepository.findById(Integer.parseInt(userId))
                    .orElseThrow(() -> new EntityNotFoundException("User to add not found"));

            org.getUsers().add(userToAdd);
            userToAdd.getOrganizations().add(org);

            organizationRepository.save(org);
            userRepository.save(userToAdd);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "User added to organisation successfully");

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "An unexpected error occurred");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
