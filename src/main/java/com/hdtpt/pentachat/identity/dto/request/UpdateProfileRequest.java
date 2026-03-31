package com.hdtpt.pentachat.identity.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {
    private String fullName;
    private String bio;
    private String avatar;
    private String phoneNumber;
    private String address;
}
