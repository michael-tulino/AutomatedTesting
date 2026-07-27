package autogen;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.appiancorp.ps.automatedtest.fixture.SitesFixture;

// Placeholder coverage for configuring an Object Test's fail-override setting, which lets a
// failing test be manually marked as overridden. No requirements ticket yet - each @Test method
// below is a description of the scenario it will eventually verify, not a working implementation.
public class ConfigureTestFailOverrideTest {
  private static final String TEST_SITE_URL = "https://ignytedemo.appiancloud.com/suite";
  private static final String IADC_SITE_URL = "ignyte-appian-developer-copilo";
  private static final String TEST_USERNAME = "automated.tester";
  private static final String TEST_BROWSER = "CHROME";
  private static final String TEST_SITE_VERSION = "24.3";
  private static final String TEST_SITE_LOCALE = "en_US";
  private static final Integer TEST_TIMEOUT = 60;

  private static SitesFixture fixture;

  @BeforeAll
  public static void setup() {
    fixture = new SitesFixture();
    fixture.setTakeErrorScreenshotsTo(true);
    fixture.setupWithBrowser(TEST_BROWSER);
    fixture.setAppianUrlTo(TEST_SITE_URL);
    fixture.setTimeoutSecondsTo(TEST_TIMEOUT);
    fixture.setAppianVersionTo(TEST_SITE_VERSION);
    fixture.setAppianLocaleTo(TEST_SITE_LOCALE);
    fixture.loginWithUsername(TEST_USERNAME);
  }

  @AfterAll
  public static void cleanup() {
    fixture.tearDown();
  }

  // Would verify that a "Configure Test" action is available on a failing Object Test result
  // row.
  @Test
  public void testConfigureTestActionIsPresentOnFailingTestResult() {
    // Description only - not yet implemented.
  }

  // Would verify that enabling the Test Override setting on a failing Object Test changes its
  // displayed status icon to reflect the override, rather than the original failure.
  @Test
  public void testEnablingTestOverrideChangesFailingTestIcon() {
    // Description only - not yet implemented.
  }

  // Would verify that clearing a previously-set Test Override reverts the Object Test's status
  // icon back to its original pass/fail result.
  @Test
  public void testClearingTestOverrideRevertsToOriginalResultIcon() {
    // Description only - not yet implemented.
  }
}
