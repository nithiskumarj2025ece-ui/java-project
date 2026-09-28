package com.example.garagedesk.service;

import com.example.garagedesk.dto.BillRequest;
import com.example.garagedesk.entity.Bill;
import com.example.garagedesk.entity.JobCard;
import com.example.garagedesk.entity.JobStatus;
import com.example.garagedesk.exception.BusinessRuleException;
import com.example.garagedesk.exception.ConflictException;
import com.example.garagedesk.exception.ResourceNotFoundException;
import com.example.garagedesk.repository.BillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BillService {

    private final BillRepository billRepository;
    private final JobCardService jobCardService;

    public BillService(BillRepository billRepository, JobCardService jobCardService) {
        this.billRepository = billRepository;
        this.jobCardService = jobCardService;
    }

    @Transactional
    public Bill generateBill(BillRequest request) {
        JobCard jobCard = jobCardService.getJobCardById(request.getJobCardId());

        if (jobCard.getStatus() != JobStatus.COMPLETED) {
            throw new BusinessRuleException("Bill can only be generated for a COMPLETED job");
        }
        if (billRepository.existsByJobCardId(jobCard.getId())) {
            throw new ConflictException("A bill already exists for job card " + jobCard.getId());
        }

        BigDecimal partsAmount = request.getPartsAmount().setScale(2, RoundingMode.HALF_UP);
        BigDecimal labourCharge = request.getLabourCharge().setScale(2, RoundingMode.HALF_UP);

        Bill bill = new Bill();
        bill.setJobCard(jobCard);
        bill.setPartsAmount(partsAmount);
        bill.setLabourCharge(labourCharge);
        bill.setTotalAmount(partsAmount.add(labourCharge));
        bill.setGeneratedAt(LocalDateTime.now());
        return billRepository.save(bill);
    }

    public List<Bill> getAllBills() {
        return billRepository.findAll();
    }

    public Bill getBillById(Long id) {
        return billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill with id " + id + " not found"));
    }

    public void deleteBill(Long id) {
        getBillById(id);
        billRepository.deleteById(id);
    }
}
