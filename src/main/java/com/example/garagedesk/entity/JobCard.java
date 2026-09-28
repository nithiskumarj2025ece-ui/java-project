package com.example.garagedesk.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class JobCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @Column(length = 1000, nullable = false)
    private String requestedServices;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status;

    // many job cards over time can use the same bay
    @ManyToOne
    @JoinColumn(name = "bay_id")
    private Bay bay;

    @ManyToOne
    @JoinColumn(name = "mechanic_id")
    private Mechanic mechanic;

    private LocalDateTime createdAt;

    // Bill owns the foreign key; ignored in JSON to avoid an endless loop
    @OneToOne(mappedBy = "jobCard")
    @JsonIgnore
    private Bill bill;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Vehicle getVehicle() { return vehicle; }
    public void setVehicle(Vehicle vehicle) { this.vehicle = vehicle; }

    public String getRequestedServices() { return requestedServices; }
    public void setRequestedServices(String requestedServices) { this.requestedServices = requestedServices; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public JobStatus getStatus() { return status; }
    public void setStatus(JobStatus status) { this.status = status; }

    public Bay getBay() { return bay; }
    public void setBay(Bay bay) { this.bay = bay; }

    public Mechanic getMechanic() { return mechanic; }
    public void setMechanic(Mechanic mechanic) { this.mechanic = mechanic; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Bill getBill() { return bill; }
    public void setBill(Bill bill) { this.bill = bill; }
}
