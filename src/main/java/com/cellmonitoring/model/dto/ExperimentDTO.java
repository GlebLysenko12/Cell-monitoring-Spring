package com.cellmonitoring.model.dto;

import com.cellmonitoring.model.ExperimentStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ExperimentDTO {
    private Long id;
    private String name;
    private String description;
    private String cellType;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private ExperimentStatus status;
    private Integer imageCount;
    private Long durationHours;
}