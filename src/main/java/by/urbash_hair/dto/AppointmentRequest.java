package by.urbash_hair.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AppointmentRequest {
    
    @JsonProperty("appointment_date")
    private String date;
    
    private String time;
    private String notes;
    
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    
    @JsonProperty("service")
    private String serviceName;
    
    @JsonProperty("serviceId")
    private Long serviceId;
    
    @JsonProperty("masterId")
    private Long masterId;
    
    @JsonProperty("clientId")
    private Long clientId;
}
