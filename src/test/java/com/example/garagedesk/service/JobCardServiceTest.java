package com.example.garagedesk.service;

import com.example.garagedesk.dto.AssignRequest;
import com.example.garagedesk.entity.Bay;
import com.example.garagedesk.entity.JobCard;
import com.example.garagedesk.entity.JobStatus;
import com.example.garagedesk.entity.Mechanic;
import com.example.garagedesk.exception.BusinessRuleException;
import com.example.garagedesk.exception.ConflictException;
import com.example.garagedesk.repository.JobCardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

// Plain unit tests: no database needed
@ExtendWith(MockitoExtension.class)
class JobCardServiceTest {

    @Mock
    private JobCardRepository jobCardRepository;
    @Mock
    private VehicleService vehicleService;
    @Mock
    private BayService bayService;
    @Mock
    private MechanicService mechanicService;

    @InjectMocks
    private JobCardService jobCardService;

    private Bay bay;
    private Mechanic mechanic;

    @BeforeEach
    void setUp() {
        bay = new Bay();
        bay.setId(1L);
        bay.setBayNumber(1);
        bay.setBayName("Bay 1");

        mechanic = new Mechanic();
        mechanic.setId(5L);
        mechanic.setName("Ravi");
        lenient().when(jobCardRepository.save(any(JobCard.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private JobCard jobCardWithStatus(JobStatus status) {
        JobCard jobCard = new JobCard();
        jobCard.setId(10L);
        jobCard.setStatus(status);
        when(jobCardRepository.findById(10L)).thenReturn(Optional.of(jobCard));
        return jobCard;
    }

    private AssignRequest assignRequest() {
        AssignRequest request = new AssignRequest();
        request.setBayId(1L);
        request.setMechanicId(5L);
        return request;
    }

    @Test
    void inProgressCannotGoDirectlyToCompleted() {
        jobCardWithStatus(JobStatus.IN_PROGRESS);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> jobCardService.updateStatus(10L, JobStatus.COMPLETED));
        assertEquals("Job must pass quality-check before it can be completed", ex.getMessage());
    }

    @Test
    void waitingCannotGoDirectlyToCompleted() {
        jobCardWithStatus(JobStatus.WAITING);

        assertThrows(BusinessRuleException.class,
                () -> jobCardService.updateStatus(10L, JobStatus.COMPLETED));
    }

    @Test
    void waitingCannotStartWithoutBayAndMechanic() {
        jobCardWithStatus(JobStatus.WAITING);

        assertThrows(BusinessRuleException.class,
                () -> jobCardService.updateStatus(10L, JobStatus.IN_PROGRESS));
    }

    @Test
    void qualityCheckToCompletedFreesBayAndMechanic() {
        JobCard jobCard = jobCardWithStatus(JobStatus.QUALITY_CHECK);
        bay.setAvailable(false);
        mechanic.setAvailable(false);
        jobCard.setBay(bay);
        jobCard.setMechanic(mechanic);

        JobCard result = jobCardService.updateStatus(10L, JobStatus.COMPLETED);

        assertEquals(JobStatus.COMPLETED, result.getStatus());
        assertTrue(bay.isAvailable());
        assertTrue(mechanic.isAvailable());
    }

    @Test
    void assigningAnOccupiedBayIsRejected() {
        JobCard jobCard = jobCardWithStatus(JobStatus.WAITING);
        when(bayService.getBayForUpdate(1L)).thenReturn(bay);
        when(mechanicService.getMechanicById(5L)).thenReturn(mechanic);
        when(jobCardRepository.existsByBayIdAndStatusInAndIdNot(eq(1L), anyList(), eq(10L))).thenReturn(true);

        ConflictException ex = assertThrows(ConflictException.class,
                () -> jobCardService.assignBayAndMechanic(10L, assignRequest()));
        assertEquals("Bay 1 is already occupied by an active job", ex.getMessage());
        assertNull(jobCard.getBay());
    }

    @Test
    void assigningAFreeBayWorks() {
        JobCard jobCard = jobCardWithStatus(JobStatus.WAITING);
        when(bayService.getBayForUpdate(1L)).thenReturn(bay);
        when(mechanicService.getMechanicById(5L)).thenReturn(mechanic);

        JobCard result = jobCardService.assignBayAndMechanic(10L, assignRequest());

        assertSame(bay, result.getBay());
        assertSame(mechanic, result.getMechanic());
        assertFalse(bay.isAvailable());
        assertFalse(mechanic.isAvailable());
    }
}
