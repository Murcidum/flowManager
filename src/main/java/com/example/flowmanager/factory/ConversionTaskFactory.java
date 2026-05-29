package com.example.flowmanager.factory;

import com.example.flowmanager.entity.ConversionStatus;
import com.example.flowmanager.entity.ConversionTask;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ConversionTaskFactory {

    public ConversionTask createPending(String originalFileName, String sourceBucket, String sourceKey) {
        ConversionTask task = new ConversionTask();
        task.setOriginalFileName(originalFileName);
        task.setSourceBucket(sourceBucket);
        task.setSourceKey(sourceKey);
        task.setStatus(ConversionStatus.PENDING);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        return task;
    }
}
