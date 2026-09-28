package com.example.garagedesk.dto;

import com.example.garagedesk.entity.JobStatus;
import jakarta.validation.constraints.NotNull;

public class StatusRequest {

    @NotNull(message = "must not be null")
    private JobStatus status;

    public JobStatus getStatus() { return status; }
    public void setStatus(JobStatus status) { this.status = status; }
}
