package com.example.JobTracker.Mapper;

import com.example.JobTracker.DTO.AdminResponse;
import com.example.JobTracker.Model.Admin;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AdminMapper {

    AdminResponse toResponse(Admin admin);

}