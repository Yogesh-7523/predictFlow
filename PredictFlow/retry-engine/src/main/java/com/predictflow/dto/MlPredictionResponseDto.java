package com.predictflow.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MlPredictionResponseDto {
    private Double probSuccess;
    private Integer recommendedDelay;
}