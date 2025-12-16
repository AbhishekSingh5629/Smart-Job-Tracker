package com.example.JobTracker.Mapper;

import com.example.JobTracker.DTO.JobResponse;
import com.example.JobTracker.DTO.UserResponse;
import com.example.JobTracker.Model.Job;
import com.example.JobTracker.Model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface JobMapper {
    JobResponse toResponse(Job job);
}