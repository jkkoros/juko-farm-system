package com.example.farmmanagement.repository;

import com.example.farmmanagement.model.Crop;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CropRepository extends JpaRepository<Crop, Long> {}
