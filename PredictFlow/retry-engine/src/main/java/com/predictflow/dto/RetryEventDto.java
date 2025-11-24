package com.predictflow.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RetryEventDto {
    private Long txnId;
    private Integer attempt;
    private Double prediction;
}