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

// IV-240: Add ability to add new Applications in the System from the IADC Settings tab.
public class AddApplicationTest {
  private static final String TEST_SITE_URL = "https://ignytedemo.appiancloud.com/suite";
  private static final String IADC_SITE_URL = "ignyte-appian-developer-copilo";
  private static final String TEST_USERNAME = "automated.tester";
  private static final String TEST_BROWSER = "CHROME";
  private static final String TEST_SITE_VERSION = "24.3";
  private static final String TEST_SITE_LOCALE = "en_US";
  private static final Integer TEST_TIMEOUT = 60;

  private static final String ADD_APPLICATION_ACTION = "Add Application";
  private static final String APPLICATIONS_GRID_INDEX = "[1]";
  private static final int MAX_PAGES_TO_SCAN = 25;

  private static final String APPLICATION_NAME_PREFIX = "IV240_";
  private static final String ACRONYM_PREFIX = "IV";
  private static final String UUID_PREFIX = "uuid-iv240-";
  private static final String ENV_KEY_PREFIX = "envkey";
  private static final int ADMIN_GROUP_ID_MIN = 1000;
  private static final int ADMIN_GROUP_ID_MAX = 9999;

  private static final Logger LOG = LogManager.getLogger(AddApplicationTest.class);
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
  public void testSubmitOnAddApplicationFormSavesApplicationToApplicationsGrid() {
    String applicationName = APPLICATION_NAME_PREFIX + fixture.getRandomAlphabetString(8);

    navigateToAddApplicationForm();
    populateAddApplicationForm(applicationName);
    fixture.clickOnButton("Submit");

    navigateToSettingsTab();
    assertTrue(
        applicationIsPresentInApplicationsGrid(applicationName),
        "Application [" + applicationName
            + "] should show up in the Applications Grid after submitting the Add Application form.");

    // No cleanup mechanism available via this fixture for IADC Application records:
    // the record type exposes only Add/Update Application actions, and the Update
    // Application form has no field to deactivate or otherwise reverse the save.
  }

  @Test
  public void testCancelOnAddApplicationFormDoesNotSaveApplication() {
    String applicationName = APPLICATION_NAME_PREFIX + fixture.getRandomAlphabetString(8);

    navigateToAddApplicationForm();
    populateAddApplicationForm(applicationName);
    fixture.clickOnButton("Cancel");

    navigateToSettingsTab();
    assertFalse(
        applicationIsPresentInApplicationsGrid(applicationName),
        "Application [" + applicationName + "] should not be saved to the Applications Grid after clicking Cancel.");
  }

  private void navigateToSettingsTab() {
    fixture.navigateToSite(IADC_SITE_URL);
    fixture.clickOnSitePage("Settings");
  }

  private void navigateToAddApplicationForm() {
    navigateToSettingsTab();
    fixture.clickOnAction(ADD_APPLICATION_ACTION);
  }

  private void populateAddApplicationForm(String applicationName) {
    String acronym = ACRONYM_PREFIX + fixture.getRandomAlphabetString(4);
    String uuid = UUID_PREFIX + fixture.getRandomAlphabetString(12);
    String envKey = ENV_KEY_PREFIX + fixture.getRandomAlphabetString(6);
    String adminGroupId = String.valueOf(fixture.getRandomIntegerFromTo(ADMIN_GROUP_ID_MIN, ADMIN_GROUP_ID_MAX));

    populateFormField("Application Name", applicationName);
    populateFormField("Acronym", acronym);
    populateFormField("UUID", uuid);
    populateFormField("Environment Key", envKey);
    populateFormField("Admin Group ID", adminGroupId);
  }

  private void populateFormField(String fieldName, String value) {
    String currentValue = fixture.getFieldValue(fieldName);
    if (value.equals(currentValue)) {
      LOG.debug("SKIP POPULATE (already set) [" + fieldName + "]");
      return;
    }

    try {
      fixture.populateFieldWithValue(fieldName, value);
    } catch (RuntimeException e) {
      fail("Field [" + fieldName + "] was not editable: " + e.getMessage());
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
