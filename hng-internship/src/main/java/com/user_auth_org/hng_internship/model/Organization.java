package com.user_auth_org.hng_internship.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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
public class Organization {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true)
    private Integer orgId;

    @NotBlank(message = "name is mandatory")
    @Column(unique = true)
    private String name;
    private String description;

    @ToString.Exclude
    @ManyToMany
    @JoinTable(
            name = "user_organization",
            joinColumns = @JoinColumn(name = "org_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> users = new HashSet<>();

    @Override
    public int hashCode() {
        return Objects.hash(orgId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Organization that = (Organization) o;
        return Objects.equals(orgId, that.orgId);
    }

    // Add and remove users
    public void addUser(User user) {
        users.add(user);
        user.getOrganizations().add(this);
    }

    public void removeUser(User user) {
        users.remove(user);
        user.getOrganizations().remove(this);
    }

    public Organization(String name) {
        this.name = name + "'s Organization";
    }
}
