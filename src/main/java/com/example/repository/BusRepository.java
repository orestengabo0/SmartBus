package com.example.repository;

import java.util.List;
import java.util.Optional;

import com.example.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.model.Bus;
import com.example.model.BusPark;

public interface BusRepository extends JpaRepository<Bus, Long> {
    List<Bus> findByOperator(User operator);
    Optional<Bus> findByPlateNumber(String plateNumber);
    List<Bus> findByActive(boolean active);
    Bus getBusById(Long id);
}
