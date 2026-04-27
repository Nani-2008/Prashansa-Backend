package com.prashansa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_user")
public class AppUser {

    @Id
    private String id;

    @Column(unique = true, nullable = false, length = 20)
    private String phone;

    private String name;

    @Column(nullable = false, length = 32)
    private String role;

    private String location;

    private String password;

    private String emergencyContact;

    private String profilePicUrl;

    public AppUser() {
    }

    public AppUser(String id, String phone, String name, String role, String location, String password) {
        this.id = id;
        this.phone = phone;
        this.name = name;
        this.role = role;
        this.location = location;
        this.password = password;
    }

    public AppUser(String id, String phone, String name, String role, String location, String password, String emergencyContact) {
        this.id = id;
        this.phone = phone;
        this.name = name;
        this.role = role;
        this.location = location;
        this.password = password;
        this.emergencyContact = emergencyContact;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    public void setProfilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }
}
