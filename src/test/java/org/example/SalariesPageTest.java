package org.example;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.List;

/**
 * SalariesPageTest
 * * Inherits from BaseTest to utilize the persistent Chrome profile.
 * Focuses on basic validation of the Indeed Salaries landing page.
 */
public class SalariesPageTest extends BaseTest {

    private final String SALARIES_URL = "https://www.indeed.com/salaries";

    /**
     * Test 1: Confirm the page title contains "Salary".
     * Validates that the user is on the correct page.
     */
    @Test
    public void testSalariesPageTitle() {
        driver.get(SALARIES_URL);
        String title = driver.getTitle();
        Assert.assertTrue(title.contains("Salaries"),
                "The page title should contain the word 'Salary'. Actual: " + title);
    }

    /**
     * Test 2: Confirm the URL persists as /salaries.
     * Checks for unexpected redirects to the homepage or login wall.
     */
    @Test
    public void testSalariesPageURL() {
        driver.get(SALARIES_URL);
        String currentUrl = driver.getCurrentUrl();
        Assert.assertTrue(currentUrl.contains("salaries"),
                "The URL should contain 'salaries'. Actual: " + currentUrl);
    }

    /**
     * Test 3: Verify the 'Job Title' search input is present and visible.
     * Introduces WebDriverWait to handle dynamic element rendering.
     */
    @Test
    public void testJobTitleInputIsPresent() {
        driver.get(SALARIES_URL);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        // 1. First, wait for the main Salaries content area to exist
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("main")));

        // 2. Target the 'What' input box. Indeed 2026 uses 'input-title-autocomplete'
        // or simply 'text-input-what' for these types of salary searches.
        WebElement jobInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[id*='what'], input[id*='title'], #salary-search-title, .y-typeahead-input")
        ));

        Assert.assertTrue(jobInput.isDisplayed(), "The Job Title search box failed to load.");
    }

    /**
     * Test 4: Sanity check for links.
     * Verifies the page isn't blank or an error state by counting <a> tags.
     */
    @Test
    public void testSalariesPageHasLinks() {
        driver.get(SALARIES_URL);
        List<WebElement> links = driver.findElements(By.tagName("a"));

        Assert.assertTrue(links.size() > 0,
                "The page should contain at least one link (<a> tag).");
    }

    /**
     * Test 5: Confirm the page title is not empty.
     * A basic existence check for page metadata.
     */
    @Test
    public void testSalariesPageHasNonEmptyTitle() {
        driver.get(SALARIES_URL);
        String title = driver.getTitle();

        Assert.assertNotNull(title, "Title should not be null.");
        Assert.assertFalse(title.trim().isEmpty(), "Page title should not be empty.");
    }
}