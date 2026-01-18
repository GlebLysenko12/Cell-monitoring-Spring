package com.cellmonitoring.service;

import com.cellmonitoring.model.BoundaryValues;
import com.cellmonitoring.model.CellMetrics;
import com.cellmonitoring.model.EnvironmentParams;
import com.cellmonitoring.model.Experiment;
import com.cellmonitoring.model.ShootingParams;
import com.cellmonitoring.model.dto.ReportRequestDTO;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
public class ReportService {

    private Font russianFont;
    private Font russianBoldFont;
    private Font russianTitleFont;

    // Инициализация шрифтов с поддержкой кириллицы
    private void initFonts() throws Exception {
        try {
            com.lowagie.text.pdf.BaseFont baseFont = com.lowagie.text.pdf.BaseFont.createFont(
                    "c:/windows/fonts/arial.ttf",
                    com.lowagie.text.pdf.BaseFont.IDENTITY_H,
                    com.lowagie.text.pdf.BaseFont.EMBEDDED
            );

            russianFont = new Font(baseFont, 12, Font.NORMAL);
            russianBoldFont = new Font(baseFont, 12, Font.BOLD);
            russianTitleFont = new Font(baseFont, 18, Font.BOLD);

            log.info("Используется шрифт Arial для кириллицы");
        } catch (Exception e) {
            try {
                com.lowagie.text.pdf.BaseFont baseFont = com.lowagie.text.pdf.BaseFont.createFont(
                        "c:/windows/fonts/times.ttf",
                        com.lowagie.text.pdf.BaseFont.IDENTITY_H,
                        com.lowagie.text.pdf.BaseFont.EMBEDDED
                );

                russianFont = new Font(baseFont, 12, Font.NORMAL);
                russianBoldFont = new Font(baseFont, 12, Font.BOLD);
                russianTitleFont = new Font(baseFont, 18, Font.BOLD);

                log.info("Используется шрифт Times New Roman для кириллицы");
            } catch (Exception e2) {
                log.warn("Не удалось загрузить системные шрифты, используем альтернативный вариант");
                com.lowagie.text.pdf.BaseFont baseFont = com.lowagie.text.pdf.BaseFont.createFont(
                        com.lowagie.text.pdf.BaseFont.HELVETICA,
                        com.lowagie.text.pdf.BaseFont.CP1252,
                        com.lowagie.text.pdf.BaseFont.EMBEDDED
                );

                russianFont = new Font(baseFont, 12, Font.NORMAL);
                russianBoldFont = new Font(baseFont, 12, Font.BOLD);
                russianTitleFont = new Font(baseFont, 18, Font.BOLD);
            }
        }
    }

