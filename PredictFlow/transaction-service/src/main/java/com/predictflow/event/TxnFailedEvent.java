package com.predictflow.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TxnFailedEvent {
    private Long txnId;
    private Long userId;
    private Double amount;
    private String reason;
    private Integer retryCount;
    private String merchant;
}
