package io.hp77creator.github.awsscannerservice.util;

import io.hp77creator.github.awsscannerservice.models.db.Finding;
import org.springframework.data.jpa.domain.Specification;

public class FindingSpecification {
    public static Specification<Finding> hasBucket(String bucket) {
        return (root, query, cb) ->
                bucket == null ? null : cb.equal(root.get("bucket"), bucket);
    }
    public static Specification<Finding> hasKeyStartingWith(String prefix) {
        return (root, query, cb) ->
                prefix == null ? null : cb.like(root.get("key"), prefix + "%");
    }
}
