package autogen;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.appiancorp.ps.automatedtest.fixture.SitesFixture;

// IV-240: Add a new "Add Application" action to the Settings tab that launches a form to
// create a new IADC Application, with Cancel/Submit buttons.
public class AddIadcApplicationTest {
  private static final String TEST_SITE_URL = "https://ignytedemo.appiancloud.com/suite";
  private static final String IADC_SITE_URL = "ignyte-appian-developer-copilo";
  private static final String TEST_USERNAME = "automated.tester";
  private static final String TEST_BROWSER = "CHROME";
  private static final String TEST_SITE_VERSION = "24.3";
  private static final String TEST_SITE_LOCALE = "en_US";
  private static final Integer TEST_TIMEOUT = 60;

  private static final String APPLICATIONS_GRID_INDEX = "[1]";
  private static final int MAX_PAGES_TO_SCAN = 25;
  private static final String APPLICATION_NAME_PREFIX = "IV240_";
  private static final String ACRONYM_PREFIX = "IV";
  private static final String ENV_KEY = "ignytedemo";
  private static final String ADMIN_GROUP_ID = "1146";

  private static final Logger LOG = LogManager.getLogger(AddIadcApplicationTest.class);
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

  @Test
  public void testAddApplicationActionIsPresentOnSettingsTab() {
    navigateToSettingsTab();

    assertTrue(
        fixture.verifyButtonIsPresent("Add Application"),
        "The [Add Application] action should be present on the Settings tab.");
  }

  @Test
  public void testSubmitOnAddApplicationFormSavesApplicationToApplicationsGrid() {
    String applicationName = APPLICATION_NAME_PREFIX + fixture.getRandomAlphabetString(8);
    String acronym = ACRONYM_PREFIX + fixture.getRandomAlphabetString(4);
    String uuid = fixture.getRandomAlphabetString(12);

    navigateToSettingsTab();
    fixture.clickOnButton("Add Application");
    populateAddApplicationForm(applicationName, acronym, uuid, ENV_KEY, ADMIN_GROUP_ID);
    fixture.clickOnButton("Submit");

    navigateToSettingsTab();
    assertTrue(
        applicationIsPresentInApplicationsGrid(applicationName),
        "Application [" + applicationName
            + "] should show up in the Applications Grid after submitting the Add Application form.");

    // No cleanup mechanism available via this fixture for IADC Application records: the only
    // record actions on this record type are Add Application, Update Application, Add Rule, and
    // Clone Test Rules, and neither the Add nor the Update form exposes the isActive field that
    // the Applications Grid filters on.
  }

  @Test
  public void testCancelOnAddApplicationFormDoesNotSaveApplication() {
    String applicationName = APPLICATION_NAME_PREFIX + fixture.getRandomAlphabetString(8);
    String acronym = ACRONYM_PREFIX + fixture.getRandomAlphabetString(4);
    String uuid = fixture.getRandomAlphabetString(12);

    navigateToSettingsTab();
    fixture.clickOnButton("Add Application");
    populateAddApplicationForm(applicationName, acronym, uuid, ENV_KEY, ADMIN_GROUP_ID);
    fixture.clickOnButton("Cancel");

    navigateToSettingsTab();
    assertFalse(
        applicationIsPresentInApplicationsGrid(applicationName),
        "Application [" + applicationName
            + "] should not be saved to the Applications Grid after clicking Cancel.");
  }

  private void navigateToSettingsTab() {
    fixture.navigateToSite(IADC_SITE_URL);
    fixture.clickOnSitePage("Settings");
  }

  private void populateAddApplicationForm(
      String applicationName, String acronym, String uuid, String envKey, String adminGroupId) {
    populateRequiredField("Application Name", applicationName);
    populateRequiredField("Acronym", acronym);
    populateRequiredField("UUID", uuid);
    populateRequiredField("Environment Key", envKey);
    populateRequiredField("Admin Group ID", adminGroupId);
  }

  private void populateRequiredField(String fieldName, String value) {
    String currentValue = fixture.getFieldValue(fieldName);
    if (value.equals(currentValue)) {
      LOG.debug("SKIP POPULATE (already set) [" + fieldName + "]");
      return;
    }

    try {
      fixture.populateFieldWithValue(fieldName, value);
    } catch (RuntimeException e) {
      fail("Required field [" + fieldName + "] was not editable: " + e.getMessage());
    }
  }

  private boolean applicationIsPresentInApplicationsGrid(String applicationName) {
    for (int page = 1; page <= MAX_PAGES_TO_SCAN; page++) {
      int rowCount = fixture.getGridRowCount(APPLICATIONS_GRID_INDEX);
      for (int row = 1; row <= rowCount; row++) {
        String name = fixture.getGridColumnRowValue(APPLICATIONS_GRID_INDEX, "Name", "[" + row + "]");
        if (applicationName.equals(name)) {
          return true;
        }
      }

      try {
        fixture.clickOnGridNavigation(APPLICATIONS_GRID_INDEX, "next");
      } catch (RuntimeException e) {
        return false;
      }
    }

    return false;
  }
}
