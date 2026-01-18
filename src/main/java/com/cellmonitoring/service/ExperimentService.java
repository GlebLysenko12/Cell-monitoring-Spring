package com.cellmonitoring.service;

import com.cellmonitoring.model.Experiment;
import com.cellmonitoring.model.dto.ExperimentDTO;
import com.cellmonitoring.repository.CellMetricsRepository;
import com.cellmonitoring.repository.ExperimentRepository;
import com.cellmonitoring.model.dto.ReportRequestDTO;
import com.cellmonitoring.repository.ImageDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ExperimentService {

    private final ExperimentRepository experimentRepository;
    private final CellMetricsRepository cellMetricsRepository;
    private final ImageDataRepository imageDataRepository;
    private final ReportService reportService;

    public List<ExperimentDTO> getAllExperimentsForUser(Long userId) {
        return experimentRepository.findByUserId(userId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }


    // ExperimentService.java
    public Optional<Experiment> getExperimentById(Long id) {
        return experimentRepository.findByIdWithImages(id);   //
    }


    @Transactional
    public void deleteExperiment(Long id) {
        experimentRepository.deleteById(id);
    }

    public List<ExperimentDTO> searchExperiments(String keyword) {
        return experimentRepository.searchByKeyword(keyword)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private ExperimentDTO convertToDTO(Experiment experiment) {
        ExperimentDTO dto = new ExperimentDTO();
        dto.setId(experiment.getId());
        dto.setName(experiment.getName());
        dto.setDescription(experiment.getDescription());
        dto.setCellType(experiment.getCellType());
        dto.setStartDate(experiment.getStartDate());
        dto.setEndDate(experiment.getEndDate());
        dto.setStatus(experiment.getStatus());

        // Получаем количество изображений из репозитория
        int imageCount = imageDataRepository.findByExperimentId(experiment.getId()).size();
        dto.setImageCount(imageCount);

        if (experiment.getStartDate() != null && experiment.getEndDate() != null) {
            dto.setDurationHours(Duration.between(experiment.getStartDate(), experiment.getEndDate()).toHours());
        } else {
            dto.setDurationHours(0L);
        }

        return dto;
    }

    public byte[] generateExperimentReport(Long experimentId, ReportRequestDTO request) {
        Optional<Experiment> experiment = getExperimentById(experimentId);
        if (experiment.isEmpty()) {
            throw new RuntimeException("Experiment not found");
        }

        return reportService.generatePdfReport(experiment.get(), request);
    }
}
