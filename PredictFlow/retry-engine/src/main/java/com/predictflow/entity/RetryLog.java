package com.predictflow.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "retry_logs", schema = "predictflow")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RetryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "txn_id", nullable = false)
    private Long txnId;

    @Column(name = "predicted_success")
    private Double predictedSuccess;

    // changed: matches DB column names
    @Column(name = "recommended_delay_sec")
    private Integer recommendedDelaySec;

    @Column(name = "delay_applied_sec")
    private Integer delayAppliedSec;

    @Column(name = "attempt_number")
    private Integer attemptNumber;

    @Column(name = "retry_time")
    private Instant retryTime;

    @Column(name = "retry_status")
    private String retryStatus;

    @Column(name = "notes")
    private String notes;
}
