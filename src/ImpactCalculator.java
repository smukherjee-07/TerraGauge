import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class ImpactCalculator {
    private final Map<String, EmissionFactor> factorRegistry = new HashMap<>();

    public void loadEmissionFactors(String csvFilePath) throws IOException {
        factorRegistry.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFilePath))) {
            String line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    ActivityCategory cat = ActivityCategory.fromString(parts[0].trim());
                    String name = parts[1].trim();
                    double factorVal = Double.parseDouble(parts[2].trim());
                    String unit = parts[3].trim();
                    String source = parts[4].trim();

                    EmissionFactor ef = new EmissionFactor(cat, name, factorVal, unit, source);
                    factorRegistry.put(name.toLowerCase(), ef);
                }
            }
        }
    }

    public EmissionFactor getFactor(String activityName) {
        String searchTerm = activityName.toLowerCase().trim();
        EmissionFactor factor = factorRegistry.get(searchTerm);
        if (factor != null) {
            return factor;
        }

        List<EmissionFactor> matches = new ArrayList<>();
        for (EmissionFactor candidate : factorRegistry.values()) {
            if (candidate.getActivityName().toLowerCase().contains(searchTerm)) {
                matches.add(candidate);
            }
        }

        if (matches.size() == 1) {
            return matches.get(0);
        }
        if (matches.size() > 1) {
            throw new IllegalArgumentException("Activity identifier '" + activityName
                    + "' is ambiguous. Choose one of: " + formatActivityNames(matches));
        }
        throw new NoSuchElementException("No activity matched '" + activityName
                + ". Choose one of: " + formatActivityNames(factorRegistry.values()));
    }

    private String formatActivityNames(Collection<EmissionFactor> factors) {
        List<String> names = new ArrayList<>();
        for (EmissionFactor factor : factors) {
            names.add(factor.getActivityName());
        }
        return String.join(", ", names);
    }

    public ImpactRecord calculateSingle(Activity activity) {
        EmissionFactor ef = getFactor(activity.getActivityName());
        double co2e = activity.calculateEstimatedCo2e(ef.getFactor());
        return new ImpactRecord(activity.getActivityId(), ef.getFactor(), co2e);
    }

    public List<ImpactRecord> processBatch(List<Activity> activities) {
        int threads = Math.max(1, Runtime.getRuntime().availableProcessors());
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        List<Future<ImpactRecord>> futures = new ArrayList<>();
        List<ImpactRecord> results = new ArrayList<>();

        try {
            for (Activity act : activities) {
                Callable<ImpactRecord> task = () -> {
                    EmissionFactor ef = getFactor(act.getActivityName());
                    double co2e = act.calculateEstimatedCo2e(ef.getFactor());
                    return new ImpactRecord(act.getActivityId(), ef.getFactor(), co2e);
                };
                futures.add(executor.submit(task));
            }

            for (Future<ImpactRecord> future : futures) {
                try {
                    results.add(future.get());
                } catch (ExecutionException e) {
                    System.err.println("Worker thread error: " + e.getCause().getMessage());
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Batch processing was interrupted: " + e.getMessage());
        } finally {
            executor.shutdown();
        }
        return results;
    }

    public Map<String, EmissionFactor> getRegistry() {
        return Collections.unmodifiableMap(factorRegistry);
    }
}