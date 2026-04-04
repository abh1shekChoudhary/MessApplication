package org.messplacement.messsecond;

import org.messplacement.messsecond.DTO.BulkUploadResult;
import org.messplacement.messsecond.Service.BulkUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * POST /students/bulk — multipart file upload (CSV or XLSX).
 * Restricted to ADMIN only.
 *
 * Expected file format (header row required):
 *   reg | date       | breakfast | lunch | dinner
 *   22BAI10001 | 2025-04-01 | true | true | false
 */
@RestController
@RequestMapping("/students/bulk")
@CrossOrigin(origins = {
        "http://localhost:4200",
        "http://localhost",
        "https://mess-application-front-end-angular.vercel.app"
})
public class BulkUploadController {

    @Autowired
    private BulkUploadService bulkUploadService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BulkUploadResult> upload(
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        String name = file.getOriginalFilename();
        boolean validType = name != null &&
                (name.endsWith(".csv") || name.endsWith(".xlsx") || name.endsWith(".xls"));

        if (!validType) {
            BulkUploadResult error = new BulkUploadResult();
            error.addError(0, "Only .csv, .xlsx, and .xls files are accepted.");
            return ResponseEntity.badRequest().body(error);
        }

        return ResponseEntity.ok(bulkUploadService.processFile(file));
    }
}
