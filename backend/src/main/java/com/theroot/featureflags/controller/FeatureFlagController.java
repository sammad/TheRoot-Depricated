package com.theroot.featureflags.controller;

import com.theroot.featureflags.model.FeatureFlag;
import com.theroot.featureflags.repository.FeatureFlagRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/features")
@CrossOrigin(origins = "http://localhost:5173")
public class FeatureFlagController {

    private final FeatureFlagRepository repository;

    public FeatureFlagController(FeatureFlagRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<FeatureFlag> getAll() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<FeatureFlag> getById(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<FeatureFlag> create(@Valid @RequestBody FeatureFlag flag) {
        if (repository.existsByKey(flag.getKey())) {
            return ResponseEntity.badRequest().build();
        }
        FeatureFlag saved = repository.save(flag);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FeatureFlag> update(@PathVariable Long id,
                                               @Valid @RequestBody FeatureFlag updated) {
        return repository.findById(id).map(existing -> {
            existing.setKey(updated.getKey());
            existing.setName(updated.getName());
            existing.setDescription(updated.getDescription());
            existing.setEnabled(updated.isEnabled());
            return ResponseEntity.ok(repository.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<FeatureFlag> toggle(@PathVariable Long id) {
        return repository.findById(id).map(flag -> {
            flag.setEnabled(!flag.isEnabled());
            return ResponseEntity.ok(repository.save(flag));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
