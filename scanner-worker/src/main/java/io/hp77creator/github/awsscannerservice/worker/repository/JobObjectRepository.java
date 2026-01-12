package io.hp77creator.github.awsscannerservice.worker.repository;

import io.hp77creator.github.awsscannerservice.common.model.JobObject;
import io.hp77creator.github.awsscannerservice.common.model.JobObjectId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for JobObject entity.
 * Used by the worker to update job_object status during processing.
 */
@Repository
public interface JobObjectRepository extends JpaRepository<JobObject, JobObjectId> {
}
