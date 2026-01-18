package com.cellmonitoring.model;

import lombok.Data;
import jakarta.persistence.Embeddable;

@Embeddable
@Data
public class ShootingParams {
    private Integer shootIntervalMinutes;
    private Integer numberOfPositions;
    private Double magnification;
    private String cameraType;
}