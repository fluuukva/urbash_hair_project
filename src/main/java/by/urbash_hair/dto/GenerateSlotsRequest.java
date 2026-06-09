package by.urbash_hair.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class GenerateSlotsRequest {
    private LocalDate startDate;
    private LocalDate endDate;
    private List<Integer> weekdays;
    private String startTime;
    private Integer durationMinutes;
    private Long masterId;
    private Long serviceId;
}