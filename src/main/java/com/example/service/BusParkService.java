package com.example.service;

import com.example.dto.BusParkDTO;
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

@Service
@Transactional
@RequiredArgsConstructor
public class BusParkService {
    private final BusParkRepository busParkRepository;
    private final CurrentUserService currentUserService;

    public BusPark createBusPark(BusParkDTO busParkDTO) {
        User currentUser = currentUserService.getCurrentUser();
        if(currentUser.getRole() != Role.ADMIN || currentUser.getRole() != Role.SUPER_ADMIN) {
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

        return busParkRepository.save(busPark);
    }

    public List<BusPark> getAllBusParks() {
        return busParkRepository.findByActive((true));
    }

    public BusPark getBusParkById(Long id) {
        return busParkRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bus park not found with id: " + id));
    }

    public List<BusPark> getBusParksByLocation(String location) {
        return busParkRepository.findByLocation(location);
    }

    public BusPark updateBusPark(Long id, BusParkDTO busParkDTO) {
        // Check permissions
        User currentUser = currentUserService.getCurrentUser();
        if (currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.SUPER_ADMIN) {
            throw new UnauthorizedException("Only admins can update bus parks");
        }

        BusPark busPark = getBusParkById(id);

        busPark.setName(busParkDTO.getName());
        busPark.setLocation(busParkDTO.getLocation());
        busPark.setAddress(busParkDTO.getAddress());
        busPark.setContactNumber(busParkDTO.getContactNumber());
        busPark.setLatitude(busParkDTO.getLatitude());
        busPark.setLongitude(busParkDTO.getLongitude());

        return busParkRepository.save(busPark);
    }

    public void deleteBusPark(Long id) {
        // Check permissions
        User currentUser = currentUserService.getCurrentUser();
        if (currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.SUPER_ADMIN) {
            throw new UnauthorizedException("Only admins can delete bus parks");
        }

        BusPark busPark = getBusParkById(id);
        busPark.setActive(false); // Soft delete
        busParkRepository.save(busPark);
    }
}
