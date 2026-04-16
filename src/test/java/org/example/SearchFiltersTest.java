package org.example;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;
import java.time.Duration;
import java.util.List;

public class SearchFiltersTest extends BaseTest {

    private static final String SEARCH_URL =
            "https://www.indeed.com/jobs?q=software+engineer&l=Miami%2C+FL";

    private void pause() {
        try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
    }

    private void loadSearchPage() {
        driver.get(SEARCH_URL);
        driver.manage().window().maximize();
        new WebDriverWait(driver, Duration.ofSeconds(20))
                .until(ExpectedConditions.presenceOfElementLocated(By.id("mosaic-jobResults")));
    }

    /**
     * Finds the first visible filter-popup option whose text includes {@code optionText}.
     *
     * Strategy:
     *  Pass 1 — target label / ARIA-role elements (the typical filter option structure):
     *           uses .includes() so checkmark characters (e.g. "✓Full-time") still match.
     *  Pass 2 — fallback to any element whose trimmed .textContent === optionText exactly,
     *           skipping non-interactive tags (HTML, BODY, SCRIPT, STYLE).
     *
     * Both passes exclude job-card ancestors ([data-jk] and #mosaic-jobResults) so
     * job-type badges like "Full-time" on listing cards are never returned.
     */
    private WebElement findPopupOption(String optionText) {
        return (WebElement) ((JavascriptExecutor) driver).executeScript(
            // --- Pass 1: label / ARIA role elements ---------------------------------
            "var pass1 = Array.from(document.querySelectorAll(" +
            "  'label, [role=\"option\"], [role=\"radio\"], [role=\"menuitem\"], [role=\"checkbox\"]'" +
            "));" +
            "for (var el of pass1) {" +
            "  if (el.textContent.includes(arguments[0]) &&" +
            "      !el.closest('[data-jk]') &&" +
            "      !el.closest('#mosaic-jobResults') &&" +
            "      window.getComputedStyle(el).display !== 'none' &&" +
            "      window.getComputedStyle(el).visibility !== 'hidden') {" +
            "    return el;" +
            "  }" +
            "}" +
            // --- Pass 2: exact trimmed textContent on any visible element -----------
            "var skip = {HTML:1,BODY:1,SCRIPT:1,STYLE:1,HEAD:1,META:1,LINK:1};" +
            "var pass2 = Array.from(document.querySelectorAll('*'));" +
            "for (var el of pass2) {" +
            "  if (skip[el.tagName]) continue;" +
            "  if (el.textContent.trim() === arguments[0] &&" +
            "      !el.closest('[data-jk]') &&" +
            "      !el.closest('#mosaic-jobResults') &&" +
            "      window.getComputedStyle(el).display !== 'none' &&" +
            "      window.getComputedStyle(el).visibility !== 'hidden') {" +
            "    return el;" +
            "  }" +
            "}" +
            "return null;",
            optionText);
    }

