package com.cellmonitoring.model;

import lombok.Data;
import jakarta.persistence.Embeddable;

@Embeddable
@Data
public class EnvironmentParams {
    private Double targetTemperature;
    private Double targetHumidity;
    private Double targetCo2;
    private Integer measurementInterval;
}
