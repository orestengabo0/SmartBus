package com.example.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.model.Bus;
import com.example.model.BusPark;

public interface BusRepository extends JpaRepository<Bus, Long> {
    List<Bus> findByCurrentBusPark(BusPark busPark);
    List<Bus> findByOperatorName(String operatorName);
    Optional<Bus> findByPlateNumber(String plateNumber);
}
