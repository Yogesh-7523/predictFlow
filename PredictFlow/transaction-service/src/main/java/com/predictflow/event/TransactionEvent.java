package com.predictflow.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionEvent {
    private Long id;
    private String userEmail;
    private Double amount;
    private String merchant;
    private String status;
}