    public byte[] generatePdfReport(Experiment experiment, ReportRequestDTO request) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            // Инициализируем шрифты
            initFonts();

            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, outputStream);

            document.open();

            // Заголовок отчёта
            Paragraph title = new Paragraph("Отчет по эксперименту: " + experiment.getName(), russianTitleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(Chunk.NEWLINE);

            // Основная информация
            addMainInfo(document, experiment);

            // Параметры среды
            addEnvironmentParams(document, experiment);

            // Параметры съёмки
            addShootingParams(document, experiment);

            // Граничные значения
            addBoundaryValues(document, experiment);

            // Статистика и метрики
            addStatisticsAndMetrics(document, experiment);

            // Подпись
            addSignature(document);

            document.close();
            log.info("PDF отчет успешно сгенерирован");
            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("Ошибка генерации PDF-отчета", e);
            throw new RuntimeException("Не удалось сгенерировать отчёт: " + e.getMessage(), e);
        }
    }

    private void addMainInfo(Document document, Experiment experiment) throws DocumentException {
        Paragraph mainInfoHeader = new Paragraph("Основная информация", russianBoldFont);
        mainInfoHeader.setSpacingBefore(10);
        mainInfoHeader.setSpacingAfter(5);
        document.add(mainInfoHeader);

        PdfPTable mainTable = createTwoColumnTable();
        addRow(mainTable, "ID эксперимента", experiment.getId().toString());
        addRow(mainTable, "Название", experiment.getName());
        addRow(mainTable, "Тип клеток", experiment.getCellType());
        addRow(mainTable, "Описание",
                experiment.getDescription() != null && !experiment.getDescription().isEmpty()
                        ? experiment.getDescription() : "Нет описания");
        addRow(mainTable, "Дата начала",
                experiment.getStartDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
        addRow(mainTable, "Дата окончания",
                experiment.getEndDate() != null ?
                        experiment.getEndDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")) : "Не завершён");
        addRow(mainTable, "Статус",
                experiment.getStatus() != null ? experiment.getStatus().getDisplayName() : "Не указан");

        document.add(mainTable);
        document.add(Chunk.NEWLINE);
    }

    private void addEnvironmentParams(Document document, Experiment experiment) throws DocumentException {
        if (experiment.getEnvironmentParams() != null) {
            EnvironmentParams env = experiment.getEnvironmentParams();

            Paragraph envHeader = new Paragraph("Параметры среды", russianBoldFont);
            envHeader.setSpacingBefore(10);
            envHeader.setSpacingAfter(5);
            document.add(envHeader);

            PdfPTable envTable = createTwoColumnTable();
            addRow(envTable, "Целевая температура",
                    env.getTargetTemperature() != null ? env.getTargetTemperature() + " °C" : "Не задано");
            addRow(envTable, "Целевая влажность",
                    env.getTargetHumidity() != null ? env.getTargetHumidity() + " %" : "Не задано");
            addRow(envTable, "Целевой уровень CO₂",
                    env.getTargetCo2() != null ? env.getTargetCo2() + " %" : "Не задано");
            addRow(envTable, "Интервал измерений",
                    env.getMeasurementInterval() != null ? env.getMeasurementInterval() + " мин" : "Не задано");

            document.add(envTable);
            document.add(Chunk.NEWLINE);
        }
    }

    private void addShootingParams(Document document, Experiment experiment) throws DocumentException {
        if (experiment.getShootingParams() != null) {
            ShootingParams shoot = experiment.getShootingParams();

            Paragraph shootHeader = new Paragraph("Параметры съёмки", russianBoldFont);
            shootHeader.setSpacingBefore(10);
            shootHeader.setSpacingAfter(5);
            document.add(shootHeader);

            PdfPTable shootTable = createTwoColumnTable();
            addRow(shootTable, "Интервал съёмки",
                    shoot.getShootIntervalMinutes() != null ? shoot.getShootIntervalMinutes() + " мин" : "Не задано");
            addRow(shootTable, "Количество позиций",
                    shoot.getNumberOfPositions() != null ? shoot.getNumberOfPositions().toString() : "Не задано");
            addRow(shootTable, "Увеличение",
                    shoot.getMagnification() != null ? shoot.getMagnification() + "x" : "Не задано");
            addRow(shootTable, "Тип камеры",
                    shoot.getCameraType() != null ? shoot.getCameraType() : "Не задано");

            document.add(shootTable);
            document.add(Chunk.NEWLINE);
        }
    }

    private void addBoundaryValues(Document document, Experiment experiment) throws DocumentException {
        if (experiment.getBoundaryValues() != null) {
            BoundaryValues bounds = experiment.getBoundaryValues();

            Paragraph boundsHeader = new Paragraph("Граничные значения", russianBoldFont);
            boundsHeader.setSpacingBefore(10);
            boundsHeader.setSpacingAfter(5);
            document.add(boundsHeader);

            PdfPTable boundsTable = createTwoColumnTable();
            addRow(boundsTable, "Верхний порог конфлюэнтности",
                    bounds.getConfluencyThresholdHigh() != null ?
                            bounds.getConfluencyThresholdHigh() + " %" : "Не задано");
            addRow(boundsTable, "Нижний порог конфлюэнтности",
                    bounds.getConfluencyThresholdLow() != null ?
                            bounds.getConfluencyThresholdLow() + " %" : "Не задано");
            addRow(boundsTable, "Порог количества клеток",
                    bounds.getCellCountAlert() != null ?
                            bounds.getCellCountAlert().toString() : "Не задано");
            addRow(boundsTable, "Максимальное отклонение размера",
                    bounds.getSizeDeviationMax() != null ?
                            bounds.getSizeDeviationMax() + " мкм" : "Не задано");

            document.add(boundsTable);
            document.add(Chunk.NEWLINE);
        }
    }

    private void addStatisticsAndMetrics(Document document, Experiment experiment) throws DocumentException {
        Paragraph statsHeader = new Paragraph("Статистика", russianBoldFont);
        statsHeader.setSpacingBefore(10);
        statsHeader.setSpacingAfter(5);
        document.add(statsHeader);

        PdfPTable statsTable = createTwoColumnTable();
        int imageCount = experiment.getImages() != null ? experiment.getImages().size() : 0;
        int metricCount = experiment.getMetrics() != null ? experiment.getMetrics().size() : 0;

        addRow(statsTable, "Количество изображений", String.valueOf(imageCount));
        addRow(statsTable, "Количество метрик", String.valueOf(metricCount));

        // Расчет продолжительности
        if (experiment.getStartDate() != null && experiment.getEndDate() != null) {
            long durationHours = java.time.Duration.between(experiment.getStartDate(), experiment.getEndDate()).toHours();
            addRow(statsTable, "Продолжительность", durationHours + " часов");
        } else {
            addRow(statsTable, "Продолжительность", "Эксперимент активен");
        }

        // Расчет средних значений
        if (experiment.getMetrics() != null && !experiment.getMetrics().isEmpty()) {
            double avgConfluency = experiment.getMetrics().stream()
                    .mapToDouble(CellMetrics::getConfluency)
                    .average()
                    .orElse(0);
            double avgTemperature = experiment.getMetrics().stream()
                    .mapToDouble(CellMetrics::getTemperature)
                    .average()
                    .orElse(0);
            double avgHumidity = experiment.getMetrics().stream()
                    .mapToDouble(CellMetrics::getHumidity)
                    .average()
                    .orElse(0);

            addRow(statsTable, "Средняя конфлюэнтность", String.format("%.1f %%", avgConfluency));
            addRow(statsTable, "Средняя температура", String.format("%.1f °C", avgTemperature));
            addRow(statsTable, "Средняя влажность", String.format("%.1f %%", avgHumidity));
        }

        document.add(statsTable);
        document.add(Chunk.NEWLINE);

        // Таблица метрик (первые 15 записей)
        if (experiment.getMetrics() != null && !experiment.getMetrics().isEmpty()) {
            Paragraph metricsHeader = new Paragraph("Метрики эксперимента", russianBoldFont);
            metricsHeader.setSpacingBefore(10);
            metricsHeader.setSpacingAfter(5);
            document.add(metricsHeader);

            // Создаем таблицу для метрик
            PdfPTable metricsTable = new PdfPTable(6);
            metricsTable.setWidthPercentage(100);
            metricsTable.setWidths(new float[]{20, 15, 15, 15, 15, 15});
            metricsTable.setSpacingBefore(5);
            metricsTable.setSpacingAfter(5);

            // Заголовки таблицы
            addTableHeaderCell(metricsTable, "Время");
            addTableHeaderCell(metricsTable, "Конфлюэнтность");
            addTableHeaderCell(metricsTable, "Клеток");
            addTableHeaderCell(metricsTable, "Размер");
            addTableHeaderCell(metricsTable, "Температура");
            addTableHeaderCell(metricsTable, "Влажность");

            // Данные таблицы
            List<CellMetrics> metrics = experiment.getMetrics();
            int limit = Math.min(15, metrics.size());

            for (int i = 0; i < limit; i++) {
                CellMetrics metric = metrics.get(i);
                metricsTable.addCell(createTableCell(
                        metric.getTimestamp().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))));
                metricsTable.addCell(createTableCell(
                        String.format("%.1f %%", metric.getConfluency())));
                metricsTable.addCell(createTableCell(
                        String.valueOf(metric.getCellCount())));
                metricsTable.addCell(createTableCell(
                        String.format("%.1f мкм", metric.getAvgCellSize())));
                metricsTable.addCell(createTableCell(
                        String.format("%.1f °C", metric.getTemperature())));
                metricsTable.addCell(createTableCell(
                        String.format("%.1f %%", metric.getHumidity())));
            }

            document.add(metricsTable);

            if (metrics.size() > 15) {
                Paragraph moreMetrics = new Paragraph("... и еще " + (metrics.size() - 15) + " записей", russianFont);
                moreMetrics.setAlignment(Element.ALIGN_CENTER);
                document.add(moreMetrics);
            }

            document.add(Chunk.NEWLINE);
        }
    }

    private void addSignature(Document document) throws DocumentException {
        Paragraph signature = new Paragraph("Отчет сгенерирован: " +
                java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")),
                new Font(russianFont.getBaseFont(), 10, Font.ITALIC, Color.GRAY));
        signature.setAlignment(Element.ALIGN_RIGHT);
        signature.setSpacingBefore(20);
        document.add(signature);
    }

    private PdfPTable createTwoColumnTable() {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{40, 60});
        table.setSpacingBefore(5);
        table.setSpacingAfter(5);
        return table;
    }

    private void addRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, russianBoldFont));
        labelCell.setBorderWidth(1);
        labelCell.setPadding(8);
        labelCell.setBackgroundColor(new Color(240, 240, 240));
        labelCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        labelCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        PdfPCell valueCell = new PdfPCell(new Phrase(value != null ? value : "---", russianFont));
        valueCell.setBorderWidth(1);
        valueCell.setPadding(8);
        valueCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        valueCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private void addTableHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, russianBoldFont));
        cell.setBackgroundColor(new Color(220, 220, 220));
        cell.setBorderWidth(1);
        cell.setPadding(6);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private PdfPCell createTableCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, russianFont));
        cell.setBorderWidth(1);
        cell.setPadding(6);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        return cell;
    }
}