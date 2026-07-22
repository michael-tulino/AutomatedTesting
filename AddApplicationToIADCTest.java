package autogen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.appiancorp.ps.automatedtest.fixture.SitesFixture;

public class AddApplicationToIADCTest {

  private static final String TEST_BROWSER = "CHROME";
  private static final String TEST_SITE_VERSION = "24.3";
  private static final String TEST_SITE_URL = "https://ignytedemo.appiancloud.com/suite";
  private static final String TEST_SITE_LOCALE = "en_US";
  private static final String TEST_USERNAME = "automated.tester";
  private static final Integer TEST_TIMEOUT = 60;
  private static final String IADC_SITE_URL = "ignyte-appian-developer-copilo";

  private static final String ADD_APPLICATION_ACTION = "Add Application";
  private static final String APPLICATIONS_GRID = "[1]";

  private static final String FIELD_APPLICATION_NAME = "Application Name";
  private static final String FIELD_ACRONYM = "Acronym";
  private static final String FIELD_UUID = "UUID";
  private static final String FIELD_ENVIRONMENT_KEY = "Environment Key";
  private static final String FIELD_ADMIN_GROUP_ID = "Admin Group ID";

  private static final String ENVIRONMENT_KEY_TOOLTIP =
      "This is a key specifying the environment for this application. Use the base url before appiancloud.com. "
          + "For example, for ignytedemo.appiancloud.com, the key should be ignytedemo.";
  private static final String ADMIN_GROUP_ID_TOOLTIP =
      "Group ID for the Admin Group of the application to add. Example: 1146";

  private static final String COLUMN_ACRONYM = "Acronym";
  private static final String COLUMN_NAME = "Name";
  private static final String COLUMN_ENV_KEY = "Env Key";
  private static final String COLUMN_UUID = "UUID";

  private static final String CANCEL_APPLICATION_NAME = "IV-240 Cancel Test Application";
  private static final String CANCEL_ACRONYM = "IVCT";
  private static final String CANCEL_UUID = "11111111-1111-1111-1111-111111111111";
  private static final String CANCEL_ENV_KEY = "ivcanceltest";
  private static final String CANCEL_ADMIN_GROUP_ID = "1146";

  private static final String SUBMIT_APPLICATION_NAME = "IV-240 Submit Test Application";
  private static final String SUBMIT_ACRONYM = "IVST";
  private static final String SUBMIT_UUID = "22222222-2222-2222-2222-222222222222";
  private static final String SUBMIT_ENV_KEY = "ivsubmittest";
  private static final String SUBMIT_ADMIN_GROUP_ID = "1147";

  private static SitesFixture fixture;

