package com.example.garagedesk.repository;

import com.example.garagedesk.entity.JobCard;
import com.example.garagedesk.entity.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobCardRepository extends JpaRepository<JobCard, Long> {

    List<JobCard> findByStatus(JobStatus status);
    boolean existsByVehicleId(Long vehicleId);
    boolean existsByMechanicId(Long mechanicId);
    boolean existsByBayId(Long bayId);

    // true if the bay has another job (not this one) in one of the given statuses
    boolean existsByBayIdAndStatusInAndIdNot(Long bayId, List<JobStatus> statuses, Long jobCardId);
}
