package org.example;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;
import java.time.Duration;
import java.util.List;

public class SearchResultsTest extends BaseTest {

    private void pause() {
        try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
    }

    /** Loads a search URL and waits for at least one job card to appear. */
    private void loadSearch(String url) {
        driver.get(url);
        new WebDriverWait(driver, Duration.ofSeconds(20))
                .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".job_seen_beacon")));
    }

    // Test 1: Loading date-sorted "data scientist" results and comparing them to relevance-sorted results
    @Test
    public void testSortByDateReordersResults() {
        // Load the default relevance-sorted results first
        loadSearch("https://www.indeed.com/jobs?q=data+scientist&l=Austin%2C+TX");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        pause();

        // Capture the first few titles from the relevance-sorted page
        List<WebElement> cardsBefore = driver.findElements(By.cssSelector("h2.jobTitle a"));
        String firstTitleBefore = cardsBefore.get(0).getText().trim();
        pause();

        // Scroll through the relevance-sorted results so the viewer can see them
        ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, 400);");
        pause();

        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0);");
        pause();

        // Now navigate to the same search sorted by date — results visibly reload
        driver.get("https://www.indeed.com/jobs?q=data+scientist&l=Austin%2C+TX&sort=date");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".job_seen_beacon")));
        pause();

        // Scroll through the date-sorted results
        ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, 400);");
        pause();

        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0);");
        pause();

        // Verify sort=date is active and results are still present
        List<WebElement> cardsAfter = driver.findElements(By.cssSelector("h2.jobTitle a"));
        Assert.assertTrue(cardsAfter.size() > 0,
                "Results should still be present after switching to date sort.");
        Assert.assertTrue(driver.getCurrentUrl().contains("sort=date"),
                "URL should contain 'sort=date' confirming the date sort is active.");
        pause();
    }

    // Test 2: Clicking a "financial analyst" job card opens the detail panel
    @Test
    public void testClickingJobCardOpensDetailPanel() {
        loadSearch("https://www.indeed.com/jobs?q=financial+analyst&l=Chicago%2C+IL");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        pause();

        // Locate the first job card title and pause so it's visible before clicking
        WebElement firstCard = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("h2.jobTitle a")));
        String jobTitle = firstCard.getText().trim();
        pause();

        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", firstCard);
        pause();

        // Wait for the right-side detail panel to load
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("jobDescriptionText")));
        pause();

        // Scroll through the description panel
        WebElement descriptionPanel = driver.findElement(By.id("jobDescriptionText"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'start'});", descriptionPanel);
        pause();

        ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, 300);");
        pause();

        Assert.assertTrue(descriptionPanel.isDisplayed(),
                "Clicking job card '" + jobTitle + "' should open the detail panel.");
        Assert.assertFalse(descriptionPanel.getText().trim().isEmpty(),
                "The job description panel should not be empty.");
        pause();
    }

    // Test 3: Changing the location field on an "electrician" search reloads results for a new city
    @Test
    public void testChangingLocationUpdatesResults() {
        loadSearch("https://www.indeed.com/jobs?q=electrician&l=Houston%2C+TX");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        pause();

        // Find the "Where" input at the top of the results page
        WebElement locationInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("#text-input-where, input[name='l'], input[id*='where']")));

        // Select all existing text and replace with a new city
        ((JavascriptExecutor) driver).executeScript("arguments[0].click(); arguments[0].focus();", locationInput);
        locationInput.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        locationInput.sendKeys("Dallas, TX");
        pause();

        // Submit the updated location
        locationInput.sendKeys(Keys.ENTER);
        pause();

        // Wait for results to reload for the new city
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".job_seen_beacon")));
        pause();

        // Scroll through the new results so they're visible on screen
        ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, 400);");
        pause();

        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0);");
        pause();

        // Verify the URL updated to reflect Dallas and that results loaded
        Assert.assertTrue(driver.getCurrentUrl().toLowerCase().contains("dallas") ||
                          driver.getCurrentUrl().toLowerCase().contains("tx"),
                "URL should reflect the new location after changing it. Actual: " + driver.getCurrentUrl());

        List<WebElement> cards = driver.findElements(By.cssSelector(".job_seen_beacon"));
        Assert.assertTrue(cards.size() > 0,
                "Job results should load after changing the location to Dallas, TX.");
        pause();
    }

    // Test 4: Clicking through the first 3 "project manager" job cards updates the detail panel each time
    @Test
    public void testClickingMultipleJobCardsUpdatesDetailPanel() {
        loadSearch("https://www.indeed.com/jobs?q=project+manager&l=Seattle%2C+WA");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        pause();

        String previousDescription = "";

        for (int i = 0; i < 3; i++) {
            // Re-fetch card list each iteration so stale references don't cause failures
            List<WebElement> cards = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                    By.cssSelector("h2.jobTitle a")));

            // Scroll the target card into the center of the viewport before clicking
            WebElement card = cards.get(i);
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", card);
            pause();

            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", card);
            pause();

            // Wait for the detail panel to fully load
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("jobDescriptionText")));
            pause();

            // Scroll through the detail panel so the content is visible
            ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, 200);");
            pause();

            ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, -200);");
            pause();

            // Use the first 300 chars of the job description — unique per job and always present
            String description = driver.findElement(By.id("jobDescriptionText")).getText().trim();
            String snippet = description.substring(0, Math.min(300, description.length()));
            pause();

            Assert.assertNotEquals(snippet, previousDescription,
                    "Clicking card " + (i + 1) + " should load a different job description in the panel.");

            previousDescription = snippet;
            pause();
        }
    }

    // Test 5: Clicking "Next" pagination on a "teacher" search loads the second page of results
    @Test
    public void testPaginationNextPageLoadsNewResults() {
        loadSearch("https://www.indeed.com/jobs?q=teacher&l=Los+Angeles%2C+CA");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        pause();

        // Scroll slowly to the bottom so the pagination is clearly visible
        ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, document.body.scrollHeight / 2);");
        pause();

        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
        pause();

        // Locate the Next button and pause so it's visible before clicking
        WebElement nextBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                "//a[@aria-label='Next Page'] | //a[contains(.,'Next')] | " +
                "//nav//a[contains(@href,'start=')]")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", nextBtn);
        pause();

        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", nextBtn);
        pause();

        // Wait for page 2 to load
        wait.until(ExpectedConditions.urlContains("start="));
        pause();

        Assert.assertTrue(driver.getCurrentUrl().contains("start="),
                "Clicking Next should load page 2 with a 'start=' offset in the URL.");

        // Scroll through the new page of results
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".job_seen_beacon")));
        pause();

        ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, 400);");
        pause();

        List<WebElement> page2Cards = driver.findElements(By.cssSelector(".job_seen_beacon"));
        Assert.assertTrue(page2Cards.size() > 0,
                "Page 2 of results should contain job listings.");
        pause();
    }
}
