package com.cellmonitoring.model.dto;

import lombok.Data;

@Data
public class ReportRequestDTO {
    private boolean includeCharts = true;
    private boolean includeImages = false;
    private boolean includeRawData = false;
    private String language = "ru";  // ru или en
}