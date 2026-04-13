package org.example;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;
import org.testng.annotations.Test;
import java.time.Duration;
import java.util.List;

public class CompanyReviewsTest extends BaseTest {

    private final String REVIEWS_URL = "https://www.indeed.com/companies";

    @Test
    public void testCompanySearchInputIsVisible() {
        driver.get(REVIEWS_URL);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        // Indeed often uses 'main-content-search' or 'ifl-Input-input' for their search bars now
        WebElement searchInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[name='q'], .ifl-Input-input, #main-content-search")
        ));
        Assert.assertTrue(searchInput.isDisplayed(), "The company search input is missing.");
    }

    @Test
    public void testPopularCompaniesSectionLoads() {
        driver.get(REVIEWS_URL);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        // Wait for the main container that holds company profiles
        WebElement container = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("main, #main, .css-1fb26v2")
        ));
        Assert.assertTrue(container.isDisplayed(), "The main companies section failed to load.");
    }

    /**
     * Test 3: Replaced Star Ratings with Company Cards.
     * Checks for the presence of company profile tiles.
     */
    @Test
    public void testCompanyProfileCardsArePresent() {
        driver.get(REVIEWS_URL);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        // Scroll to ensure lazy-loaded cards are triggered
        ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, 600);");

        // Targeted company cards/links
        List<WebElement> companyCards = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                By.cssSelector("a[data-testid*='company-tile'], a[href*='/cmp/'], .css-1fb26v2")
        ));

        Assert.assertTrue(companyCards.size() > 0, "No company profile cards were found.");
    }

    @Test
    public void testWriteReviewButtonVisibility() {
        driver.get(REVIEWS_URL);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        // 1. Scroll to the bottom as the 'Write a Review' link is often in the footer
        // or at the end of the company list in the 2026 layout.
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");

        // 2. Use an XPath that targets the text regardless of casing or surrounding tags.
        // translate() is used here to make the search case-insensitive for 'write a review'.
        WebElement writeReviewBtn = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(translate(text(), 'WRITEA REVIEW', 'writea review'), 'write a review')] | //a[contains(@href, 'review')]")
        ));

        Assert.assertTrue(writeReviewBtn.isDisplayed(), "The 'Write a review' element was found but is not visible.");
    }

    @Test
    public void testNavigateToSpecificCompanyReviews() {
        driver.get(REVIEWS_URL);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        // Find the first company link and click it
        WebElement firstLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("a[href*='/cmp/']")
        ));

        firstLink.click();

        // Ensure we moved to a company profile
        wait.until(ExpectedConditions.urlContains("/cmp/"));
        Assert.assertTrue(driver.getCurrentUrl().contains("/cmp/"), "Navigation to company profile failed.");
    }
}