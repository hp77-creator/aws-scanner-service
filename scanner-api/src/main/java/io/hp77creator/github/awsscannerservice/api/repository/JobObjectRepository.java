package io.hp77creator.github.awsscannerservice.api.repository;

import io.hp77creator.github.awsscannerservice.common.model.JobObject;
import io.hp77creator.github.awsscannerservice.common.model.JobObjectId;
import io.hp77creator.github.awsscannerservice.common.model.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface JobObjectRepository extends JpaRepository<JobObject, JobObjectId> {
    
    long countByIdJobIdAndStatus(UUID jobId, JobStatus status);
    
    /**
     * Get aggregated counts for all statuses for a given job
     */
    @Query("SELECT jo.status, COUNT(jo) FROM job_objects jo WHERE jo.id.jobId = :jobId GROUP BY jo.status")
    Object[][] countByJobIdGroupByStatus(@Param("jobId") UUID jobId);
}
