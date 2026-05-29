package com.example.flowmanager.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "conversion_tasks")
@Getter
@Setter
@NoArgsConstructor
public class ConversionTask {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "original_file_name", nullable = false)
    private String originalFileName;

    @Column(name = "source_bucket", nullable = false)
    private String sourceBucket;

    @Column(name = "source_key", nullable = false)
    private String sourceKey;

    @Column(name = "result_bucket")
    private String resultBucket;

    @Column(name = "result_key")
    private String resultKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConversionStatus status;

    @Column(name = "error_message", columnDefinition = "text")
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

}
