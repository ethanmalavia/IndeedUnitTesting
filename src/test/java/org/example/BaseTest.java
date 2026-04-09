package org.example;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BaseTest
 *
 * Shared setup, teardown, and navigation helper for every test class.
 *
 * -----------------------------------------------------------------------
 * AUTOMATION CONSTRAINT: Cloudflare Bot Protection
 * -----------------------------------------------------------------------
 * Indeed.com is protected by Cloudflare, which detects and challenges
 * automated browsers.
 */
public class BaseTest {

    protected ChromeDriver driver;

    @BeforeMethod
    public void setUp() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();

        // Layer 1: remove visible automation markers
        options.setExperimentalOption("excludeSwitches", List.of("enable-automation"));
        options.setExperimentalOption("useAutomationExtension", false);
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--start-maximized");

        // Mimic a real Windows/Chrome user agent
        options.addArguments(
            "user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/134.0.0.0 Safari/537.36"
        );

        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

        // Layer 2: CDP stealth script — runs before any page JS on every navigation
        Map<String, Object> stealthScript = new HashMap<>();
        stealthScript.put("source",
            "Object.defineProperty(navigator, 'webdriver', {get: () => undefined});" +
            "window.chrome = { runtime: {} };" +
            "Object.defineProperty(navigator, 'languages', {get: () => ['en-US', 'en']});" +
            "Object.defineProperty(navigator, 'plugins',   {get: () => [1, 2, 3, 4, 5]});"
        );
        driver.executeCdpCommand("Page.addScriptToEvaluateOnNewDocument", stealthScript);
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    /**
     * Navigates to a URL and handles Cloudflare's bot-verification page.
     *
     * Step 1 — Navigate and wait up to 5 seconds for an automatic resolution.
     *           Cloudflare's JavaScript challenge often self-completes for
     *           browsers that pass its fingerprint checks.
     *
     * Step 2 — If the challenge is still present after 5 seconds, print a
     *           console message and wait up to 60 seconds for the tester to
     *           click through the verification in the open browser window.
     *
     * Step 3 — If the challenge is not resolved within 60 seconds, fail the
     *           test with a descriptive message rather than hanging forever.
     *
     * All test classes call this method instead of driver.get() so that
     * Cloudflare handling is consistent across the entire test suite.
     */
    protected void navigateTo(String url) {
        driver.get(url);

        // Step 1: give Cloudflare up to 5 seconds to auto-resolve
        try {
            new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(d -> !d.getTitle().contains("Just a moment")
                          && !d.getTitle().contains("Attention Required")
                          && !d.getTitle().contains("Please Wait"));
            return; // page loaded cleanly — no challenge present
        } catch (TimeoutException e) {
            // Challenge is still showing — fall through to manual step
        }

        // Step 2: prompt for manual resolution
        System.out.println("\n========================================================");
        System.out.println(" CLOUDFLARE VERIFICATION REQUIRED");
        System.out.println(" Please complete the check in the browser window.");
        System.out.println(" The test will resume automatically once it is done.");
        System.out.println(" Waiting up to 60 seconds...");
        System.out.println("========================================================\n");

        // Step 3: wait for manual resolution or fail
        try {
            new WebDriverWait(driver, Duration.ofSeconds(60))
                .until(d -> !d.getTitle().contains("Just a moment")
                          && !d.getTitle().contains("Attention Required")
                          && !d.getTitle().contains("Please Wait"));
            System.out.println("[Cloudflare] Verification passed. Resuming test.\n");
        } catch (TimeoutException e) {
            Assert.fail(
                "Cloudflare verification was not completed within 60 seconds. " +
                "Please solve the challenge manually when it appears in the browser window."
            );
        }
    }
}
