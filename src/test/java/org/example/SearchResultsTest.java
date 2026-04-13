package org.example;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;
import java.time.Duration;
import java.util.List;

public class SearchResultsTest extends BaseTest {


    private static final String SEARCH_URL =
            "https://www.indeed.com/jobs?q=software+engineer&l=Miami%2C+FL";

    @Test
    public void testAtLeastOneJobCardIsDisplayed() {
        // Navigate first since SearchResultsTest doesn't have its own setup
        driver.get(SEARCH_URL);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".job_seen_beacon")));

        List<WebElement> jobCards = driver.findElements(By.cssSelector(".job_seen_beacon, [data-jk], .resultContent"));
        Assert.assertTrue(jobCards.size() > 0, "No job cards found on results page.");
    }

    @Test
    public void testJobCardsDisplayJobTitles() {
        driver.get(SEARCH_URL);
        List<WebElement> jobTitles = driver.findElements(By.cssSelector("h2.jobTitle span[title], h2.jobTitle a"));
        Assert.assertTrue(jobTitles.size() > 0, "Job titles are missing.");
        Assert.assertFalse(jobTitles.get(0).getText().trim().isEmpty(), "First job title is empty.");
    }

    @Test
    public void testJobCardsDisplayCompanyNames() {
        driver.get(SEARCH_URL);
        List<WebElement> companyNames = driver.findElements(By.cssSelector("[data-testid='company-name'], .companyName"));
        Assert.assertTrue(companyNames.size() > 0, "Company names are missing.");
        Assert.assertFalse(companyNames.get(0).getText().trim().isEmpty(), "First company name is empty.");
    }

    @Test
    public void testJobCardsDisplayLocations() {
        driver.get(SEARCH_URL);
        List<WebElement> locations = driver.findElements(By.cssSelector("[data-testid='text-location'], .companyLocation"));
        Assert.assertTrue(locations.size() > 0, "Location information is missing from job cards.");
    }

    @Test
    public void testResultsPageURLContainsSearchParameters() {
        driver.get(SEARCH_URL);
        String currentUrl = driver.getCurrentUrl();
        Assert.assertTrue(currentUrl.toLowerCase().contains("software"), "URL missing search query.");
        Assert.assertTrue(currentUrl.toLowerCase().contains("miami") || currentUrl.contains("l="), "URL missing location.");
    }
}