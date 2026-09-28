package com.example.garagedesk.controller;

import com.example.garagedesk.dto.AssignRequest;
import com.example.garagedesk.dto.JobCardRequest;
import com.example.garagedesk.dto.StatusRequest;
import com.example.garagedesk.entity.JobCard;
import com.example.garagedesk.entity.JobStatus;
import com.example.garagedesk.service.JobCardService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/jobcards", "/api/job-cards"})
public class JobCardController {

    private final JobCardService jobCardService;

    public JobCardController(JobCardService jobCardService) {
        this.jobCardService = jobCardService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public JobCard createJobCard(@Valid @RequestBody JobCardRequest request) {
        return jobCardService.createJobCard(request);
    }

    // optional filter: GET /api/jobcards?status=WAITING
    @GetMapping
    public List<JobCard> getAllJobCards(@RequestParam(required = false) JobStatus status) {
        return jobCardService.getAllJobCards(status);
    }

    @GetMapping("/{id}")
    public JobCard getJobCard(@PathVariable Long id) {
        return jobCardService.getJobCardById(id);
    }

    @PutMapping("/{id}/assign")
    public JobCard assignBayAndMechanic(@PathVariable Long id, @Valid @RequestBody AssignRequest request) {
        return jobCardService.assignBayAndMechanic(id, request);
    }

    @PutMapping("/{id}/status")
    public JobCard updateStatus(@PathVariable Long id, @Valid @RequestBody StatusRequest request) {
        return jobCardService.updateStatus(id, request.getStatus());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteJobCard(@PathVariable Long id) { jobCardService.deleteJobCard(id); }
}
