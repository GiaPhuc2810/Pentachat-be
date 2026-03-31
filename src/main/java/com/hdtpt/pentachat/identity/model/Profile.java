package com.hdtpt.pentachat.identity.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import com.hdtpt.pentachat.util.BaseEntity;

@Entity
@Table(name = "profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Profile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    @Column(length = 255)
    private String fullName;

    @Column(length = 500)
    private String bio;

    @Column(length = 255)
    private String avatar;

    @Column(length = 255)
    private String phoneNumber;

    @Column(length = 255)
    private String address;
}
