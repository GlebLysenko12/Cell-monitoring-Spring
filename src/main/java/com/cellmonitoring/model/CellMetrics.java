package com.cellmonitoring.model;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cell_metrics")
@Data
public class CellMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private Double confluency;

    @Column(nullable = false)
    private Integer cellCount;

    private Double avgCellSize;
    private Double cellDensity;

    private Double temperature;
    private Double humidity;
    private Double co2Level;

    @ManyToOne
    @JoinColumn(name = "experiment_id", nullable = false)
    private Experiment experiment;
}