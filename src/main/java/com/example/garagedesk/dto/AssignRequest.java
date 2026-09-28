package com.example.garagedesk.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class AssignRequest {

    @NotNull(message = "must not be null")
    @Positive(message = "must be a positive number")
    private Long bayId;

    @NotNull(message = "must not be null")
    @Positive(message = "must be a positive number")
    private Long mechanicId;

    public Long getBayId() { return bayId; }
    public void setBayId(Long bayId) { this.bayId = bayId; }

    public Long getMechanicId() { return mechanicId; }
    public void setMechanicId(Long mechanicId) { this.mechanicId = mechanicId; }
}
