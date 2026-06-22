package com.theroot.featureflags.controller;

import com.theroot.featureflags.model.FeatureFlag;
import com.theroot.featureflags.repository.FeatureFlagRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/features")
@CrossOrigin(origins = "http://localhost:5173")
public class FeatureFlagController {

    private static final Logger log = LoggerFactory.getLogger(FeatureFlagController.class);

    private final FeatureFlagRepository repository;

    public FeatureFlagController(FeatureFlagRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<FeatureFlag> getAll() {
        log.debug("Fetching all feature flags");
        List<FeatureFlag> flags = repository.findAll();
        log.debug("Returning {} feature flags", flags.size());
        return flags;
    }

    @GetMapping("/{id}")
    public ResponseEntity<FeatureFlag> getById(@PathVariable Long id) {
        log.debug("Fetching feature flag by id: {}", id);
        return repository.findById(id)
                .map(flag -> {
                    log.debug("Found feature flag: {} (key={})", id, flag.getKey());
                    return ResponseEntity.ok(flag);
                })
                .orElseGet(() -> {
                    log.warn("Feature flag not found: id={}", id);
                    return ResponseEntity.notFound().build();
                });
    }

    @PostMapping
    public ResponseEntity<FeatureFlag> create(@Valid @RequestBody FeatureFlag flag) {
        log.info("Creating feature flag: key={}, name={}", flag.getKey(), flag.getName());

        if (repository.existsByKey(flag.getKey())) {
            log.warn("Duplicate feature flag key: {}", flag.getKey());
            return ResponseEntity.badRequest().build();
        }

        FeatureFlag saved = repository.save(flag);
        log.info("Created feature flag: id={}, key={}", saved.getId(), saved.getKey());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FeatureFlag> update(@PathVariable Long id,
                                               @Valid @RequestBody FeatureFlag updated) {
        log.info("Updating feature flag: id={}, key={}", id, updated.getKey());

        return repository.findById(id).map(existing -> {
            existing.setKey(updated.getKey());
            existing.setName(updated.getName());
            existing.setDescription(updated.getDescription());
            existing.setEnabled(updated.isEnabled());
            FeatureFlag saved = repository.save(existing);
            log.info("Updated feature flag: id={}, key={}", saved.getId(), saved.getKey());
            return ResponseEntity.ok(saved);
        }).orElseGet(() -> {
            log.warn("Feature flag not found for update: id={}", id);
            return ResponseEntity.notFound().build();
        });
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<FeatureFlag> toggle(@PathVariable Long id) {
        log.info("Toggling feature flag: id={}", id);

        return repository.findById(id).map(flag -> {
            boolean before = flag.isEnabled();
            flag.setEnabled(!before);
            FeatureFlag saved = repository.save(flag);
            log.info("Toggled feature flag: id={}, key={}, enabled: {} -> {}",
                    id, saved.getKey(), before, saved.isEnabled());
            return ResponseEntity.ok(saved);
        }).orElseGet(() -> {
            log.warn("Feature flag not found for toggle: id={}", id);
            return ResponseEntity.notFound().build();
        });
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("Deleting feature flag: id={}", id);

        if (repository.existsById(id)) {
            repository.deleteById(id);
            log.info("Deleted feature flag: id={}", id);
            return ResponseEntity.noContent().build();
        }

        log.warn("Feature flag not found for deletion: id={}", id);
        return ResponseEntity.notFound().build();
    }
}
