package com.example.repository;

import java.util.List;
import java.util.Optional;

import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.model.BusPark;

public interface BusParkRepository extends JpaRepository<BusPark, Long> {
    List<BusPark> findByLocation(String location);
    Optional<BusPark> findByName(String name);

    boolean existsByNameAndLocation(@NotBlank String name, @NotBlank String location);

    List<BusPark> findByActive(boolean active);

    BusPark getBusParkById(Long id);
}
