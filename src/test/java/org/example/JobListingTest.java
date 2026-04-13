package org.example;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;
import org.testng.annotations.Test;
import java.time.Duration;

public class JobListingTest extends BaseTest {

    private static final String SEARCH_URL =
            "https://www.indeed.com/jobs?q=software+engineer&l=Miami%2C+FL";

    /**
     * Helper to navigate and open the first job listing.
     * Uses JS Click and Scroll to ensure stability.
     */
    private void openFirstJobListing() {
        driver.get(SEARCH_URL);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        // 1. Wait for job titles to appear
        WebElement firstJobLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("h2.jobTitle a, a[data-jk]")
        ));

        // 2. Scroll to the card and click via JavaScript
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", firstJobLink);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", firstJobLink);

        // 3. Wait for the detail pane to load
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("jobDescriptionText")));
    }

    @Test
    public void testClickingJobCardLoadsDescriptionPanel() {
        openFirstJobListing();
        WebElement descriptionPanel = driver.findElement(By.id("jobDescriptionText"));
        Assert.assertTrue(descriptionPanel.isDisplayed(),
                "The job description panel should be visible after clicking a job card");
    }

    @Test
    public void testJobDescriptionTextIsNotEmpty() {
        openFirstJobListing();
        WebElement descriptionPanel = driver.findElement(By.id("jobDescriptionText"));
        String text = descriptionPanel.getText();
        Assert.assertNotNull(text);
        Assert.assertTrue(text.length() > 0, "The job description text should not be empty");
    }

    @Test
    public void testJobDetailShowsCompanyName() {
        openFirstJobListing();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        //Locator for company name in the right-side detail pane
        WebElement companyName = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-testid='inlineHeader-companyName'], .jobsearch-InlineCompanyRating div, [class*='companyName']")));

        Assert.assertFalse(companyName.getText().isEmpty(), "Company name text should be present in detail view");
    }

    @Test
    public void testURLUpdatesWithJobKeyAfterClick() {

        driver.get(SEARCH_URL);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        WebElement firstJobLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("h2.jobTitle a")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", firstJobLink);


        boolean urlChanged = wait.until(ExpectedConditions.urlContains("vjk="));

        Assert.assertTrue(urlChanged, "The URL should contain the 'vjk=' job key parameter. Current URL: " + driver.getCurrentUrl());
    }
}