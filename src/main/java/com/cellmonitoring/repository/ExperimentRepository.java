package com.cellmonitoring.repository;

import com.cellmonitoring.model.Experiment;
import com.cellmonitoring.model.ExperimentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ExperimentRepository extends JpaRepository<Experiment, Long> {

    List<Experiment> findByUserId(Long userId);

    List<Experiment> findByStatus(ExperimentStatus status);

    List<Experiment> findByStartDateBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT e FROM Experiment e WHERE " +
            "LOWER(e.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.cellType) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Experiment> searchByKeyword(@Param("keyword") String keyword);

    @Query("SELECT e FROM Experiment e WHERE e.user.id = :userId AND e.status = 'COMPLETED'")
    List<Experiment> findCompletedExperimentsByUser(@Param("userId") Long userId);

    // ExperimentRepository.java
    @Query("SELECT e FROM Experiment e LEFT JOIN FETCH e.images WHERE e.id = :id")
    Optional<Experiment> findByIdWithImages(@Param("id") Long id);
}