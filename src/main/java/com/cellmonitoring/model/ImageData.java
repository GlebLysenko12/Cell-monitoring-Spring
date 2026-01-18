package com.cellmonitoring.model;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "image_data")
@Data
public class ImageData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String filename;

    @Column(nullable = false)
    private String filePath;

    @Column(nullable = false)
    private LocalDateTime captureTime;

    @Column(nullable = false)
    private Integer imageNumber;

    @Lob
    private String analysisResultJson;

    private Double confluency;
    private Integer cellCount;
    private Double avgCellSize;

    @ManyToOne
    @JoinColumn(name = "experiment_id", nullable = false)
    private Experiment experiment;

    // ✅ ДОБАВЬТЕ ЭТОТ МЕТОД ДЛЯ ПОЛУЧЕНИЯ ВЕБ-ПУТИ
    @Transient  // Не сохраняется в БД
    public String getWebImagePath() {
        // Если в базе хранится физический путь, преобразуем его
        String webPath = this.filePath;

        // Преобразование физических путей в веб-пути
        if (webPath.startsWith("/data/images/")) {
            webPath = webPath.replace("/data/images/", "/uploads/images/");
        }

        // Убедимся, что путь начинается с /
        if (!webPath.startsWith("/")) {
            webPath = "/" + webPath;
        }

        // Убедимся, что путь заканчивается с /
        if (!webPath.endsWith("/")) {
            webPath = webPath + "/";
        }

        return webPath + this.filename;
    }

    // ✅ АЛЬТЕРНАТИВНЫЙ МЕТОД - всегда возвращает правильный путь
    @Transient
    public String getStaticImagePath() {
        return "/uploads/images/" + this.filename;
    }

    // ✅ МЕТОД для тестовых изображений (если реальных нет)
    @Transient
    public String getPlaceholderImagePath() {
        return "https://via.placeholder.com/300x200/667eea/ffffff?text=Cell+" + this.imageNumber;
    }

}
