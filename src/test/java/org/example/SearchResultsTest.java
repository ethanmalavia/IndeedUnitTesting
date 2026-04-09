package org.example;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SearchResultsTest
 *
 * Verifies that the search results page for a known query displays the
 * expected structure: job cards, titles, company names, and locations.
 *
 * Unlike the other test classes that use @BeforeMethod (a fresh browser
 * per test), this class uses @BeforeClass so the browser opens once,
 * navigates to the results page once, and all five tests run against
 * that same loaded page before the browser closes. This is appropriate
 * here because every test examines the same page — there is no reason
 * to reload it five separate times.
 *
 * URL under test: https://www.indeed.com/jobs?q=software+engineer&l=Miami%2C+FL
 */
public class SearchResultsTest {

    // Static so the single driver instance is shared across all @Test methods
    private static ChromeDriver driver;

    private static final String SEARCH_URL =
            "https://www.indeed.com/jobs?q=software+engineer&l=Miami%2C+FL";

    @BeforeClass
    public static void setUpOnce() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("excludeSwitches", List.of("enable-automation"));
        options.setExperimentalOption("useAutomationExtension", false);
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--start-maximized");
        options.addArguments(
            "user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36"
        );

        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

        // CDP stealth script — same as BaseTest
        Map<String, Object> stealthScript = new HashMap<>();
        stealthScript.put("source",
            "Object.defineProperty(navigator, 'webdriver', {get: () => undefined});" +
            "window.chrome = { runtime: {} };" +
            "Object.defineProperty(navigator, 'languages', {get: () => ['en-US', 'en']});" +
            "Object.defineProperty(navigator, 'plugins',   {get: () => [1, 2, 3, 4, 5]});"
        );
        driver.executeCdpCommand("Page.addScriptToEvaluateOnNewDocument", stealthScript);

        // Navigate once for all five tests
        driver.get(SEARCH_URL);
        handleCloudflare();

        // Wait until at least one job card is present before any test runs
        new WebDriverWait(driver, Duration.ofSeconds(20))
            .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".job_seen_beacon")));
    }

    @AfterClass
    public static void tearDownOnce() {
        if (driver != null) {
            driver.quit();
        }
    }

    /**
     * Cloudflare handler — mirrors BaseTest.navigateTo() but written as a
     * static method so it can be called from the static @BeforeClass.
     */
    private static void handleCloudflare() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(d -> !d.getTitle().contains("Just a moment")
                          && !d.getTitle().contains("Attention Required")
                          && !d.getTitle().contains("Please Wait"));
            return;
        } catch (TimeoutException e) {
            // Challenge still showing — prompt for manual resolution
        }

        System.out.println("\n========================================================");
        System.out.println(" CLOUDFLARE VERIFICATION REQUIRED");
        System.out.println(" Please complete the check in the browser window.");
        System.out.println(" Waiting up to 60 seconds...");
        System.out.println("========================================================\n");

        try {
            new WebDriverWait(driver, Duration.ofSeconds(60))
                .until(d -> !d.getTitle().contains("Just a moment")
                          && !d.getTitle().contains("Attention Required")
                          && !d.getTitle().contains("Please Wait"));
        } catch (TimeoutException e) {
            Assert.fail("Cloudflare verification was not completed within 60 seconds.");
        }
    }

    // Test 1: At least one job card should appear on the results page
    @Test
    public void testAtLeastOneJobCardIsDisplayed() {
        List<WebElement> jobCards = driver.findElements(By.cssSelector(".job_seen_beacon"));
        Assert.assertTrue(jobCards.size() > 0,
                "At least one job card should be present on the search results page");
    }

    // Test 2: Job cards should display a non-empty job title
    @Test
    public void testJobCardsDisplayJobTitles() {
        List<WebElement> jobTitles = driver.findElements(By.cssSelector("h2.jobTitle"));
        Assert.assertTrue(jobTitles.size() > 0,
                "Job title elements should be present in the search results");
        Assert.assertFalse(jobTitles.get(0).getText().isEmpty(),
                "The first job title should not be empty text");
    }

    // Test 3: Job cards should display a non-empty company name
    @Test
    public void testJobCardsDisplayCompanyNames() {
        List<WebElement> companyNames = driver.findElements(
                By.cssSelector("[data-testid='company-name']"));
        Assert.assertTrue(companyNames.size() > 0,
                "Company name elements should be present in the search results");
        Assert.assertFalse(companyNames.get(0).getText().isEmpty(),
                "The first company name should not be empty text");
    }

    // Test 4: Job cards should display a location
    @Test
    public void testJobCardsDisplayLocations() {
        List<WebElement> locations = driver.findElements(
                By.cssSelector("[data-testid='text-location']"));
        Assert.assertTrue(locations.size() > 0,
                "Location elements should be present in the search results");
    }

    // Test 5: The results page URL should contain the original search parameters
    @Test
    public void testResultsPageURLContainsSearchParameters() {
        String currentUrl = driver.getCurrentUrl();
        Assert.assertTrue(currentUrl.contains("q=software"),
                "URL should contain the job title query parameter. URL: " + currentUrl);
        Assert.assertTrue(currentUrl.contains("l="),
                "URL should contain the location query parameter. URL: " + currentUrl);
    }
}
