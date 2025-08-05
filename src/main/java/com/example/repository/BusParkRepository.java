package com.example.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.model.BusPark;

public interface BusParkRepository extends JpaRepository<BusPark, Long> {
    List<BusPark> findByLocation(String location);
    Optional<BusPark> findByName(String name);
}
