package autogen;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.appiancorp.ps.automatedtest.fixture.SitesFixture;

// Placeholder coverage for the "Start Review" action that kicks off a new IADC Code Review for
// an application/package. No requirements ticket yet - each @Test method below is a description
// of the scenario it will eventually verify, not a working implementation.
public class StartCodeReviewTest {
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

  // Would verify that a "Start Review" action is available for an application/package that has
  // an integration package uploaded.
  @Test
  public void testStartReviewActionIsPresentForUploadedPackage() {
    // Description only - not yet implemented.
  }

  // Would verify that submitting the Start Review form creates a new IADC Code Review record
  // showing up in the Code Reviews grid with an initial (e.g. In Progress/Pending) status.
  @Test
  public void testSubmitOnStartReviewFormCreatesCodeReviewRecord() {
    // Description only - not yet implemented.
  }

  // Would verify that clicking Cancel on the Start Review form does not create a new Code
  // Review record.
  @Test
  public void testCancelOnStartReviewFormDoesNotCreateCodeReview() {
    // Description only - not yet implemented.
  }
}
