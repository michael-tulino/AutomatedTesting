package autogen;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.appiancorp.ps.automatedtest.fixture.SitesFixture;

// Placeholder coverage for the "Clone Test Rules" record action, which copies an existing IADC
// Application's Rules into another Application. No requirements ticket yet - each @Test method
// below is a description of the scenario it will eventually verify, not a working implementation.
public class CloneTestRulesTest {
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

  // Would verify that a "Clone Test Rules" record action is available from an IADC Application's
  // Settings summary tab.
  @Test
  public void testCloneTestRulesActionIsPresentOnApplicationSummary() {
    // Description only - not yet implemented.
  }

  // Would verify that selecting a target Application and one or more source Rules, then clicking
  // Submit, adds copies of those Rules to the target Application's Rules Grid.
  @Test
  public void testSubmitOnCloneTestRulesFormCopiesRulesToTargetApplication() {
    // Description only - not yet implemented.
  }

  // Would verify that clicking Cancel on the Clone Test Rules form does not add any rules to the
  // target Application's Rules Grid.
  @Test
  public void testCancelOnCloneTestRulesFormDoesNotCopyRules() {
    // Description only - not yet implemented.
  }
}
