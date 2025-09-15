package com.example.mappers;

import com.example.dto.responses.ProfileResponse;
import com.example.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    ProfileResponse toProfileResponse(User user);
}
