package com.example.garagedesk.service;

import com.example.garagedesk.entity.Mechanic;
import com.example.garagedesk.exception.ResourceNotFoundException;
import com.example.garagedesk.repository.MechanicRepository;
import com.example.garagedesk.repository.JobCardRepository;
import com.example.garagedesk.exception.ConflictException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MechanicService {

    private final MechanicRepository mechanicRepository;
    private final JobCardRepository jobCardRepository;

    public MechanicService(MechanicRepository mechanicRepository, JobCardRepository jobCardRepository) {
        this.mechanicRepository = mechanicRepository;
        this.jobCardRepository = jobCardRepository;
    }

    public Mechanic createMechanic(Mechanic mechanic) {
        mechanic.setId(null);
        return mechanicRepository.save(mechanic);
    }

    public List<Mechanic> getAllMechanics() {
        return mechanicRepository.findAll();
    }

    public Mechanic getMechanicById(Long id) {
        return mechanicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mechanic with id " + id + " not found"));
    }

    public void deleteMechanic(Long id) {
        getMechanicById(id);
        if (jobCardRepository.existsByMechanicId(id)) throw new ConflictException("Mechanic is referenced by a job card and cannot be deleted");
        mechanicRepository.deleteById(id);
    }
}
