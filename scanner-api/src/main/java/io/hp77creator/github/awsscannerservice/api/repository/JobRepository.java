package io.hp77creator.github.awsscannerservice.api.repository;

import io.hp77creator.github.awsscannerservice.common.model.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface JobRepository extends JpaRepository<Job, UUID> {
}
