package com.example.service;

import com.example.dto.requests.BusParkRequest;
import com.example.dto.responses.BusParkResponse;
import com.example.exception.ResourceAlreadyExistsException;
import com.example.exception.ResourceNotFoundException;
import com.example.exception.UnauthorizedException;
import com.example.mappers.BusParkMapper;
import com.example.model.BusPark;
import com.example.model.Role;
import com.example.model.User;
import com.example.repository.BusParkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class BusParkService {
    private final BusParkRepository busParkRepository;
    private final CurrentUserService currentUserService;
    private final BusParkMapper busParkMapper;

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
        return busParkMapper.toBusParkResponse(busPark);
    }

    public List<BusParkResponse> getAllBusParks() {
        List<BusPark> busParks = busParkRepository.findByActive(true);
        return busParkMapper.toBusParkResponseList(busParks);
    }

    public BusParkResponse getBusParkById(Long id) {
        BusPark busPark = busParkRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bus park not found with id: " + id));
        return busParkMapper.toBusParkResponse(busPark);
    }

    public List<BusParkResponse> getBusParksByLocation(String location) {
        List<BusPark> busParks = busParkRepository.findByLocation(location);
        return busParkMapper.toBusParkResponseList(busParks);
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
        return busParkMapper.toBusParkResponse(busPark);
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
}
