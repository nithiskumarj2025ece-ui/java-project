package com.example.garagedesk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class JobCardRequest {

    @NotNull(message = "must not be null")
    @Positive(message = "must be a positive number")
    private Long vehicleId;

    @NotBlank(message = "must not be blank")
    private String requestedServices;

    private String description;

    public Long getVehicleId() { return vehicleId; }
    public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }

    public String getRequestedServices() { return requestedServices; }
    public void setRequestedServices(String requestedServices) { this.requestedServices = requestedServices; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
