package autogen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.appiancorp.ps.automatedtest.fixture.SitesFixture;

// IV-240: Add a new Application from the IADC Site's Settings tab.
public class AddApplicationTest {
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
  private static final String ACRONYM = "IV240";
  private static final String UUID_VALUE = "11111111-1111-1111-1111-111111111111";
  private static final String ENV_KEY = "ignytedemo";
  private static final String ADMIN_GROUP_ID = "1146";
  private static final String ENV_KEY_TOOLTIP =
      "This is a key specifying the environment for this application. Use the base url before "
          + "appiancloud.com. For example, for ignytedemo.appiancloud.com, the key should be ignytedemo.";
  private static final String ADMIN_GROUP_ID_TOOLTIP =
      "Group ID for the Admin Group of the application to add. Example: 1146";

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

    navigateToSettingsTab();
    fixture.clickOnAction("Add Application");
    populateAddApplicationForm(applicationName);

    assertEquals(
        ENV_KEY,
        fixture.getFieldWithTooltipValue(ENV_KEY_TOOLTIP),
        "Environment Key field should display the help tooltip described in the ticket.");
    assertEquals(
        ADMIN_GROUP_ID,
        fixture.getFieldWithTooltipValue(ADMIN_GROUP_ID_TOOLTIP),
        "Admin Group ID field should display the help tooltip described in the ticket.");

    fixture.clickOnButton("Submit");

    navigateToSettingsTab();
    assertTrue(
        applicationIsPresentInGrid(applicationName),
        "Application [" + applicationName
            + "] should show up in the Applications Grid after submitting the Add Application form.");

    // No cleanup mechanism available via this fixture for IADC Application: only an
    // "Update Application" related action is exposed, with no deactivate/delete action
    // and no isActive field on the Add/Update form.
  }

  @Test
  public void testCancelOnAddApplicationFormDoesNotSaveApplication() {
    String applicationName = APPLICATION_NAME_PREFIX + fixture.getRandomAlphabetString(8);

    navigateToSettingsTab();
    fixture.clickOnAction("Add Application");
    populateAddApplicationForm(applicationName);
    fixture.clickOnButton("Cancel");

    navigateToSettingsTab();
    assertFalse(
        applicationIsPresentInGrid(applicationName),
        "Application [" + applicationName
            + "] should not be saved to the Applications Grid after clicking Cancel.");
  }

  private void navigateToSettingsTab() {
    fixture.navigateToSite(IADC_SITE_URL);
    fixture.clickOnSitePage("Settings");
  }

  private void populateAddApplicationForm(String applicationName) {
    populateRequiredField("Application Name", applicationName);
    populateRequiredField("Acronym", ACRONYM);
    populateRequiredField("UUID", UUID_VALUE);
    populateRequiredField("Environment Key", ENV_KEY);
    populateRequiredField("Admin Group ID", ADMIN_GROUP_ID);
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

  private boolean applicationIsPresentInGrid(String applicationName) {
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
