package com.theroot.featureflags.seeder;

import com.theroot.featureflags.model.FeatureFlag;
import com.theroot.featureflags.repository.FeatureFlagRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class FeatureFlagSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(FeatureFlagSeeder.class);

    private final FeatureFlagRepository repository;

    public FeatureFlagSeeder(FeatureFlagRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            log.info("Database already seeded ({} feature flags found), skipping", repository.count());
            return;
        }

        log.info("Seeding database with default feature flags...");

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

        log.info("Seeded 5 default feature flags successfully");
    }
}
