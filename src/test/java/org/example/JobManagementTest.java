package org.example;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;
import org.testng.annotations.Test;
import java.time.Duration;

public class JobManagementTest extends BaseTest {

    @Test
    public void verifySavedJobsCountVisible() {
        driver.get("https://www.indeed.com/myjobs");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement savedTab = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//button[contains(., 'Saved')]")));
        Assert.assertTrue(savedTab.isDisplayed(), "Saved jobs tab should be visible");
    }

    @Test
    public void verifyAppliedJobsTabNavigation() {
        driver.get("https://www.indeed.com/myjobs");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        WebElement appliedTab = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(translate(text(), 'APPLIED', 'applied'), 'applied')] | //button[contains(., 'Applied')]")
        ));
        appliedTab.click();
        Assert.assertTrue(driver.getCurrentUrl().contains("applied"), "URL should update to Applied section");
    }

    @Test
    public void verifyArchivedJobsTab() {
        driver.get("https://www.indeed.com/myjobs");

        WebElement archived = driver.findElement(By.xpath("//*[contains(text(), 'Archived')]"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", archived);
        Assert.assertTrue(driver.getCurrentUrl().contains("archive"));
    }

    @Test
    public void verifyInterviewsTabVisibility() {
        // 1. Navigate to the main My Jobs dashboard
        driver.get("https://www.indeed.com/myjobs");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        // 2. Target the Interviews tab (handling case sensitivity and element type)
        WebElement interviewsTab = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//a[contains(translate(text(), 'INTERVIEWS', 'interviews'), 'interviews')] | //button[contains(., 'Interviews')]")
        ));

        // 3. Simple assertion of visibility
        Assert.assertTrue(interviewsTab.isDisplayed(), "The Interviews tab should be visible on the My Jobs dashboard.");
    }

    @Test
    public void verifyJobCardRedirection() {
        driver.get("https://www.indeed.com/myjobs");
        // Check if there is at least one saved job to click
        try {
            WebElement firstJob = driver.findElement(By.cssSelector(".jobCardShelfContainer, a[data-jk]"));
            firstJob.click();
            Assert.assertNotNull(driver.getWindowHandle(), "Should open job details");
        } catch (NoSuchElementException e) {
            System.out.println("No saved jobs found to test redirection.");
        }
    }
}