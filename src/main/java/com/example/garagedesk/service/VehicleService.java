package com.example.garagedesk.service;

import com.example.garagedesk.entity.Vehicle;
import com.example.garagedesk.exception.ConflictException;
import com.example.garagedesk.exception.ResourceNotFoundException;
import com.example.garagedesk.repository.VehicleRepository;
import com.example.garagedesk.repository.JobCardRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final JobCardRepository jobCardRepository;

    public VehicleService(VehicleRepository vehicleRepository, JobCardRepository jobCardRepository) {
        this.vehicleRepository = vehicleRepository;
        this.jobCardRepository = jobCardRepository;
    }

    public Vehicle createVehicle(Vehicle vehicle) {
        if (vehicleRepository.existsByRegistrationNumber(vehicle.getRegistrationNumber())) {
            throw new ConflictException("Vehicle " + vehicle.getRegistrationNumber() + " is already registered");
        }
        vehicle.setId(null);
        return vehicleRepository.save(vehicle);
    }

    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    public Vehicle getVehicleById(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle with id " + id + " not found"));
    }

    public void deleteVehicle(Long id) {
        getVehicleById(id);
        if (jobCardRepository.existsByVehicleId(id)) throw new ConflictException("Vehicle has job cards and cannot be deleted");
        vehicleRepository.deleteById(id);
    }
}
