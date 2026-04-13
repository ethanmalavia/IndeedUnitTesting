package org.example;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * BaseTest
 *
 * Shared setup, teardown, and navigation helper for every test class.
 *
 * -----------------------------------------------------------------------
 * AUTOMATION CONSTRAINT: Cloudflare Bot Protection
 * -----------------------------------------------------------------------
 * Indeed.com is protected by Cloudflare, which detects and challenges
 * automated browsers.
 */
public class BaseTest {

    protected ChromeDriver driver;
    @BeforeMethod
    public void setUp() throws InterruptedException {
        ChromeOptions options = new ChromeOptions();
        /*
        1. Enter chrome://version into your chrome browser, and copy the text after "Profile Path"
        2. Close ALL instances of chrome; task manager them. Chrome will not allow multiple instances to run under the same profile
        3. PUT THE PROFILE PATH IN THE LINE BELOW HERE AFTER 'user-data-dir='. Change nothing else.
        4. We will have to log in every time a test runs, but at least we got in. The 30 second stoppage is to give the time to do so.
        */
        options.addArguments("user-data-dir=C:\\Users\\Ronnie\\AppData\\Local\\Google\\Chrome\\User Data\\Default");

        // Point to the specific profile folder (usually 'Default')
        options.addArguments("profile-directory=Default");

        // Standard Stealth flags
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.setExperimentalOption("excludeSwitches", List.of("enable-automation"));
        options.setExperimentalOption("useAutomationExtension", false);

        driver = new ChromeDriver(options);

        // 5. Navigate directly - your login and cf_clearance should persist
        driver.get("https://www.indeed.com");
        Thread.sleep(5000);
    }
    /*
    I think I figured it out; the following requires you to open a debugging version of chrome to grab a valid cookie to get around cloudflare, and put the cookie in cfToken

    ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("debuggerAddress", "127.0.0.1:9222");

        WebDriver driver = new ChromeDriver(options);

        System.out.println("Connected to: " + driver.getTitle());

        // Grab the cookies
        Set<Cookie> cookies = driver.manage().getCookies();
        for (Cookie ck : cookies) {
            if (ck.getName().contains("cf_clearance")) {
                System.out.println("SUCCESS! Copy this value: " + ck.getValue());
            }
        }

    */

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    /**
     * Navigates to a URL and handles Cloudflare's bot-verification page.
     *
     * Step 1 — Navigate and wait up to 5 seconds for an automatic resolution.
     *           Cloudflare's JavaScript challenge often self-completes for
     *           browsers that pass its fingerprint checks.
     *
     * Step 2 — If the challenge is still present after 5 seconds, print a
     *           console message and wait up to 60 seconds for the tester to
     *           click through the verification in the open browser window.
     *
     * Step 3 — If the challenge is not resolved within 60 seconds, fail the
     *           test with a descriptive message rather than hanging forever.
     *
     * All test classes call this method instead of driver.get() so that
     * Cloudflare handling is consistent across the entire test suite.
     */
   /* protected void navigateTo(String url) {
        driver.get(url);

        // Step 1: give Cloudflare up to 5 seconds to auto-resolve
        try {
            new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(d -> !d.getTitle().contains("Just a moment")
                          && !d.getTitle().contains("Attention Required")
                          && !d.getTitle().contains("Please Wait"));
            return; // page loaded cleanly — no challenge present
        } catch (TimeoutException e) {
            // Challenge is still showing — fall through to manual step
        }

        // Step 2: prompt for manual resolution
        System.out.println("\n========================================================");
        System.out.println(" CLOUDFLARE VERIFICATION REQUIRED");
        System.out.println(" Please complete the check in the browser window.");
        System.out.println(" The test will resume automatically once it is done.");
        System.out.println(" Waiting up to 60 seconds...");
        System.out.println("========================================================\n");

        // Step 3: wait for manual resolution or fail
        try {
            new WebDriverWait(driver, Duration.ofSeconds(60))
                .until(d -> !d.getTitle().contains("Just a moment")
                          && !d.getTitle().contains("Attention Required")
                          && !d.getTitle().contains("Please Wait"));
            System.out.println("[Cloudflare] Verification passed. Resuming test.\n");
        } catch (TimeoutException e) {
            Assert.fail(
                "Cloudflare verification was not completed within 60 seconds. " +
                "Please solve the challenge manually when it appears in the browser window."
            );
        }
    }*/
}
