package com.example.farmmanagement.repository;

import com.example.farmmanagement.model.Farm;
import com.example.farmmanagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FarmRepository extends JpaRepository<Farm, Long> {
    List<Farm> findByUser(User user);
}