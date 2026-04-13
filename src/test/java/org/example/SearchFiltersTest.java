package org.example;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;
import java.time.Duration;

public class SearchFiltersTest extends BaseTest {

    private static final String SEARCH_URL =
            "https://www.indeed.com/jobs?q=software+engineer&l=Miami%2C+FL";

    private void loadSearchPage() {
        driver.get(SEARCH_URL);
        // Ensure the window is maximized so filters aren't hidden in a 'Filters' button
        driver.manage().window().maximize();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        // Wait for any element that indicates the search results have actually loaded
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("mosaic-jobResults")));
    }

    @Test
    public void testDatePostedFilterButtonIsPresent() {
        loadSearchPage();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        // Target the button that contains 'Date Posted' text - more resilient than specific IDs
        WebElement dateFilter = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//button[contains(translate(text(), 'DATEPOSTED', 'dateposted'), 'date posted')] | //button[@id='filter-dateposted']")
        ));
        Assert.assertTrue(dateFilter.isDisplayed(), "The 'Date posted' filter button should be visible");
    }

    @Test
    public void testJobTypeFilterButtonIsPresent() {
        loadSearchPage();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        WebElement jobTypeFilter = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//button[contains(translate(text(), 'JOBTYPE', 'jobtype'), 'job type')] | //button[@id='filter-jobtype']")
        ));
        Assert.assertTrue(jobTypeFilter.isDisplayed(), "The 'Job type' filter button should be visible");
    }

    @Test
    public void testLast24HoursFilterIsReflectedInURL() {
        // Direct navigation is the most reliable way to test filter logic
        String filteredUrl = SEARCH_URL + "&fromage=1";
        driver.get(filteredUrl);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        wait.until(ExpectedConditions.urlContains("fromage=1"));

        Assert.assertTrue(driver.getCurrentUrl().contains("fromage=1"), "URL should retain the 'fromage=1' parameter.");
    }

    @Test
    public void testLast24HoursFilterPageLoads() {
        driver.get(SEARCH_URL + "&fromage=1");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        // Wait for results container or the 'no results' message
        WebElement content = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("#mosaic-jobResults, .error_page_content, .jobsearch-NoResultsHeader")));

        Assert.assertTrue(content.isDisplayed(), "Filter page failed to load content.");
    }

    @Test
    public void testFulltimeJobTypeFilterIsReflectedInURL() {
        String filteredUrl = SEARCH_URL + "&jt=fulltime";
        driver.get(filteredUrl);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        wait.until(ExpectedConditions.urlContains("jt=fulltime"));

        Assert.assertTrue(driver.getCurrentUrl().contains("jt=fulltime"), "URL should retain 'jt=fulltime'.");
    }
}