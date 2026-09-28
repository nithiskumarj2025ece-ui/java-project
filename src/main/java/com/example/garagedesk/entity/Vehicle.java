package com.example.garagedesk.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "must not be blank")
    @Column(name = "vehicle_number", unique = true, nullable = false)
    private String registrationNumber;

    @NotBlank(message = "must not be blank")
    private String ownerName;

    private String phoneNumber;

    @NotBlank(message = "must not be blank")
    private String vehicleModel;

    @NotBlank(message = "must not be blank")
    private String vehicleType;

    // One vehicle can have many job cards over time
    @OneToMany(mappedBy = "vehicle")
    @JsonIgnore
    private List<JobCard> jobCards = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getVehicleModel() { return vehicleModel; }
    public void setVehicleModel(String vehicleModel) { this.vehicleModel = vehicleModel; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public List<JobCard> getJobCards() { return jobCards; }
    public void setJobCards(List<JobCard> jobCards) { this.jobCards = jobCards; }
}
