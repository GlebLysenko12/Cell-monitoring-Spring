package com.cellmonitoring.repository;

import com.cellmonitoring.model.ImageData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ImageDataRepository extends JpaRepository<ImageData, Long> {
    List<ImageData> findByExperimentId(Long experimentId);
    List<ImageData> findByExperimentIdOrderByCaptureTimeAsc(Long experimentId);
    
}