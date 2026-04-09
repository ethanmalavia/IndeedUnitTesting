package org.example;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * HomePageTest
 *
 * Verifies that the core UI elements of the Indeed homepage are present
 * and functioning before any user interaction takes place.
 *
 * URL under test: https://www.indeed.com
 */
public class HomePageTest extends BaseTest {

    @Test
    public void testPageTitleContainsIndeed() {
        navigateTo("https://www.indeed.com");
        String title = driver.getTitle();
        Assert.assertTrue(title.contains("Indeed"),
                "Page title should contain 'Indeed'. Actual title: " + title);
    }

    // Test 2: The "What" input field (job title / keywords) must be visible
    @Test
    public void testJobKeywordInputIsPresent() {
        navigateTo("https://www.indeed.com");
        WebElement whatInput = driver.findElement(By.id("text-input-what"));
        Assert.assertTrue(whatInput.isDisplayed(),
                "The job keyword input field should be visible on the homepage");
    }

    // Test 3: The "Where" input field (location) must be visible
    @Test
    public void testLocationInputIsPresent() {
        navigateTo("https://www.indeed.com");
        WebElement whereInput = driver.findElement(By.id("text-input-where"));
        Assert.assertTrue(whereInput.isDisplayed(),
                "The location input field should be visible on the homepage");
    }

    // Test 4: The search submit button must be visible and enabled
    @Test
    public void testSearchButtonIsPresentAndEnabled() {
        navigateTo("https://www.indeed.com");
        WebElement searchButton = driver.findElement(By.cssSelector("button[type='submit']"));
        Assert.assertTrue(searchButton.isDisplayed(),
                "The search button should be visible on the homepage");
        Assert.assertTrue(searchButton.isEnabled(),
                "The search button should be enabled on the homepage");
    }

    // Test 5: The page header must contain at least one navigation link
    @Test
    public void testHeaderNavigationLinksExist() {
        navigateTo("https://www.indeed.com");
        List<WebElement> headerLinks = driver.findElements(By.cssSelector("header a, nav a"));
        Assert.assertTrue(headerLinks.size() > 0,
                "The page header should contain at least one navigation link");
    }
}
