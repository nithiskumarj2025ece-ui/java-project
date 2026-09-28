package com.example.garagedesk.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // one job card has at most one bill
    @OneToOne(optional = false)
    @JoinColumn(name = "job_card_id", unique = true)
    private JobCard jobCard;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal partsAmount;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal labourCharge;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    private LocalDateTime generatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public JobCard getJobCard() { return jobCard; }
    public void setJobCard(JobCard jobCard) { this.jobCard = jobCard; }

    public BigDecimal getPartsAmount() { return partsAmount; }
    public void setPartsAmount(BigDecimal partsAmount) { this.partsAmount = partsAmount; }

    public BigDecimal getLabourCharge() { return labourCharge; }
    public void setLabourCharge(BigDecimal labourCharge) { this.labourCharge = labourCharge; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
}
