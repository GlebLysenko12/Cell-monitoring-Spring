package com.cellmonitoring.controller;

import com.cellmonitoring.model.Experiment;
import com.cellmonitoring.model.dto.ExperimentDTO;
import com.cellmonitoring.model.dto.ReportRequestDTO;
import com.cellmonitoring.service.ExperimentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/experiments")
@RequiredArgsConstructor
public class ExperimentController {

    private final ExperimentService experimentService;

    @GetMapping
    public String listExperiments(@RequestParam(required = false) String keyword,
                                  Model model) {
        List<ExperimentDTO> experiments;

        if (keyword != null && !keyword.isEmpty()) {
            experiments = experimentService.searchExperiments(keyword);
        } else {
            Long userId = 1L;
            experiments = experimentService.getAllExperimentsForUser(userId);
        }

        model.addAttribute("experiments", experiments);
        model.addAttribute("keyword", keyword);
        return "experiment/list";
    }


    @GetMapping("/{id}")
    public String getExperimentDetails(@PathVariable Long id, Model model) {
        // Шаг 2: Получаем эксперимент (с изображениями!)
        Experiment experiment = experimentService.getExperimentById(id)
                .orElseThrow(() -> new RuntimeException("Experiment not found"));

        // Передаём в шаблон
        model.addAttribute("experiment", experiment);
        model.addAttribute("images", experiment.getImages());     // ← здесь уже 9 элементов!
        model.addAttribute("metrics", experiment.getMetrics());

        return "experiment/details";
    }

    @PostMapping("/{id}/delete")
    public String deleteExperiment(@PathVariable Long id) {
        experimentService.deleteExperiment(id);
        return "redirect:/experiments";
    }


    @PostMapping("/{id}/report/generate")
    public ResponseEntity<Resource> generateReport(@PathVariable Long id,
                                                   @ModelAttribute ReportRequestDTO request) {
        try {
            byte[] reportContent = experimentService.generateExperimentReport(id, request);

            ByteArrayResource resource = new ByteArrayResource(reportContent);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=experiment_report_" + id + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(reportContent.length)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ByteArrayResource(("Ошибка генерации отчета: " + e.getMessage()).getBytes()));
        }
    }
}
