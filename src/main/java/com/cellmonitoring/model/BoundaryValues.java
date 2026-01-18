package com.cellmonitoring.model;

import lombok.Data;
import jakarta.persistence.Embeddable;

@Embeddable
@Data
public class BoundaryValues {
    private Double confluencyThresholdHigh;
    private Double confluencyThresholdLow;
    private Integer cellCountAlert;
    private Double sizeDeviationMax;
}