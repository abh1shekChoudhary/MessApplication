package org.messplacement.messsecond.DTO;

import java.util.ArrayList;
import java.util.List;

/**
 * Summary result returned by the bulk upload endpoint.
 */
public class BulkUploadResult {

    private int successCount;
    private int failureCount;
    private List<String> errors = new ArrayList<>();

    public BulkUploadResult() {}

    public void incrementSuccess() { this.successCount++; }
    public void addError(int row, String reason) {
        this.failureCount++;
        this.errors.add(String.format("Row %d: %s", row, reason));
    }

    public int getSuccessCount() { return successCount; }
    public int getFailureCount() { return failureCount; }
    public List<String> getErrors() { return errors; }
}
