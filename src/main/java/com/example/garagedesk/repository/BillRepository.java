package com.example.garagedesk.repository;

import com.example.garagedesk.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillRepository extends JpaRepository<Bill, Long> {

    boolean existsByJobCardId(Long jobCardId);
}
