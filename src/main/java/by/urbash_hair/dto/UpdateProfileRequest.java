package by.urbash_hair.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String lastName;
    private String firstName;
    private String middleName;
    private String email;
}