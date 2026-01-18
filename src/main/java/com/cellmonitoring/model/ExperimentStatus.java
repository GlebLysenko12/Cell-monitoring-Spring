package com.cellmonitoring.model;

public enum ExperimentStatus {
    ACTIVE("Активный"),
    COMPLETED("Завершен"),
    INTERRUPTED("Прерван"),
    FAILED("Ошибка");

    private final String displayName;

    ExperimentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    // Метод для отображения в интерфейсе
    public static ExperimentStatus fromString(String text) {
        for (ExperimentStatus status : ExperimentStatus.values()) {
            if (status.name().equalsIgnoreCase(text)) {
                return status;
            }
        }
        return ACTIVE;
    }
}