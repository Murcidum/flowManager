package com.example.flowmanager.controller;

import com.example.flowmanager.dto.ConversionStatusResponse;
import com.example.flowmanager.dto.UploadResponse;
import com.example.flowmanager.service.FlowManagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class ConversionController {

    private final FlowManagerService flowManagerService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponse> upload(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.accepted().body(flowManagerService.upload(file));
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<ConversionStatusResponse> getStatus(@PathVariable UUID id) {
        return ResponseEntity.ok(flowManagerService.getStatus(id));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(@PathVariable UUID id) {
        byte[] content = flowManagerService.downloadResult(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"converted_" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(content);
    }
}
