package org.example;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;

/**
 * JobListingTest
 *
 * Verifies the job detail view that appears when a user clicks a job card
 * on the search results page. On desktop, Indeed shows details in a
 * right-side panel without navigating away from the results page.
 *
 * URL under test: https://www.indeed.com/jobs?q=software+engineer&l=Miami%2C+FL
 */
public class JobListingTest extends BaseTest {

    private static final String SEARCH_URL =
            "https://www.indeed.com/jobs?q=software+engineer&l=Miami%2C+FL";

    /**
     * Clicks the first job card title on the results page and waits for
     * the job description panel to become available.
     */
    private void openFirstJobListing() {

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("h2.jobTitle a")));
        driver.findElement(By.cssSelector("h2.jobTitle a")).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("jobDescriptionText")));
    }

    // Test 1: Clicking a job card loads the job description panel
    @Test
    public void testClickingJobCardLoadsDescriptionPanel() {
        openFirstJobListing();
        WebElement descriptionPanel = driver.findElement(By.id("jobDescriptionText"));
        Assert.assertTrue(descriptionPanel.isDisplayed(),
                "The job description panel should be visible after clicking a job card");
    }

    // Test 2: The job description panel contains non-empty text
    @Test
    public void testJobDescriptionTextIsNotEmpty() {
        openFirstJobListing();
        WebElement descriptionPanel = driver.findElement(By.id("jobDescriptionText"));
        Assert.assertFalse(descriptionPanel.getText().isEmpty(),
                "The job description text should not be empty");
    }

    // Test 3: The detail view shows a company name
    @Test
    public void testJobDetailShowsCompanyName() {
        openFirstJobListing();
        WebElement companyName = driver.findElement(
                By.cssSelector("[data-testid='inlineHeader-companyName'], [class*='companyName']"));
        Assert.assertTrue(companyName.isDisplayed(),
                "The company name should be visible in the job detail view");
        Assert.assertFalse(companyName.getText().isEmpty(),
                "The company name should not be empty in the job detail view");
    }

    // Test 4: The detail view shows an "Apply" button
    @Test
    public void testApplyButtonIsPresentInDetailView() {
        openFirstJobListing();
        // Indeed shows either "Apply on company site" or an Indeed Apply button
        WebElement applyButton = driver.findElement(
                By.cssSelector("#indeedApplyButton, [class*='applyButton'], button[aria-label*='pply']"));
        Assert.assertTrue(applyButton.isDisplayed(),
                "An Apply button should be visible in the job detail view");
    }

    // Test 5: The URL is updated with a job key (vjk) after clicking a job card
    @Test
    public void testURLUpdatesWithJobKeyAfterClick() {

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("h2.jobTitle a")));
        driver.findElement(By.cssSelector("h2.jobTitle a")).click();

        // Indeed adds vjk= (view job key) to the URL when a job detail is opened
        wait.until(ExpectedConditions.urlContains("vjk="));
        Assert.assertTrue(driver.getCurrentUrl().contains("vjk="),
                "The URL should contain the 'vjk=' job key parameter after clicking a job. URL: "
                        + driver.getCurrentUrl());
    }
}