  @BeforeAll
  static void setUp() {
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
  static void tearDown() {
    fixture.tearDown();
  }

  @Test
  void addApplicationFormShowsExpectedFieldsAndCancelDoesNotSave() {
    fixture.navigateToSite(IADC_SITE_URL);
    fixture.clickOnSitePage("Settings");

    assertTrue(fixture.verifyButtonIsPresent(ADD_APPLICATION_ACTION),
        "'" + ADD_APPLICATION_ACTION + "' action should be available on the Settings tab");

    fixture.clickOnButton(ADD_APPLICATION_ACTION);

    assertTrue(fixture.verifyFieldIsPresent(FIELD_APPLICATION_NAME), "Application Name field should be present");
    assertTrue(fixture.verifyFieldIsPresent(FIELD_ACRONYM), "Acronym field should be present");
    assertTrue(fixture.verifyFieldIsPresent(FIELD_UUID), "UUID field should be present");
    assertTrue(fixture.verifyFieldIsPresent(FIELD_ENVIRONMENT_KEY), "Environment Key field should be present");
    assertTrue(fixture.verifyFieldIsPresent(FIELD_ADMIN_GROUP_ID), "Admin Group ID field should be present");
    assertTrue(fixture.verifyButtonIsPresent("Submit"), "Submit button should be present");
    assertTrue(fixture.verifyButtonIsPresent("Cancel"), "Cancel button should be present");

    fixture.populateFieldWithValue(FIELD_APPLICATION_NAME, CANCEL_APPLICATION_NAME);
    fixture.populateFieldWithValue(FIELD_ACRONYM, CANCEL_ACRONYM);
    fixture.populateFieldWithValue(FIELD_UUID, CANCEL_UUID);
    fixture.populateFieldWithValue(FIELD_ENVIRONMENT_KEY, CANCEL_ENV_KEY);
    fixture.populateFieldWithValue(FIELD_ADMIN_GROUP_ID, CANCEL_ADMIN_GROUP_ID);

    assertTrue(fixture.verifyFieldWithTooltipContains(ENVIRONMENT_KEY_TOOLTIP, new String[] { CANCEL_ENV_KEY }),
        "Environment Key field should carry the documented help tooltip");
    assertTrue(fixture.verifyFieldWithTooltipContains(ADMIN_GROUP_ID_TOOLTIP, new String[] { CANCEL_ADMIN_GROUP_ID }),
        "Admin Group ID field should carry the documented help tooltip");

    fixture.clickOnButton("Cancel");

    assertEquals(-1, locateRowByColumnValue(COLUMN_NAME, CANCEL_APPLICATION_NAME),
        "Cancel should not save the application to the Applications grid");
  }

  @Test
  void submitAddApplicationFormSavesApplicationAndItAppearsInApplicationsGrid() {
    fixture.navigateToSite(IADC_SITE_URL);
    fixture.clickOnSitePage("Settings");
    fixture.clickOnButton(ADD_APPLICATION_ACTION);

    fixture.populateFieldWithValue(FIELD_APPLICATION_NAME, SUBMIT_APPLICATION_NAME);
    fixture.populateFieldWithValue(FIELD_ACRONYM, SUBMIT_ACRONYM);
    fixture.populateFieldWithValue(FIELD_UUID, SUBMIT_UUID);
    fixture.populateFieldWithValue(FIELD_ENVIRONMENT_KEY, SUBMIT_ENV_KEY);
    fixture.populateFieldWithValue(FIELD_ADMIN_GROUP_ID, SUBMIT_ADMIN_GROUP_ID);
    fixture.clickOnButton("Submit");

    int row = locateRowByColumnValue(COLUMN_NAME, SUBMIT_APPLICATION_NAME);
    assertTrue(row > 0,
        "Newly created application '" + SUBMIT_APPLICATION_NAME + "' was not found in the Applications grid");

    assertEquals(SUBMIT_APPLICATION_NAME, fixture.getGridColumnRowValue(APPLICATIONS_GRID, COLUMN_NAME, "[" + row + "]"));
    assertEquals(SUBMIT_ACRONYM, fixture.getGridColumnRowValue(APPLICATIONS_GRID, COLUMN_ACRONYM, "[" + row + "]"));
    assertEquals(SUBMIT_ENV_KEY, fixture.getGridColumnRowValue(APPLICATIONS_GRID, COLUMN_ENV_KEY, "[" + row + "]"));
    assertEquals(SUBMIT_UUID, fixture.getGridColumnRowValue(APPLICATIONS_GRID, COLUMN_UUID, "[" + row + "]"));

    // IADC Application has no deactivate/delete record action, so the created test row is left in place.
  }

  private static int locateRowByColumnValue(String columnName, String targetValue) {
    int totalCount = fixture.getGridTotalCount(APPLICATIONS_GRID);
    int checked = 0;
    while (checked < totalCount) {
      int rowCount = fixture.getGridRowCount(APPLICATIONS_GRID);
      for (int i = 1; i <= rowCount; i++) {
        String value = fixture.getGridColumnRowValue(APPLICATIONS_GRID, columnName, "[" + i + "]");
        if (targetValue.equals(value)) {
          return i;
        }
      }
      checked += rowCount;
      if (checked < totalCount) {
        fixture.clickOnGridNavigation(APPLICATIONS_GRID, "next");
      }
    }
    return -1;
  }
}
