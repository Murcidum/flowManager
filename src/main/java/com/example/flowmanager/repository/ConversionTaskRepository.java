package com.example.flowmanager.repository;

import com.example.flowmanager.entity.ConversionTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ConversionTaskRepository extends JpaRepository<ConversionTask, UUID> {
}
