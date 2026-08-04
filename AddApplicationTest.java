package autogen;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.appiancorp.ps.automatedtest.fixture.SitesFixture;

// IV-240: Add a new "Add Application" action to the Settings tab that launches a form to create
// a new IADC Application, with Cancel/Submit buttons.
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

  private static final String APP_NAME_PREFIX = "IV240_";
  private static final String ENVIRONMENT_KEY_TOOLTIP =
      "This is a key specifying the environment for this application. Use the base url before "
          + "appiancloud.com. For example, for ignytedemo.appiancloud.com, the key should be "
          + "ignytedemo.";
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
  public void testAddApplicationActionLaunchesFormWithExpectedFields() {
    navigateToSettings();

    assertTrue(
        fixture.verifyButtonIsPresent("Add Application"),
        "The [Add Application] action should be present on the Settings tab.");

    fixture.clickOnButton("Add Application");

    assertTrue(
        fixture.verifyFieldIsPresent("Application Name"),
        "The [Application Name] field should be present on the Add Application form.");
    assertTrue(
        fixture.verifyFieldIsPresent("Acronym"),
        "The [Acronym] field should be present on the Add Application form.");
    assertTrue(
        fixture.verifyFieldIsPresent("UUID"),
        "The [UUID] field should be present on the Add Application form.");
    assertTrue(
        fixture.verifyFieldIsPresent("Environment Key"),
        "The [Environment Key] field should be present on the Add Application form.");
    assertTrue(
        fixture.verifyFieldIsPresent("Admin Group ID"),
        "The [Admin Group ID] field should be present on the Add Application form.");

    fixture.clickOnButton("Cancel");
  }

  @Test
  public void testEnvironmentKeyFieldShowsExpectedHelpTooltip() {
    navigateToSettings();
    fixture.clickOnButton("Add Application");

    assertTrue(
        fieldWithTooltipIsPresent(ENVIRONMENT_KEY_TOOLTIP),
        "The [Environment Key] field should show the expected help tooltip.");

    fixture.clickOnButton("Cancel");
  }

  @Test
  public void testAdminGroupIdFieldShowsExpectedHelpTooltip() {
    navigateToSettings();
    fixture.clickOnButton("Add Application");

    assertTrue(
        fieldWithTooltipIsPresent(ADMIN_GROUP_ID_TOOLTIP),
        "The [Admin Group ID] field should show the expected help tooltip.");

    fixture.clickOnButton("Cancel");
  }

  @Test
  public void testCancelOnAddApplicationFormDoesNotSaveApplication() {
    String appName = APP_NAME_PREFIX + fixture.getRandomAlphabetString(8);

    navigateToSettings();
    fixture.clickOnButton("Add Application");
    populateRequiredField("Application Name", appName);
    fixture.clickOnButton("Cancel");

    navigateToSettings();
    assertFalse(
        applicationIsPresentInApplicationsGrid(appName),
        "Application [" + appName
            + "] should not be saved to the Applications Grid after clicking Cancel.");

    // Nothing was persisted - Cancel does not save - so no cleanup is needed.
  }

  @Test
  public void testSubmitOnAddApplicationFormSavesApplicationToApplicationsGrid() {
    String suffix = fixture.getRandomAlphabetString(8);
    String appName = APP_NAME_PREFIX + suffix;
    String acronym = "AC" + suffix.substring(0, 4);
    String uuidValue = UUID.randomUUID().toString();
    String environmentKey = "iv240" + suffix.toLowerCase();
    String adminGroupId = "1146";

    navigateToSettings();
    fixture.clickOnButton("Add Application");
    populateRequiredField("Application Name", appName);
    populateRequiredField("Acronym", acronym);
    populateRequiredField("UUID", uuidValue);
    populateRequiredField("Environment Key", environmentKey);
    populateRequiredField("Admin Group ID", adminGroupId);
    fixture.clickOnButton("Submit");

    navigateToSettings();
    assertTrue(
        applicationIsPresentInApplicationsGrid(appName),
        "Application [" + appName
            + "] should show up in the Applications Grid after submitting the Add Application "
            + "form.");

    // No cleanup mechanism available via this fixture for an IADC Application - the record
    // type's only actions are Add Application, Update Application, Add Rule, and Clone Test
    // Rules, none of which deactivate or delete an application.
  }

  private void navigateToSettings() {
    fixture.navigateToSite(IADC_SITE_URL);
    fixture.clickOnSitePage("Settings");
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

  private boolean fieldWithTooltipIsPresent(String tooltip) {
    try {
      fixture.getFieldWithTooltipValue(tooltip);
      return true;
    } catch (RuntimeException e) {
      return false;
    }
  }

  private boolean applicationIsPresentInApplicationsGrid(String appName) {
    for (int page = 1; page <= MAX_PAGES_TO_SCAN; page++) {
      int rowCount = fixture.getGridRowCount(APPLICATIONS_GRID_INDEX);
      for (int row = 1; row <= rowCount; row++) {
        String name =
            fixture.getGridColumnRowValue(APPLICATIONS_GRID_INDEX, "Name", "[" + row + "]");
        if (appName.equals(name)) {
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