    /**
     * Clicks a filter button to open its popup, finds the option by text using
     * JavaScript (Pass 1 = label/ARIA, Pass 2 = exact text), clicks it, then
     * clicks the "Update" / "See results" button if the URL has not yet changed.
     */
    private void openFilterAndSelect(WebDriverWait wait,
                                     String filterButtonXpath,
                                     String optionText) {
        WebElement filterBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath(filterButtonXpath)));
        pause();
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", filterBtn);
        pause();

        String urlBefore = driver.getCurrentUrl();

        // Poll until JS finder locates the popup option
        WebElement option = new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(d -> findPopupOption(optionText));

        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", option);
        pause();
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", option);
        pause();

        // If URL has not yet changed, click Update to commit the selection
        if (driver.getCurrentUrl().equals(urlBefore)) {
            try {
                WebElement updateBtn = new WebDriverWait(driver, Duration.ofSeconds(5))
                        .until(d -> (WebElement) ((JavascriptExecutor) d).executeScript(
                            "var btns = Array.from(document.querySelectorAll('button'));" +
                            "for (var btn of btns) {" +
                            "  var t = btn.textContent.trim();" +
                            "  if ((t === 'Update' || t === 'See results' || t === 'Apply') &&" +
                            "      !btn.closest('#mosaic-jobResults') &&" +
                            "      window.getComputedStyle(btn).display !== 'none') {" +
                            "    return btn;" +
                            "  }" +
                            "}" +
                            "return null;"));
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", updateBtn);
                pause();
            } catch (TimeoutException ignored) {
                // No confirmation button visible — filter navigated via another mechanism
            }
        }
    }

    // Test 1: Date Posted filter — open popup, select "Last 24 hours"
    @Test
    public void testDatePostedLast24HoursFilter() {
        loadSearchPage();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        pause();

        openFilterAndSelect(wait,
                "//button[contains(translate(.,'DATEPOSTED','dateposted'),'date posted')] | " +
                "//button[@id='filter-dateposted']",
                "Last 24 hours"
        );

        wait.until(ExpectedConditions.urlContains("fromage=1"));
        pause();

        ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, 400);");
        pause();
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0);");
        pause();

        Assert.assertTrue(driver.getCurrentUrl().contains("fromage=1"),
                "URL should contain 'fromage=1' after selecting Last 24 hours.");
        Assert.assertTrue(driver.findElements(By.cssSelector(".job_seen_beacon")).size() > 0,
                "Results should still be present after applying the date filter.");
        pause();
    }

    // Test 2: Job Type filter — open popup, select "Full-time"
    @Test
    public void testJobTypeFullTimeFilter() {
        loadSearchPage();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        pause();

        openFilterAndSelect(wait,
                "//button[contains(translate(.,'JOBTYPE','jobtype'),'job type')] | " +
                "//button[@id='filter-jobtype']",
                "Full-time"
        );

        // Indeed now encodes job type filters as sc=0kf:attr(CF3CP); (Full-time attribute code)
        wait.until(ExpectedConditions.urlContains("CF3CP"));
        pause();

        ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, 400);");
        pause();
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0);");
        pause();

        Assert.assertTrue(driver.getCurrentUrl().contains("CF3CP"),
                "URL should contain Indeed's Full-time filter attribute code (CF3CP).");
        Assert.assertTrue(driver.findElements(By.cssSelector(".job_seen_beacon")).size() > 0,
                "Full-time results should be present after applying the filter.");
        pause();
    }

    // Test 3: Job Type filter — open popup, select "Contract"
    @Test
    public void testJobTypeContractFilter() {
        loadSearchPage();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        pause();

        openFilterAndSelect(wait,
                "//button[contains(translate(.,'JOBTYPE','jobtype'),'job type')] | " +
                "//button[@id='filter-jobtype']",
                "Contract"
        );

        // Indeed now encodes job type filters as sc=0kf:attr(NJXCK); (Contract attribute code)
        wait.until(ExpectedConditions.urlContains("NJXCK"));
        pause();

        ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, 400);");
        pause();
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0);");
        pause();

        Assert.assertTrue(driver.getCurrentUrl().contains("NJXCK"),
                "URL should contain Indeed's Contract filter attribute code (NJXCK).");
        Assert.assertTrue(driver.findElements(By.cssSelector(".job_seen_beacon")).size() > 0,
                "Contract results should be present after applying the filter.");
        pause();
    }

    // Test 4: Date Posted filter — open popup, select "Last 7 days"
    @Test
    public void testDatePostedLast7DaysFilter() {
        loadSearchPage();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        pause();

        openFilterAndSelect(wait,
                "//button[contains(translate(.,'DATEPOSTED','dateposted'),'date posted')] | " +
                "//button[@id='filter-dateposted']",
                "Last 7 days"
        );

        wait.until(ExpectedConditions.urlContains("fromage=7"));
        pause();

        ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, 400);");
        pause();
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0);");
        pause();

        Assert.assertTrue(driver.getCurrentUrl().contains("fromage=7"),
                "URL should contain 'fromage=7' after selecting Last 7 days.");
        Assert.assertTrue(driver.findElements(By.cssSelector(".job_seen_beacon")).size() > 0,
                "Results should still be present after applying the 7-day date filter.");
        pause();
    }

    // Test 5: Stack filters — Full-time + Last 7 days applied together
    @Test
    public void testStackedJobTypeAndDateFilter() {
        loadSearchPage();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        pause();

        // Step 1 — Job Type → Full-time
        openFilterAndSelect(wait,
                "//button[contains(translate(.,'JOBTYPE','jobtype'),'job type')] | " +
                "//button[@id='filter-jobtype']",
                "Full-time"
        );
        // Indeed now encodes job type filters as sc=0kf:attr(CF3CP); (Full-time attribute code)
        wait.until(ExpectedConditions.urlContains("CF3CP"));
        pause();

        // Step 2 — Date Posted → Last 7 days (stacked on top of Full-time)
        openFilterAndSelect(wait,
                "//button[contains(translate(.,'DATEPOSTED','dateposted'),'date posted')] | " +
                "//button[@id='filter-dateposted']",
                "Last 7 days"
        );
        wait.until(ExpectedConditions.urlContains("fromage=7"));
        pause();

        // Scroll through results with both filters active
        ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, 400);");
        pause();
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0);");
        pause();

        String url = driver.getCurrentUrl();
        Assert.assertTrue(url.contains("CF3CP"),
                "URL should still contain Indeed's Full-time attribute code (CF3CP) with both filters stacked.");
        Assert.assertTrue(url.contains("fromage=7"),
                "URL should contain 'fromage=7' with both filters stacked.");
        Assert.assertTrue(driver.findElements(By.cssSelector(".job_seen_beacon")).size() > 0,
                "Results should load with Full-time and Last 7 days filters applied together.");
        pause();
    }
}
