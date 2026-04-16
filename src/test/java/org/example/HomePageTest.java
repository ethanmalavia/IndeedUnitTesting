package org.example;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;

/**
 * HomePageTest
 *
 * Verifies that the core UI elements of the Indeed homepage are present,
 * functional, and respond correctly to real user interactions such as
 * typing, clicking, and scrolling.
 *
 * URL under test: https://www.indeed.com
 */
public class HomePageTest extends BaseTest {

    private void typeInto(WebElement element, CharSequence... keys) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].click(); arguments[0].focus();", element);
        new Actions(driver).sendKeys(element, keys).perform();
    }

    private void pause() {
        try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
    }

    // Test 1: Page title confirms we landed on Indeed
    @Test
    public void testPageTitleContainsIndeed() {
        pause();
        String title = driver.getTitle();
        Assert.assertTrue(title.contains("Indeed"),
                "Page title should contain 'Indeed'. Actual title: " + title);
        pause();
    }

    // Test 2: Typing triggers autocomplete suggestions, and clicking one populates the field
    @Test
    public void testKeywordAutocompleteAndSelection() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement whatInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("text-input-what")));

        pause();
        typeInto(whatInput, "nurse");
        pause();

        // Wait for the dropdown to appear then click the first suggestion
        WebElement firstSuggestion = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("[role='option'], [role='listbox'] li, [class*='autocomplete'] li")
        ));

        String suggestionText = firstSuggestion.getText().trim();
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", firstSuggestion);
        pause();

        wait.until(ExpectedConditions.attributeToBeNotEmpty(whatInput, "value"));
        String inputValue = whatInput.getAttribute("value");
        Assert.assertFalse(inputValue.isEmpty(),
                "Clicking a suggestion should populate the keyword input. Suggestion was: " + suggestionText);
        pause();
    }

    // Test 3: Both the search button (click) and Enter key navigate to results
    @Test
    public void testSearchSubmitsViaButtonAndEnterKey() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        // --- Part A: submit via button click ---
        WebElement whatInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("text-input-what")));
        pause();

        typeInto(whatInput, "Software Engineer");
        pause();

        WebElement whereInput = driver.findElement(By.id("text-input-where"));
        typeInto(whereInput, "Miami, FL");
        pause();

        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].click();", driver.findElement(By.cssSelector("button[type='submit']")));

        wait.until(ExpectedConditions.urlContains("/jobs"));
        pause();

        Assert.assertTrue(driver.getCurrentUrl().contains("/jobs"),
                "Clicking search should navigate to the jobs results page. Actual URL: " + driver.getCurrentUrl());

        // --- Part B: navigate back and submit via Enter key ---
        driver.navigate().back();
        wait.until(ExpectedConditions.urlToBe("https://www.indeed.com/"));
        pause();

        whatInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("text-input-what")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].value = '';", whatInput);
        typeInto(whatInput, "Data Analyst");
        pause();

        // Dismiss autocomplete first — ENTER on an open dropdown selects a suggestion, not the form
        new Actions(driver).sendKeys(Keys.ESCAPE).pause(Duration.ofMillis(300)).sendKeys(Keys.ENTER).perform();

        wait.until(ExpectedConditions.urlContains("/jobs"));
        pause();

        Assert.assertTrue(driver.getCurrentUrl().contains("/jobs"),
                "Pressing Enter should also submit the search form. Actual URL: " + driver.getCurrentUrl());
    }

    // Test 4: Scrolling down reveals content below the fold
    @Test
    public void testScrollingDownRevealsContent() {
        JavascriptExecutor js = (JavascriptExecutor) driver;

        pause();
        js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
        pause();

        Number scrollY = (Number) js.executeScript("return window.scrollY;");
        Assert.assertTrue(scrollY.doubleValue() > 0,
                "The page should scroll down and reveal content below the fold");
        pause();
    }

    // Test 5: Clicking a popular job category link on the homepage navigates to those results
    @Test
    public void testClickingJobCategoryNavigatesToResults() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        pause();

        // Scroll down to where popular category / trending search links appear
        ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, 500);");
        pause();

        // Find any job category link on the homepage (links to /jobs?q=...)
        WebElement categoryLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(@href,'/jobs?q=') or contains(@href,'/jobs?l=')]")));

        String categoryText = categoryLink.getText().trim();

        // Scroll it to the center of the viewport so it's clearly visible before clicking
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", categoryLink);
        pause();

        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", categoryLink);
        pause();

        // Full page navigation to the job results for that category
        wait.until(ExpectedConditions.urlContains("/jobs"));
        pause();

        // Scroll through the results that loaded for the chosen category
        ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, 400);");
        pause();

        Assert.assertTrue(driver.getCurrentUrl().contains("/jobs"),
                "Clicking category '" + categoryText + "' should navigate to job results. URL: " + driver.getCurrentUrl());
        pause();
    }
}
