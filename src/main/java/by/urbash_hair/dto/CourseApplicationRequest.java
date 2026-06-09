package by.urbash_hair.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CourseApplicationRequest {
    
    @JsonProperty("firstName")
    private String firstName;
    
    @JsonProperty("lastName")
    private String lastName;
    
    private String email;
    private String phone;
    
    @JsonProperty("interest")
    private String interest;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("courseId")
    private Long courseId;
    
    @JsonProperty("vacancy")
    private String vacancy;
    
    @JsonProperty("clientId")
    private Long clientId;
    
    public boolean isJobApplication() {
        if (vacancy != null && !vacancy.isEmpty()) {
            return true;
        }
        if (interest != null && interest.contains("Вакансия")) {
            return true;
        }
        return false;
    }
    
    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        }
        return "";
    }
}
