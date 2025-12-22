package com.example.farmmanagement.repository;

import com.example.farmmanagement.model.CherryTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CherryTransferRepository extends JpaRepository<CherryTransfer, Long> {

    // Get all transfers ordered by latest first
    List<CherryTransfer> findAllByOrderByTransferDateDescIdDesc();
}
