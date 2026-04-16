package org.example;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.List;
import java.util.Set;

/**
 * JobApplicationFlowTest
 *
 * Tests the complete job application journey on Indeed:
 *   1 — Search returns job listings with an Easy Apply filter
 *   2 — Clicking a job card opens the detail panel with an Apply button
 *   3 — Clicking Apply opens the application (new tab or modal)
 *   4 — The application page contains input fields
 *   5 — Clicking "Apply on company site" redirects to an external URL
 *
 * Covers a critical user flow not tested anywhere else in the suite.
 */
public class JobApplicationFlowTest extends BaseTest {

    private static final String SEARCH_URL     = "https://www.indeed.com/jobs?q=registered+nurse&l=Miami%2C+FL";
    private static final String EASY_APPLY_URL = "https://www.indeed.com/jobs?q=registered+nurse&l=Miami%2C+FL&iafilter=1";

    private void pause() {
        try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
    }

    /**
     * Locates the Apply button by visible text and aria-label.
     * XPath is used because Indeed's generated CSS class names change on every deploy.
     */
    private WebElement findApplyButton(WebDriverWait wait) {
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0);");
        return wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                "//button[contains(., 'Apply') and not(contains(., 'lert'))] | " +
                "//a[contains(., 'Apply') and not(contains(., 'lert'))] | " +
                "//button[contains(@aria-label, 'pply')] | " +
                "//a[contains(@aria-label, 'pply')] | " +
                "//*[@id='indeedApplyButton']"
        )));
    }

    /**
     * Clicks Apply and switches to the new tab if one opens.
     * Returns the original window handle so the caller can switch back later.
     */
    private String clickApplyAndSwitchTab(WebDriverWait wait) {
        String originalWindow = driver.getWindowHandle();
        Set<String> windowsBefore = driver.getWindowHandles();

        WebElement applyBtn = findApplyButton(wait);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", applyBtn);
        pause();

        wait.until(d ->
                d.getWindowHandles().size() > windowsBefore.size() ||
                !d.findElements(By.cssSelector("[role='dialog'], .ia-BasePage, #ia-container")).isEmpty()
        );

        Set<String> windowsAfter = driver.getWindowHandles();
        if (windowsAfter.size() > windowsBefore.size()) {
            for (String handle : windowsAfter) {
                if (!windowsBefore.contains(handle)) {
                    driver.switchTo().window(handle);
                    break;
                }
            }
            pause();
        }

        return originalWindow;
    }

    // Test 1: Easy Apply filter returns job listings
    @Test(priority = 1)
    public void testEasyApplyFilterReturnsListings() {
        driver.get(EASY_APPLY_URL);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".job_seen_beacon")));
        pause();

        List<WebElement> jobCards = driver.findElements(By.cssSelector(".job_seen_beacon"));
        Assert.assertTrue(jobCards.size() > 0,
                "Easy Apply filter should return at least one job listing.");
        pause();
    }

    // Test 2: Clicking a job card opens the detail panel and shows an Apply button
    @Test(priority = 2)
    public void testJobDetailPanelHasApplyButton() {
        driver.get(SEARCH_URL);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".job_seen_beacon")));
        pause();

        WebElement firstJob = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("h2.jobTitle a, a[data-jk]")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", firstJob);
        pause();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("jobDescriptionText")));
        pause();

        WebElement applyButton = findApplyButton(wait);
        Assert.assertTrue(applyButton.isDisplayed(),
                "An Apply button should be visible in the job detail panel.");
        pause();
    }

    // Test 3: Clicking Apply opens the application form (new tab or modal)
    @Test(priority = 3)
    public void testEasyApplyOpensApplicationForm() {
        driver.get(EASY_APPLY_URL);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".job_seen_beacon")));
        pause();

        WebElement firstJob = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("h2.jobTitle a")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", firstJob);
        pause();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("jobDescriptionText")));
        pause();

        clickApplyAndSwitchTab(wait);

        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("main")));
        pause();

        boolean hasForm = !driver.findElements(By.cssSelector(
                "input, textarea, form, [role='dialog'], [class*='apply'], [class*='Apply']")).isEmpty();

        Assert.assertTrue(hasForm,
                "The application form or page should contain form content. URL: " + driver.getCurrentUrl());
        pause();
    }

    // Test 4: The application page contains input fields
    @Test(priority = 4)
    public void testApplicationFormHasInputFields() {
        driver.get(EASY_APPLY_URL);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".job_seen_beacon")));
        pause();

        WebElement firstJob = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("h2.jobTitle a")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", firstJob);
        pause();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("jobDescriptionText")));
        pause();

        clickApplyAndSwitchTab(wait);

        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("main")));
        pause();

        List<WebElement> inputs = driver.findElements(By.cssSelector(
                "input[type='text'], input[type='email'], input[type='tel'], " +
                "textarea, input[type='file'], input[type='radio'], input[type='checkbox']"));

        Assert.assertTrue(inputs.size() > 0,
                "The application page should contain at least one input field.");
        pause();
    }

    // Test 5: Clicking "Apply on company site" opens an external URL in a new tab
    @Test(priority = 5)
    public void testApplyOnCompanySiteRedirects() {
        driver.get(SEARCH_URL);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".job_seen_beacon")));
        pause();

        String externalUrl = null;
        for (int i = 0; i < 8; i++) {
            List<WebElement> jobLinks = driver.findElements(By.cssSelector("h2.jobTitle a"));
            if (i >= jobLinks.size()) break;

            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", jobLinks.get(i));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("jobDescriptionText")));

            List<WebElement> companyBtns = driver.findElements(By.xpath(
                    "//button[contains(., 'company site')] | //a[contains(., 'company site')]"
            ));

            if (!companyBtns.isEmpty()) {
                pause();

                Set<String> windowsBefore = driver.getWindowHandles();

                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", companyBtns.get(0));
                pause();

                wait.until(d -> d.getWindowHandles().size() > windowsBefore.size());

                for (String handle : driver.getWindowHandles()) {
                    if (!windowsBefore.contains(handle)) {
                        driver.switchTo().window(handle);
                        break;
                    }
                }
                pause();

                externalUrl = driver.getCurrentUrl();
                break;
            }
        }

        Assert.assertNotNull(externalUrl,
                "Should have found a job listing with an 'Apply on company site' button.");
        Assert.assertFalse(externalUrl.contains("indeed.com"),
                "Clicking 'Apply on company site' should redirect to the company's external website. URL: " + externalUrl);
        pause();
    }
}
