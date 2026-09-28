package com.example.garagedesk.service;

import com.example.garagedesk.entity.Bay;
import com.example.garagedesk.exception.ConflictException;
import com.example.garagedesk.exception.ResourceNotFoundException;
import com.example.garagedesk.repository.BayRepository;
import com.example.garagedesk.repository.JobCardRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BayService {

    private final BayRepository bayRepository;
    private final JobCardRepository jobCardRepository;

    public BayService(BayRepository bayRepository, JobCardRepository jobCardRepository) {
        this.bayRepository = bayRepository;
        this.jobCardRepository = jobCardRepository;
    }

    public Bay createBay(Bay bay) {
        if (bayRepository.existsByBayNumber(bay.getBayNumber())) {
            throw new ConflictException("Bay " + bay.getBayNumber() + " already exists");
        }
        bay.setId(null);
        return bayRepository.save(bay);
    }

    public List<Bay> getAllBays() {
        return bayRepository.findAll();
    }

    public Bay getBayById(Long id) {
        return bayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bay with id " + id + " not found"));
    }

    // Used while assigning a job. Must be called inside a transaction (the row stays locked until it ends).
    public Bay getBayForUpdate(Long id) {
        return bayRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bay with id " + id + " not found"));
    }

    public void deleteBay(Long id) {
        getBayById(id);
        if (jobCardRepository.existsByBayId(id)) throw new ConflictException("Bay is referenced by a job card and cannot be deleted");
        bayRepository.deleteById(id);
    }
}
