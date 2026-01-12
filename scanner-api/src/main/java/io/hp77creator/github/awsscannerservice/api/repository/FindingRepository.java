package io.hp77creator.github.awsscannerservice.api.repository;

import io.hp77creator.github.awsscannerservice.common.model.Finding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface FindingRepository extends JpaRepository<Finding, Long>, JpaSpecificationExecutor<Finding> {
}
