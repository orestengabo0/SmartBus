package com.example.service;

import com.example.dto.BusDTO;
import com.example.exception.ResourceAlreadyExistsException;
import com.example.exception.ResourceNotFoundException;
import com.example.exception.UnauthorizedException;
import com.example.model.Bus;
import com.example.model.BusPark;
import com.example.model.Role;
import com.example.model.User;
import com.example.repository.BusParkRepository;
import com.example.repository.BusRepository;
import com.example.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class BusService {
    private final BusRepository busRepository;
    private final UserRepository userRepository;
    private final BusParkRepository busParkRepository;
    private final CurrentUserService currentUserService;

    public Bus createBus(BusDTO busDTO) {
        // Validate
        if (busRepository.findByPlateNumber(busDTO.getPlateNumber()).isPresent()) {
            throw new ResourceAlreadyExistsException("A bus with this plate number already exists");
        }

        User currentUser = currentUserService.getCurrentUser();
        User operator;

        if(currentUser.getRole() == Role.OPERATOR){
            operator = currentUser;
        } else if (currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.SUPER_ADMIN) {
            //admin can assign buses to any operator
            if(busDTO.getOperatorId() != null){
                operator = userRepository.findById(busDTO.getOperatorId())
                        .orElseThrow(() -> new ResourceNotFoundException("Operator not found"));
                if(operator.getRole() != Role.OPERATOR){
                    throw new IllegalArgumentException("Operator is not a operator");
                }
            }else {
                throw new IllegalArgumentException("Operator ID is required");
            }
        } else {
            throw new UnauthorizedException("You don't have permission to create a bus");
        }

        // Get bus park
        BusPark busPark = busParkRepository.findById(busDTO.getBusParkId())
                .orElseThrow(() -> new ResourceNotFoundException("Bus park not found"));

        Bus bus = new Bus();
        bus.setPlateNumber(busDTO.getPlateNumber());
        bus.setTotalSeats(busDTO.getTotalSeats());
        bus.setCurrentBusPark(busPark);
        bus.setOperator(operator);
        bus.setBusModel(busDTO.getBusModel());
        bus.setBusType(busDTO.getBusType());
        bus.setYearOfManufacture(busDTO.getYearOfManufacture());
        bus.setCreatedAt(LocalDateTime.now());

        // Initialize seats
        bus.initializeSeats();

        return busRepository.save(bus);
    }

    public List<Bus> getAllBuses() {
        User currentUser = currentUserService.getCurrentUser();

        if (currentUser.getRole() == Role.OPERATOR) {
            // Operators can only see their own buses
            return busRepository.findByOperator(currentUser);
        } else {
            // Admins can see all buses
            return busRepository.findByActive(true);
        }
    }

    public Bus getBusById(Long id) {
        Bus bus = busRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bus not found with id: " + id));

        User currentUser = currentUserService.getCurrentUser();

        // Check if operator has access to this bus
        if (currentUser.getRole() == Role.OPERATOR &&
                !bus.getOperator().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You don't have access to this bus");
        }

        return bus;
    }

    public Bus updateBus(Long id, BusDTO busDTO) {
        Bus bus = getBusById(id);

        // Check permissions - only operator of this bus or admin can update
        User currentUser = currentUserService.getCurrentUser();
        if (currentUser.getRole() == Role.OPERATOR &&
                !bus.getOperator().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You can only update your own buses");
        }

        // Update fields
        if (busDTO.getPlateNumber() != null) {
            bus.setPlateNumber(busDTO.getPlateNumber());
        }

        if (busDTO.getBusModel() != null) {
            bus.setBusModel(busDTO.getBusModel());
        }

        if (busDTO.getBusType() != null) {
            bus.setBusType(busDTO.getBusType());
        }

        if (busDTO.getYearOfManufacture() > 0) {
            bus.setYearOfManufacture(busDTO.getYearOfManufacture());
        }

        if (busDTO.getBusParkId() != null) {
            BusPark busPark = busParkRepository.findById(busDTO.getBusParkId())
                    .orElseThrow(() -> new ResourceNotFoundException("Bus park not found"));
            bus.setCurrentBusPark(busPark);
        }

        // Admin can reassign bus to different operator
        if ((currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.SUPER_ADMIN)
                && busDTO.getOperatorId() != null) {
            User operator = userRepository.findById(busDTO.getOperatorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Operator not found"));

            if (operator.getRole() != Role.OPERATOR) {
                throw new IllegalArgumentException("User is not an operator");
            }

            bus.setOperator(operator);
        }

        return busRepository.save(bus);
    }

    public void deleteBus(Long id) {
        Bus bus = getBusById(id);

        // Check permissions
        User currentUser = currentUserService.getCurrentUser();
        if (currentUser.getRole() == Role.OPERATOR &&
                !bus.getOperator().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You can only delete your own buses");
        }

        bus.setActive(false); // Soft delete
        busRepository.save(bus);
    }
}
