package autogen;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.appiancorp.ps.automatedtest.fixture.SitesFixture;

// Placeholder coverage for adding an IADC Object Test to a rule/object within an application.
// No requirements ticket yet - each @Test method below is a description of the scenario it will
// eventually verify, not a working implementation.
public class AddObjectTestTest {
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

  // Would verify that an "Add Object Test" action is available from the Object Tests grid on a
  // rule/object's detail view.
  @Test
  public void testAddObjectTestActionIsPresentOnObjectTestsGrid() {
    // Description only - not yet implemented.
  }

  // Would verify that submitting the Add Object Test form saves a new Object Test row to the
  // Object Tests Grid.
  @Test
  public void testSubmitOnAddObjectTestFormSavesTestToGrid() {
    // Description only - not yet implemented.
  }

  // Would verify that clicking Cancel on the Add Object Test form does not save a new Object
  // Test to the grid.
  @Test
  public void testCancelOnAddObjectTestFormDoesNotSaveTest() {
    // Description only - not yet implemented.
  }
}
