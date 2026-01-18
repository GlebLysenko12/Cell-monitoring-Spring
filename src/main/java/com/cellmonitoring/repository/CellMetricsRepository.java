package com.cellmonitoring.repository;

import com.cellmonitoring.model.CellMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CellMetricsRepository extends JpaRepository<CellMetrics, Long> {
    List<CellMetrics> findByExperimentId(Long experimentId);
    List<CellMetrics> findByExperimentIdOrderByTimestampAsc(Long experimentId);

}