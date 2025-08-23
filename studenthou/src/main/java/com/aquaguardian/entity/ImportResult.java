package com.aquaguardian.entity;

import java.util.List;

public class ImportResult<T> {
    private List<T> importedData;
    private List<String> skippedReservoirs;
    private String message;
    private boolean success;

    public ImportResult() {}

    public ImportResult(List<T> importedData, List<String> skippedReservoirs, String message, boolean success) {
        this.importedData = importedData;
        this.skippedReservoirs = skippedReservoirs;
        this.message = message;
        this.success = success;
    }

    public List<T> getImportedData() {
        return importedData;
    }

    public void setImportedData(List<T> importedData) {
        this.importedData = importedData;
    }

    public List<String> getSkippedReservoirs() {
        return skippedReservoirs;
    }

    public void setSkippedReservoirs(List<String> skippedReservoirs) {
        this.skippedReservoirs = skippedReservoirs;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
} 