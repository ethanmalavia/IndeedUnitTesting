package org.example;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.List;

/**
 * CompanyReviewsTest
 *
 * Tests the Indeed Companies & Reviews section:
 *   1 — Landing page loads with company search input
 *   2 — Typing a company name and submitting navigates to results
 *   3 — Company profile cards are visible and clickable
 *   4 — Clicking a company card navigates to their profile page
 *   5 — Company profile page displays review-related content
 */
public class CompanyReviewsTest extends BaseTest {

    private static final String REVIEWS_URL   = "https://www.indeed.com/companies";
    private static final String COMPANY_NAME  = "Google";

    private void pause() {
        try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
    }

    // Test 1: Companies landing page loads with a visible search input
    @Test(priority = 1)
    public void testCompaniesPageLoads() {
        driver.get(REVIEWS_URL);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("main")));
        pause();

        WebElement searchInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[name='q'], input[id*='search'], input[id*='what'], " +
                               "input[placeholder*='ompan'], input[aria-label*='ompan']")
        ));
        pause();

        Assert.assertTrue(searchInput.isDisplayed(),
                "Company search input should be visible on the landing page.");
        pause();
    }

    // Test 2: Searching for a company navigates to search results
    @Test(priority = 2)
    public void testCompanySearchNavigatesToResults() {
        driver.get(REVIEWS_URL);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("main")));
        pause();

        WebElement searchInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[name='q'], input[id*='search'], input[id*='what'], " +
                               "input[placeholder*='ompan'], input[aria-label*='ompan']")
        ));

        ((JavascriptExecutor) driver).executeScript("arguments[0].click(); arguments[0].focus();", searchInput);
        searchInput.sendKeys(COMPANY_NAME);
        pause();

        searchInput.sendKeys(Keys.ENTER);
        pause();

        wait.until(d -> !d.getCurrentUrl().equals(REVIEWS_URL));
        pause();

        String url = driver.getCurrentUrl();
        Assert.assertTrue(url.contains("google") || url.contains("companies") || url.contains("q="),
                "Searching for a company should navigate away from the landing page. URL: " + url);
        pause();
    }

    // Test 3: Company profile cards (links to /cmp/) are present on the landing page
    @Test(priority = 3)
    public void testCompanyProfileCardsArePresent() {
        driver.get(REVIEWS_URL);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("main")));
        pause();

        // Scroll down to trigger any lazy-loaded cards
        ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, 400);");
        pause();

        List<WebElement> companyCards = driver.findElements(
                By.cssSelector("a[href*='/cmp/']")
        );
        pause();

        Assert.assertTrue(companyCards.size() > 0,
                "At least one company profile link should appear on the landing page.");
        pause();
    }

    // Test 4: Clicking a company card navigates to that company's profile page
    @Test(priority = 4)
    public void testClickingCompanyCardNavigatesToProfile() {
        driver.get(REVIEWS_URL);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("main")));
        pause();

        ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, 400);");
        pause();

        WebElement firstCompanyLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("a[href*='/cmp/']")
        ));

        String companyHref = firstCompanyLink.getAttribute("href");
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", firstCompanyLink);
        pause();

        wait.until(ExpectedConditions.urlContains("/cmp/"));
        pause();

        Assert.assertTrue(driver.getCurrentUrl().contains("/cmp/"),
                "Clicking a company card should navigate to the company profile. URL: " + driver.getCurrentUrl());
        pause();
    }

    // Test 5: Company profile page displays reviews-related content (ratings or review count)
    @Test(priority = 5)
    public void testCompanyProfileShowsReviewContent() {
        // Navigate directly to a known stable company profile
        driver.get("https://www.indeed.com/cmp/Google");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("main")));
        pause();

        // Scroll down to let review section render
        ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, 500);");
        pause();

        // Look for star ratings, review counts, or the reviews heading
        List<WebElement> reviewContent = driver.findElements(By.xpath(
                "//*[contains(text(),'review') or contains(text(),'Review') or contains(text(),'rating') " +
                "or contains(text(),'Rating') or contains(@aria-label,'star') or contains(@class,'rating') " +
                "or contains(@class,'star')]"
        ));
        pause();

        Assert.assertTrue(reviewContent.size() > 0,
                "Google's company profile should display review or rating content.");
        pause();
    }
}
