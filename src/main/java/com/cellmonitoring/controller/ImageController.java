package com.cellmonitoring.controller;

import com.cellmonitoring.model.ImageData;
import com.cellmonitoring.service.ExperimentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    private final ExperimentService experimentService;

    @GetMapping("/{id}/details")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getImageDetails(@PathVariable Long id) {
        // Здесь можно добавить логику получения деталей изображения
        Map<String, Object> response = new HashMap<>();

        // Временный ответ
        response.put("id", id);
        response.put("filename", "experiment_1_image_" + id + ".tiff");
        response.put("confluency", 75.5);
        response.put("cellCount", 4200);
        response.put("avgCellSize", 17.2);
        response.put("segmentationQuality", 0.95);
        response.put("artifacts", 1);

        return ResponseEntity.ok(response);
    }


}