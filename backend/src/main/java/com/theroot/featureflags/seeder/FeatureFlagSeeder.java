package com.theroot.featureflags.seeder;

import com.theroot.featureflags.model.FeatureFlag;
import com.theroot.featureflags.repository.FeatureFlagRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class FeatureFlagSeeder implements CommandLineRunner {

    private final FeatureFlagRepository repository;

    public FeatureFlagSeeder(FeatureFlagRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            return; // already seeded
        }

        repository.save(new FeatureFlag(
                "dark-mode",
                "Dark Mode",
                "Enable dark mode theme across the application",
                false
        ));
        repository.save(new FeatureFlag(
                "new-checkout",
                "New Checkout Flow",
                "Use the redesigned checkout experience",
                true
        ));
        repository.save(new FeatureFlag(
                "beta-reports",
                "Beta Reports Module",
                "Enable the new reporting dashboard for beta users",
                false
        ));
        repository.save(new FeatureFlag(
                "maintenance-mode",
                "Maintenance Mode",
                "Show maintenance banner to all users",
                false
        ));
        repository.save(new FeatureFlag(
                "ai-suggestions",
                "AI Suggestions",
                "Enable AI-powered product recommendations",
                true
        ));

        System.out.println("✓ Seeded 5 default feature flags.");
    }
}
