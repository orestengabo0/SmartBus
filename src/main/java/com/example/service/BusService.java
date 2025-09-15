package com.example.service;

import com.example.dto.requests.BusRequest;
import com.example.dto.responses.BusResponse;
import com.example.exception.ResourceAlreadyExistsException;
import com.example.exception.ResourceNotFoundException;
import com.example.exception.UnauthorizedException;
import com.example.mappers.BusMapper;
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
    private final BusMapper busMapper;

    public BusResponse createBus(BusRequest busRequest) {
        // Validate
        if (busRepository.findByPlateNumber(busRequest.getPlateNumber()).isPresent()) {
            throw new ResourceAlreadyExistsException("A bus with this plate number already exists");
        }

        User currentUser = currentUserService.getCurrentUser();
        User operator;

        if(currentUser.getRole() == Role.OPERATOR){
            operator = currentUser;
        } else if (currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.SUPER_ADMIN) {
            //admin can assign buses to any operator
            if(busRequest.getOperatorId() != null){
                operator = userRepository.findById(busRequest.getOperatorId())
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
        BusPark busPark = busParkRepository.findById(busRequest.getBusParkId())
                .orElseThrow(() -> new ResourceNotFoundException("Bus park not found"));

        Bus bus = new Bus();
        bus.setPlateNumber(busRequest.getPlateNumber());
        bus.setTotalSeats(busRequest.getTotalSeats());
        bus.setCurrentBusPark(busPark);
        bus.setOperator(operator);
        bus.setBusModel(busRequest.getBusModel());
        bus.setBusType(busRequest.getBusType());
        bus.setYearOfManufacture(busRequest.getYearOfManufacture());
        bus.setCreatedAt(LocalDateTime.now());

        // Initialize seats
        bus.initializeSeats();

        busRepository.save(bus);
        return busMapper.toBusResponse(bus);
    }

    public List<BusResponse> getAllBuses() {
        User currentUser = currentUserService.getCurrentUser();
        List<Bus> buses;

        if (currentUser.getRole() == Role.OPERATOR) {
            // Operators can only see their own buses
            buses = busRepository.findByOperator(currentUser);
        } else {
            // Admins can see all buses
            buses = busRepository.findByActive(true);
        }
        return busMapper.toBusResponseList(buses);
    }

    public BusResponse getBusById(Long id) {
        Bus bus = busRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bus not found with id: " + id));

        User currentUser = currentUserService.getCurrentUser();

        // Check if operator has access to this bus
        if (currentUser.getRole() == Role.OPERATOR &&
                !bus.getOperator().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You don't have access to this bus");
        }

        return busMapper.toBusResponse(bus);
    }

    public BusResponse updateBus(Long id, BusRequest busRequest) {
        Bus bus = busRepository.getBusById(id);

        // Check permissions - only operator of this bus or admin can update
        User currentUser = currentUserService.getCurrentUser();
        if (currentUser.getRole() == Role.OPERATOR &&
                !bus.getOperator().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You can only update your own buses");
        }

        // Update fields
        if (busRequest.getPlateNumber() != null) {
            bus.setPlateNumber(busRequest.getPlateNumber());
        }

        if (busRequest.getBusModel() != null) {
            bus.setBusModel(busRequest.getBusModel());
        }

        if (busRequest.getBusType() != null) {
            bus.setBusType(busRequest.getBusType());
        }

        if (busRequest.getYearOfManufacture() > 0) {
            bus.setYearOfManufacture(busRequest.getYearOfManufacture());
        }

        if (busRequest.getBusParkId() != null) {
            BusPark busPark = busParkRepository.findById(busRequest.getBusParkId())
                    .orElseThrow(() -> new ResourceNotFoundException("Bus park not found"));
            bus.setCurrentBusPark(busPark);
        }

        // Admin can reassign bus to different operator
        if ((currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.SUPER_ADMIN)
                && busRequest.getOperatorId() != null) {
            User operator = userRepository.findById(busRequest.getOperatorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Operator not found"));

            if (operator.getRole() != Role.OPERATOR) {
                throw new IllegalArgumentException("User is not an operator");
            }

            bus.setOperator(operator);
        }

        busRepository.save(bus);
        return busMapper.toBusResponse(bus);
    }

    public void deleteBus(Long id) {
        Bus bus = busRepository.getBusById(id);

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
