package com.predictflow.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TxnFailedEventDto {
    private Long txnId;
    private Long userId;
    private Double amount;
    private String reason;
    private Integer retryCount;
    private String merchant;
}