package com.example.garagedesk.service;

import com.example.garagedesk.dto.AssignRequest;
import com.example.garagedesk.dto.JobCardRequest;
import com.example.garagedesk.entity.Bay;
import com.example.garagedesk.entity.JobCard;
import com.example.garagedesk.entity.JobStatus;
import com.example.garagedesk.entity.Mechanic;
import com.example.garagedesk.entity.Vehicle;
import com.example.garagedesk.exception.BusinessRuleException;
import com.example.garagedesk.exception.ConflictException;
import com.example.garagedesk.exception.ResourceNotFoundException;
import com.example.garagedesk.repository.JobCardRepository;
import com.example.garagedesk.repository.BillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class JobCardService {

    // A bay is "occupied" while its job is in one of these statuses
    private static final List<JobStatus> ACTIVE_STATUSES =
            List.of(JobStatus.WAITING, JobStatus.IN_PROGRESS, JobStatus.QUALITY_CHECK);

    private final JobCardRepository jobCardRepository;
    private final BillRepository billRepository;
    private final VehicleService vehicleService;
    private final BayService bayService;
    private final MechanicService mechanicService;

    public JobCardService(JobCardRepository jobCardRepository,
                          BillRepository billRepository,
                          VehicleService vehicleService,
                          BayService bayService,
                          MechanicService mechanicService) {
        this.jobCardRepository = jobCardRepository;
        this.billRepository = billRepository;
        this.vehicleService = vehicleService;
        this.bayService = bayService;
        this.mechanicService = mechanicService;
    }

    public JobCard createJobCard(JobCardRequest request) {
        Vehicle vehicle = vehicleService.getVehicleById(request.getVehicleId());

        JobCard jobCard = new JobCard();
        jobCard.setVehicle(vehicle);
        jobCard.setRequestedServices(request.getRequestedServices());
        jobCard.setDescription(request.getDescription());
        jobCard.setStatus(JobStatus.WAITING);
        jobCard.setCreatedAt(LocalDateTime.now());
        return jobCardRepository.save(jobCard);
    }

    public List<JobCard> getAllJobCards(JobStatus status) {
        if (status == null) {
            return jobCardRepository.findAll();
        }
        return jobCardRepository.findByStatus(status);
    }

    public JobCard getJobCardById(Long id) {
        return jobCardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job card with id " + id + " not found"));
    }

    @Transactional
    public void deleteJobCard(Long id) {
        JobCard job = getJobCardById(id);
        if (billRepository.existsByJobCardId(id)) throw new ConflictException("Delete the bill before deleting this job card");
        if (job.getBay() != null) job.getBay().setAvailable(true);
        if (job.getMechanic() != null) job.getMechanic().setAvailable(true);
        jobCardRepository.delete(job);
    }

    // RULE 1: a bay can hold only one active job at a time
    @Transactional
    public JobCard assignBayAndMechanic(Long jobCardId, AssignRequest request) {
        JobCard jobCard = getJobCardById(jobCardId);

        if (jobCard.getStatus() != JobStatus.WAITING) {
            throw new BusinessRuleException("Bay and mechanic can only be assigned while the job is WAITING");
        }

        Bay bay = bayService.getBayForUpdate(request.getBayId());
        Mechanic mechanic = mechanicService.getMechanicById(request.getMechanicId());

        Bay oldBay = jobCard.getBay();
        Mechanic oldMechanic = jobCard.getMechanic();
        boolean sameBay = oldBay != null && oldBay.getId().equals(bay.getId());
        boolean sameMechanic = oldMechanic != null && oldMechanic.getId().equals(mechanic.getId());

        // check that no other unfinished job is using this bay
        if (jobCardRepository.existsByBayIdAndStatusInAndIdNot(bay.getId(), ACTIVE_STATUSES, jobCard.getId())) {
            throw new ConflictException("Bay " + bay.getBayNumber() + " is already occupied by an active job");
        }
        if (!sameBay && !bay.isAvailable()) {
            throw new ConflictException("Bay " + bay.getBayNumber() + " is not available");
        }
        if (!sameMechanic && !mechanic.isAvailable()) {
            throw new ConflictException("Mechanic " + mechanic.getName() + " is not available");
        }

        // if the job is being moved, free the bay and mechanic it had before
        if (oldBay != null && !sameBay) {
            oldBay.setAvailable(true);
        }
        if (oldMechanic != null && !sameMechanic) {
            oldMechanic.setAvailable(true);
        }

        // bay and mechanic are saved automatically when the transaction ends
        bay.setAvailable(false);
        mechanic.setAvailable(false);
        jobCard.setBay(bay);
        jobCard.setMechanic(mechanic);
        return jobCardRepository.save(jobCard);
    }

    // RULE 2: WAITING -> IN_PROGRESS -> QUALITY_CHECK -> COMPLETED (no skipping)
    @Transactional
    public JobCard updateStatus(Long jobCardId, JobStatus newStatus) {
        JobCard jobCard = getJobCardById(jobCardId);
        validateStatusChange(jobCard.getStatus(), newStatus);

        if (newStatus == JobStatus.IN_PROGRESS
                && (jobCard.getBay() == null || jobCard.getMechanic() == null)) {
            throw new BusinessRuleException("Assign a bay and a mechanic before starting the job");
        }

        jobCard.setStatus(newStatus);

        // finished job: the bay and the mechanic become free again
        if (newStatus == JobStatus.COMPLETED) {
            if (jobCard.getBay() != null) {
                jobCard.getBay().setAvailable(true);
            }
            if (jobCard.getMechanic() != null) {
                jobCard.getMechanic().setAvailable(true);
            }
        }
        return jobCardRepository.save(jobCard);
    }

    private void validateStatusChange(JobStatus current, JobStatus next) {
        if (current == JobStatus.COMPLETED) {
            throw new BusinessRuleException("Job is already completed and its status cannot be changed");
        }
        if (next == JobStatus.COMPLETED && current != JobStatus.QUALITY_CHECK) {
            throw new BusinessRuleException("Job must pass quality-check before it can be completed");
        }

        boolean allowed = (current == JobStatus.WAITING && next == JobStatus.IN_PROGRESS)
                || (current == JobStatus.IN_PROGRESS && next == JobStatus.QUALITY_CHECK)
                || (current == JobStatus.QUALITY_CHECK && next == JobStatus.COMPLETED);
        if (!allowed) {
            throw new BusinessRuleException("Invalid status change from " + current + " to " + next
                    + ". Allowed order: WAITING -> IN_PROGRESS -> QUALITY_CHECK -> COMPLETED");
        }
    }
}
