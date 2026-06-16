package by.urbash_hair.dto;

import lombok.Data;

@Data
public class BookSlotRequest {
    private String notes;
    private Integer hairLength;   // длина волос в см
    private Integer hairDensity;  // густота в см (0, 6, 7, 8, 9, 10, 12)
}