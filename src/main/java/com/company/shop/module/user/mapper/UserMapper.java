package com.company.shop.module.user.mapper;

import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.company.shop.module.user.dto.UserResponseDTO;
import com.company.shop.module.user.entity.Role;
import com.company.shop.module.user.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

	@Mapping(target = "roles", source = "roles", qualifiedByName = "mapRoles")
	UserResponseDTO toDto(User user);

	@Named("mapRoles")
	default Set<String> mapRoles(Set<Role> roles) {
		if (roles == null) {
			return null;
		}
		return roles.stream().map(Role::getName).collect(Collectors.toSet());
	}
}