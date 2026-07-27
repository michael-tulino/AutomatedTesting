package autogen;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.appiancorp.ps.automatedtest.fixture.SitesFixture;

// Placeholder coverage for importing existing Object Tests from other registered IADC
// Applications into the current one. No requirements ticket yet - each @Test method below is a
// description of the scenario it will eventually verify, not a working implementation.
public class ImportTestGridTest {
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

  // Would verify that an "Import Tests" action is available from the Object Tests grid, allowing
  // tests to be pulled in from other registered applications.
  @Test
  public void testImportTestsActionIsPresentOnObjectTestsGrid() {
    // Description only - not yet implemented.
  }

  // Would verify that selecting one or more tests from another registered application and
  // submitting the Import Tests form adds copies of those tests to the current application's
  // Object Tests grid.
  @Test
  public void testSubmitOnImportTestsFormAddsSelectedTestsToCurrentApplication() {
    // Description only - not yet implemented.
  }

  // Would verify that clicking Cancel on the Import Tests form does not add any tests to the
  // current application's Object Tests grid.
  @Test
  public void testCancelOnImportTestsFormDoesNotAddTests() {
    // Description only - not yet implemented.
  }
}
