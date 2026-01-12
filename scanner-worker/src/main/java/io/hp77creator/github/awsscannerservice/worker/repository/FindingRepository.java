package io.hp77creator.github.awsscannerservice.worker.repository;

import io.hp77creator.github.awsscannerservice.common.model.Finding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for Finding entity.
 * Used by the worker to save findings detected during scanning.
 * Idempotency is handled by the unique constraint on (bucket, key, detector_type, byte_offset).
 */
@Repository
public interface FindingRepository extends JpaRepository<Finding, Long> {
}
