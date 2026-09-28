package com.example.garagedesk.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public class BillRequest {

    @NotNull(message = "must not be null")
    @Positive(message = "must be a positive number")
    private Long jobCardId;

    @NotNull(message = "must not be null")
    @PositiveOrZero(message = "must not be negative")
    private BigDecimal partsAmount;

    @NotNull(message = "must not be null")
    @PositiveOrZero(message = "must not be negative")
    private BigDecimal labourCharge;

    public Long getJobCardId() { return jobCardId; }
    public void setJobCardId(Long jobCardId) { this.jobCardId = jobCardId; }

    public BigDecimal getPartsAmount() { return partsAmount; }
    public void setPartsAmount(BigDecimal partsAmount) { this.partsAmount = partsAmount; }

    public BigDecimal getLabourCharge() { return labourCharge; }
    public void setLabourCharge(BigDecimal labourCharge) { this.labourCharge = labourCharge; }
}
