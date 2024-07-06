package com.user_auth_org.hng_internship;

/*import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.user_auth_org.hng_internship.exception.UnauthorizedAccessException;
import com.user_auth_org.hng_internship.model.Organization;
import com.user_auth_org.hng_internship.model.User;
import com.user_auth_org.hng_internship.repository.OrganizationRepository;
import com.user_auth_org.hng_internship.repository.UserRepository;
import com.user_auth_org.hng_internship.service.OrganizationService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

public class OrganizationServiceTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrganizationService organizationService;

    public OrganizationServiceTest() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testUserCannotAccessUnauthorizedOrganization() {
        User user = new User();
        user.setUserId(1);

        Organization org = new Organization();
        org.setOrgId(2);

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(organizationRepository.findById(2)).thenReturn(Optional.of(org));

        Exception exception = assertThrows(UnauthorizedAccessException.class, () -> {
            organizationService.getSingleOrganizationData(1);
        });

        String expectedMessage = "User not authorized to access this organization";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }
}*/

import com.user_auth_org.hng_internship.model.Organization;
import com.user_auth_org.hng_internship.model.User;
import com.user_auth_org.hng_internship.repository.OrganizationRepository;
import com.user_auth_org.hng_internship.repository.UserRepository;
import com.user_auth_org.hng_internship.service.OrganizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class OrganizationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private OrganizationService organizationService;

    @BeforeEach
    public void setup() {
       MockitoAnnotations.initMocks(this);
    }

    // Test case 1: User has access to the organization
    @Test
    public void testGetSingleOrganizationData_UserHasAccess() {
        User mockUser = new User();
        mockUser.setUserId(1);
        mockUser.setEmail("user@example.com");
        mockUser.setFirstname("John");

        Organization mockOrg = new Organization(mockUser.getFirstname());
        mockOrg.setOrgId(1);
        mockOrg.setDescription("Test Description");
        mockOrg.addUser(mockUser);

        when(authentication.getName()).thenReturn("user@example.com");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        when(organizationRepository.findById(eq(1))).thenReturn(Optional.of(mockOrg));

        ResponseEntity<?> responseEntity = organizationService.getSingleOrganizationData(1, authentication);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        Map<String, Object> responseBody = (Map<String, Object>) responseEntity.getBody();
        assertEquals("success", responseBody.get("status"));
        assertEquals("Organization retrieved successfully", responseBody.get("message"));

        Map<String, Object> orgData = (Map<String, Object>) responseBody.get("data");
        assertEquals(1, Integer.parseInt(orgData.get("orgId").toString()));
        assertEquals("John's Organization", orgData.get("name"));
        assertEquals("Test Description", orgData.get("description"));
    }

    // Test case 2: User does not have access to the organization
    @Test
    public void testGetSingleOrganizationData_UserDoesNotHaveAccess() {
        User mockUser = new User();
        mockUser.setUserId(1);
        mockUser.setEmail("user@example.com");

        Organization mockOrg = new Organization();
        mockOrg.setOrgId(2);
        mockOrg.setName("Another Organization");
        mockOrg.setDescription("Another Description");
        mockOrg.setUsers(Collections.emptySet());

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        when(authentication.getName()).thenReturn("user@example.com");
        when(organizationRepository.findById(eq(2))).thenReturn(Optional.of(mockOrg));

        ResponseEntity<?> responseEntity = organizationService.getSingleOrganizationData(2, authentication);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());

        Map<String, Object> responseBody = (Map<String, Object>) responseEntity.getBody();
        assertEquals("error", responseBody.get("status"));
        assertEquals("Organization not found or user not authorized", responseBody.get("message"));
    }
}