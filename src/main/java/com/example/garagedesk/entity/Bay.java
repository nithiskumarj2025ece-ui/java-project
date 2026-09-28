package com.example.garagedesk.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Entity
public class Bay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "must not be null")
    @Positive(message = "must be a positive number")
    @Column(unique = true, nullable = false)
    private Integer bayNumber;

    @NotBlank(message = "must not be blank")
    private String bayName;

    // false while an unfinished job is using this bay
    private boolean available = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getBayNumber() { return bayNumber; }
    public void setBayNumber(Integer bayNumber) { this.bayNumber = bayNumber; }

    public String getBayName() { return bayName; }
    public void setBayName(String bayName) { this.bayName = bayName; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
}
