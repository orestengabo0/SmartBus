package com.example.mappers;

import com.example.dto.responses.ProfileResponse;
import com.example.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "id", source = "user.id")
    @Mapping(target = "fullName", source = "user.id")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "phone", source = "user.phone")
    @Mapping(target = "role", source = "user.role")
    @Mapping(target = "createdAt", source = "user.createdAt")
    ProfileResponse toProfileResponse(User user);
}
