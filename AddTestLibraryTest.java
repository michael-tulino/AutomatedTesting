package autogen;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.appiancorp.ps.automatedtest.fixture.SitesFixture;

// Placeholder coverage for creating an IADC Test Library. No requirements ticket yet - each
// @Test method below is a description of the scenario it will eventually verify, not a working
// implementation.
public class AddTestLibraryTest {
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

  // Would verify that an "Add Test Library" action is available from wherever Test Libraries
  // are managed in the IADC site.
  @Test
  public void testAddTestLibraryActionIsPresent() {
    // Description only - not yet implemented.
  }

  // Would verify that submitting the Add Test Library form with a name and its associated
  // information saves a new Test Library that then shows up in the Test Libraries listing.
  @Test
  public void testSubmitOnAddTestLibraryFormSavesNewLibrary() {
    // Description only - not yet implemented.
  }

  // Would verify that clicking Cancel on the Add Test Library form does not save a new Test
  // Library.
  @Test
  public void testCancelOnAddTestLibraryFormDoesNotSaveLibrary() {
    // Description only - not yet implemented.
  }
}
