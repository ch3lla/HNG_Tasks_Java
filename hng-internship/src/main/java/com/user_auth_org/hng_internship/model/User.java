package com.user_auth_org.hng_internship.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;

    @NotBlank(message = "First name is mandatory")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstname;

    @NotBlank(message = "Last name is mandatory")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastname;

    // @NotBlank(message = "Phone number is mandatory")
    @Pattern(regexp = "\\d{10,15}", message = "Phone number must be between 10 and 15 digits")
    private String phone;

    @NotBlank(message = "Password is mandatory")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @NotBlank(message = "Email is mandatory")
    @Email(message = "Email should be valid")
    @Column(unique = true)
    private String email;

    @ToString.Exclude
    @ManyToMany(mappedBy = "users")
    private Set<Organization> organizations = new HashSet<>();


    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(userId, user.userId);
    }

    // Add and remove organizations
    public void addOrganization(Organization organization) {
        organizations.add(organization);
        organization.getUsers().add(this);
    }

    public void removeOrganization(Organization organization) {
        organizations.remove(organization);
        organization.getUsers().remove(this);
    }

    public Organization createOrganization() {
        Organization organization = new Organization();
        organization.setName(this.firstname + "'s Organization");
        organization.addUser(this);
        this.addOrganization(organization);
        return organization;
    }
}
