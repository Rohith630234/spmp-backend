package com.college.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Key-value pair representation for frontend charting (Chart.js / Recharts).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChartDataDto {

    private String name;
    private Number value;
}
