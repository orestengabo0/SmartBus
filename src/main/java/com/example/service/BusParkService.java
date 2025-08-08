package com.example.service;

import com.example.dto.requests.BusParkRequest;
import com.example.dto.responses.BusParkResponse;
import com.example.exception.ResourceAlreadyExistsException;
import com.example.exception.ResourceNotFoundException;
import com.example.exception.UnauthorizedException;
import com.example.model.BusPark;
import com.example.model.Role;
import com.example.model.User;
import com.example.repository.BusParkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class BusParkService {
    private final BusParkRepository busParkRepository;
    private final CurrentUserService currentUserService;

    public BusParkResponse createBusPark(BusParkRequest busParkDTO) {
        User currentUser = currentUserService.getCurrentUser();
        if(currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.SUPER_ADMIN) {
            throw new UnauthorizedException("Access denied.");
        }
        if (busParkRepository.existsByNameAndLocation(busParkDTO.getName(), busParkDTO.getLocation())) {
            throw new ResourceAlreadyExistsException("A bus park with this name already exists in this location");
        }

        BusPark busPark = new BusPark();
        busPark.setName(busParkDTO.getName());
        busPark.setLocation(busParkDTO.getLocation());
        busPark.setAddress(busParkDTO.getAddress());
        busPark.setContactNumber(busParkDTO.getContactNumber());
        busPark.setLatitude(busParkDTO.getLatitude());
        busPark.setLongitude(busParkDTO.getLongitude());
        busPark.setCreatedAt(LocalDateTime.now());

        busParkRepository.save(busPark);
        return convertToDTO(busPark);
    }

    public List<BusParkResponse> getAllBusParks() {
        List<BusPark> busParks = busParkRepository.findByActive(true);
        return busParks.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public BusParkResponse getBusParkById(Long id) {
        BusPark busPark = busParkRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bus park not found with id: " + id));
        return convertToDTO(busPark);
    }

    public List<BusParkResponse> getBusParksByLocation(String location) {
        List<BusPark> busParks = busParkRepository.findByLocation(location);
        return busParks.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public BusParkResponse updateBusPark(Long id, BusParkRequest busParkDTO) {
        // Check permissions
        User currentUser = currentUserService.getCurrentUser();
        if (currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.SUPER_ADMIN) {
            throw new UnauthorizedException("Only admins can update bus parks");
        }

        BusPark busPark = busParkRepository.getBusParkById(id);

        busPark.setName(busParkDTO.getName());
        busPark.setLocation(busParkDTO.getLocation());
        busPark.setAddress(busParkDTO.getAddress());
        busPark.setContactNumber(busParkDTO.getContactNumber());
        busPark.setLatitude(busParkDTO.getLatitude());
        busPark.setLongitude(busParkDTO.getLongitude());

        busParkRepository.save(busPark);
        return convertToDTO(busPark);
    }

    public void deleteBusPark(Long id) {
        // Check permissions
        User currentUser = currentUserService.getCurrentUser();
        if (currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.SUPER_ADMIN) {
            throw new UnauthorizedException("Only admins can delete bus parks");
        }

        BusPark busPark = busParkRepository.getBusParkById(id);
        busPark.setActive(false); // Soft delete
        busParkRepository.save(busPark);
    }

    private BusParkResponse convertToDTO(BusPark busPark) {
        BusParkResponse dto = new BusParkResponse();

        dto.setId(busPark.getId());
        dto.setName(busPark.getName());
        dto.setLocation(busPark.getLocation());
        dto.setAddress(busPark.getAddress());
        dto.setContactNumber(busPark.getContactNumber());

        // Set counts instead of collections
        dto.setBusCount(busPark.getBuses() != null ? busPark.getBuses().size() : 0);
        dto.setDepartureTripCount(busPark.getDepartureTrips() != null ? busPark.getDepartureTrips().size() : 0);
        dto.setArrivalTripCount(busPark.getArrivalTrips() != null ? busPark.getArrivalTrips().size() : 0);

        dto.setLatitude(busPark.getLatitude());
        dto.setLongitude(busPark.getLongitude());
        dto.setActive(busPark.isActive());
        dto.setCreatedAt(busPark.getCreatedAt());

        return dto;
    }
}
