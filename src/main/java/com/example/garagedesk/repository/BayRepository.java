package com.example.garagedesk.repository;

import com.example.garagedesk.entity.Bay;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BayRepository extends JpaRepository<Bay, Long> {

    boolean existsByBayNumber(Integer bayNumber);

    // Locks the bay row until the transaction ends, so two requests
    // cannot assign the same bay at the same moment.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Bay b where b.id = :id")
    Optional<Bay> findByIdForUpdate(@Param("id") Long id);
}
