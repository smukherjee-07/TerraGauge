import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class TerraGaugeTest {

    private static int totalTests = 0;
    private static int passedTests = 0;

    public static void main(String[] args) {
        System.out.println("Starting TerraGauge Test Suite...\n");

        testValidActivityCreation();
        testInvalidActivityQuantity();
        testCategoryParsing();
        testEmissionFactorLookup();
            testPartialEmissionFactorLookup();
        testMissingEmissionFactor();
        testImpactCalculationFormula();
        testBatchImpactCalculation();

        System.out.println("\nTest Execution Complete.");
        System.out.printf("Results: %d/%d assertions passed.\n", passedTests, totalTests);

        if (passedTests != totalTests) {
            System.err.println("Some tests encountered failures.");
            System.exit(1);
        } else {
            System.out.println("All unit tests passed successfully.");
        }
    }

    private static void assertTrue(boolean condition, String testName) {
        totalTests++;
        if (condition) {
            passedTests++;
            System.out.println("[PASS] " + testName);
        } else {
            System.err.println("[FAIL] " + testName);
        }
    }

    private static void testValidActivityCreation() {
        try {
            Activity act = new Activity(1, ActivityCategory.TRANSPORT, "Petrol Car", 15.5, "km", LocalDate.now());
            assertTrue(act.getQuantity() == 15.5 && act.getCategory() == ActivityCategory.TRANSPORT, 
                       "Valid Activity Creation and Encapsulation");
        } catch (Exception e) {
            assertTrue(false, "Valid Activity Creation failed with exception: " + e.getMessage());
        }
    }

    private static void testInvalidActivityQuantity() {
        boolean thrown = false;
        try {
            new Activity(1, ActivityCategory.ELECTRICITY, "Grid Electricity", -5.0, "kWh", LocalDate.now());
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        assertTrue(thrown, "Rejection of Negative Quantity in Activity");

        thrown = false;
        try {
            new Activity(1, ActivityCategory.ELECTRICITY, "Grid Electricity", 0.0, "kWh", LocalDate.now());
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        assertTrue(thrown, "Rejection of Zero Quantity in Activity");
    }

    private static void testCategoryParsing() {
        ActivityCategory cat = ActivityCategory.fromString("transport");
        assertTrue(cat == ActivityCategory.TRANSPORT, "Case-insensitive Category Parsing");

        boolean caught = false;
        try {
            ActivityCategory.fromString("INVALID_CATEGORY");
        } catch (IllegalArgumentException e) {
            caught = true;
        }
        assertTrue(caught, "Rejection of Undefined ActivityCategory");
    }

    private static void testEmissionFactorLookup() {
        ImpactCalculator calc = new ImpactCalculator();
        File tempCsv = null;
        try {
            tempCsv = File.createTempFile("test_factors", ".csv");
            try (FileWriter writer = new FileWriter(tempCsv)) {
                writer.write("category,activity,factor,unit,source\n");
                writer.write("Transport,Petrol Car,0.170,kg CO2e/km,DEFRA 2023\n");
                writer.write("Electricity,Grid Electricity,0.820,kg CO2e/kWh,CEA 2023\n");
            }

            calc.loadEmissionFactors(tempCsv.getAbsolutePath());
            EmissionFactor factor = calc.getFactor("Petrol Car");

            assertTrue(factor != null && Math.abs(factor.getFactor() - 0.170) < 0.0001,
                       "Lookup and Ingestion of Emission Factor from CSV");

        } catch (IOException e) {
            assertTrue(false, "EmissionFactorLookup CSV generation failed: " + e.getMessage());
        } finally {
            if (tempCsv != null && tempCsv.exists()) {
                tempCsv.delete();
            }
        }
    }

    private static void testMissingEmissionFactor() {
        ImpactCalculator calc = new ImpactCalculator();
        boolean thrown = false;
        try {
            calc.getFactor("NonExistentMachine");
        } catch (NoSuchElementException e) {
            thrown = true;
        }
        assertTrue(thrown, "Handling of Unregistered Emission Factor Name");
    }

    private static void testPartialEmissionFactorLookup() {
        ImpactCalculator calc = new ImpactCalculator();
        File tempCsv = null;
        try {
            tempCsv = File.createTempFile("partial_factors", ".csv");
            try (FileWriter writer = new FileWriter(tempCsv)) {
                writer.write("category,activity,factor,unit,source\n");
                writer.write("Waste,Landfill Waste,0.446,kg,DEFRA 2023\n");
            }

            calc.loadEmissionFactors(tempCsv.getAbsolutePath());
            EmissionFactor factor = calc.getFactor("landfill");

            assertTrue(factor.getActivityName().equals("Landfill Waste"),
                    "Unique Partial Emission Factor Lookup");
        } catch (IOException e) {
            assertTrue(false, "PartialEmissionFactorLookup CSV generation failed: " + e.getMessage());
        } finally {
            if (tempCsv != null && tempCsv.exists()) {
                tempCsv.delete();
            }
        }
    }

    private static void testImpactCalculationFormula() {
        Activity act = new Activity(1, ActivityCategory.TRANSPORT, "Petrol Car", 100.0, "km", LocalDate.now());
        double factor = 0.170;
        double expectedCo2e = 17.0; // 100 * 0.170

        double result = act.calculateEstimatedCo2e(factor);
        assertTrue(Math.abs(result - expectedCo2e) < 0.0001, "Accurate Single CO2e Calculation");
    }

    private static void testBatchImpactCalculation() {
        ImpactCalculator calc = new ImpactCalculator();
        File tempCsv = null;
        try {
            tempCsv = File.createTempFile("batch_factors", ".csv");
            try (FileWriter writer = new FileWriter(tempCsv)) {
                writer.write("category,activity,factor,unit,source\n");
                writer.write("Transport,Petrol Car,0.170,kg CO2e/km,DEFRA 2023\n");
                writer.write("Electricity,Grid Electricity,0.820,kg CO2e/kWh,CEA 2023\n");
            }

            calc.loadEmissionFactors(tempCsv.getAbsolutePath());

            List<Activity> list = new ArrayList<>();
            list.add(new Activity(1, 1, ActivityCategory.TRANSPORT, "Petrol Car", 50.0, "km", LocalDate.now()));
            list.add(new Activity(2, 1, ActivityCategory.ELECTRICITY, "Grid Electricity", 200.0, "kWh", LocalDate.now()));

            List<ImpactRecord> records = calc.processBatch(list);

            boolean countMatch = records.size() == 2;
            boolean firstRecordValid = false;
            boolean secondRecordValid = false;

            for (ImpactRecord ir : records) {
                if (ir.getActivityId() == 1 && Math.abs(ir.getCo2eKg() - 8.5) < 0.0001) {
                    firstRecordValid = true;
                }
                if (ir.getActivityId() == 2 && Math.abs(ir.getCo2eKg() - 164.0) < 0.0001) {
                    secondRecordValid = true;
                }
            }

            assertTrue(countMatch && firstRecordValid && secondRecordValid, 
                       "Multithreaded Concurrent Batch Processing Validation");

        } catch (IOException e) {
            assertTrue(false, "Batch Processing Test setup failed: " + e.getMessage());
        } finally {
            if (tempCsv != null && tempCsv.exists()) {
                tempCsv.delete();
            }
        }
    }
}