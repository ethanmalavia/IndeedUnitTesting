package org.example;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;
import org.testng.annotations.Test;
import java.time.Duration;

public class NavigationTabsTest extends BaseTest {

    private void navigateAndVerify(By locator, String urlPart, String pageName) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));

        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);

        wait.until(ExpectedConditions.urlContains(urlPart));
        Assert.assertTrue(driver.getCurrentUrl().contains(urlPart), "Failed to navigate to " + pageName);
    }

    @Test(priority = 1)
    public void testHomeLink() {
        // Navigates via the 'Home' text link
        navigateAndVerify(By.linkText("Home"), "indeed.com", "Home");
    }

    @Test(priority = 2)
    public void testCompanyReviews() {
        navigateAndVerify(By.linkText("Company reviews"), "/companies", "Company reviews");
    }

    @Test(priority = 3)
    public void testFindSalaries() {
        navigateAndVerify(By.linkText("Find salaries"), "/salaries", "Find salaries");
    }

    @Test(priority = 4)
    public void testMyJobsIcon() {
        // Targets the heart/folder icon link
        navigateAndVerify(By.cssSelector("a[href*='/myjobs']"), "myjobs", "My Jobs");
    }
}