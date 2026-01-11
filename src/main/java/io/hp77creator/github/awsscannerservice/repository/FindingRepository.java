package io.hp77creator.github.awsscannerservice.repository;

import io.hp77creator.github.awsscannerservice.models.db.Finding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface FindingRepository extends JpaRepository<Finding, Long>, JpaSpecificationExecutor<Finding> {
}
