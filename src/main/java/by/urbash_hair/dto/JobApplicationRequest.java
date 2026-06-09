package by.urbash_hair.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class JobApplicationRequest {
    private String name;
    private String email;
    private String phone;
    private String message;
    private String vacancy;
    
    @JsonProperty("clientId")
    private Long clientId;
}
