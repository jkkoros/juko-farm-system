package com.example.farmmanagement.repository;

import com.example.farmmanagement.model.QualityRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QualityRecordRepository extends JpaRepository<QualityRecord, Long> {
    List<QualityRecord> findAllByOrderByPulpingDateDesc();
}
