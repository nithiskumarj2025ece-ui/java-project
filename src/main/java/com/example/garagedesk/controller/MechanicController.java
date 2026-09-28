package com.example.garagedesk.controller;

import com.example.garagedesk.entity.Mechanic;
import com.example.garagedesk.service.MechanicService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mechanics")
public class MechanicController {

    private final MechanicService mechanicService;

    public MechanicController(MechanicService mechanicService) {
        this.mechanicService = mechanicService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mechanic createMechanic(@Valid @RequestBody Mechanic mechanic) {
        return mechanicService.createMechanic(mechanic);
    }

    @GetMapping
    public List<Mechanic> getAllMechanics() {
        return mechanicService.getAllMechanics();
    }

    @GetMapping("/{id}")
    public Mechanic getMechanic(@PathVariable Long id) {
        return mechanicService.getMechanicById(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMechanic(@PathVariable Long id) { mechanicService.deleteMechanic(id); }
}
