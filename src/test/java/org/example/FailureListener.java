package org.example;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * FailureListener
 *
 * A TestNG listener that fires on every test failure and prints:
 *   - The test name
 *   - The current page URL at the moment of failure
 *   - The current page title at the moment of failure
 *   - The path to a saved screenshot
 *
 * Attach to every test class via @Listeners on BaseTest.
 */
public class FailureListener implements ITestListener {

    private static final String SCREENSHOT_DIR = "target/failure-screenshots/";

    @Override
    public void onTestFailure(ITestResult result) {
        // Retrieve the driver from the test instance
        Object testInstance = result.getInstance();
        if (!(testInstance instanceof BaseTest)) return;

        BaseTest base = (BaseTest) testInstance;
        if (base.driver == null) return;

        String testName = result.getName();
        String url      = base.driver.getCurrentUrl();
        String title    = base.driver.getTitle();

        System.out.println("\n========================================================");
        System.out.println("  FAILED: " + testName);
        System.out.println("  URL   : " + url);
        System.out.println("  TITLE : " + title);

        // Take and save a screenshot
        try {
            Files.createDirectories(Paths.get(SCREENSHOT_DIR));
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename  = SCREENSHOT_DIR + testName + "_" + timestamp + ".png";

            byte[] screenshot = ((TakesScreenshot) base.driver).getScreenshotAs(OutputType.BYTES);
            Files.write(Paths.get(filename), screenshot);

            System.out.println("  SCREENSHOT: " + new File(filename).getAbsolutePath());
        } catch (IOException e) {
            System.out.println("  SCREENSHOT: failed to save — " + e.getMessage());
        }

        System.out.println("========================================================\n");
    }
}
