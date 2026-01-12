package io.hp77creator.github.awsscannerservice.api.util;

import io.hp77creator.github.awsscannerservice.common.model.Finding;
import org.springframework.data.jpa.domain.Specification;

public class FindingSpecification {
    
    public static Specification<Finding> hasBucket(String bucket) {
        return (root, query, criteriaBuilder) -> 
            bucket == null ? null : criteriaBuilder.equal(root.get("bucket"), bucket);
    }
    
    public static Specification<Finding> hasKeyStartingWith(String prefix) {
        return (root, query, criteriaBuilder) -> 
            prefix == null ? null : criteriaBuilder.like(root.get("key"), prefix + "%");
    }
}
