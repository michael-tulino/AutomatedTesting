package autogen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.appiancorp.ps.automatedtest.fixture.SitesFixture;

public class AddNewApplicationToIADCTest {

  private static final String TEST_BROWSER = "CHROME";
  private static final String TEST_SITE_VERSION = "24.3";
  private static final String TEST_SITE_URL = "https://ignytedemo.appiancloud.com/suite";
  private static final String TEST_SITE_LOCALE = "en_US";
  private static final String TEST_USERNAME = "automated.tester";
  private static final Integer TEST_TIMEOUT = 60;
  private static final String IADC_SITE_URL = "ignyte-appian-developer-copilo";

  private static final String ADD_APPLICATION_ACTION = "Add Application";
  private static final String APPLICATIONS_GRID = "[1]";

  private static final String APPLICATION_NAME = "Selenium QA Test App";
  private static final String ACRONYM = "SQTA";
  private static final String UUID_VALUE = "00000000-0000-4000-8000-000000000001";
  private static final String ENV_KEY = "ignytetest";
  private static final String ADMIN_GROUP_ID = "9999";

  private static final String CANCEL_APPLICATION_NAME = "Selenium QA Cancel Test App";

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
  void addApplicationSavesWithExpectedFieldsAndAppearsInApplicationsGrid() {
    fixture.navigateToSite(IADC_SITE_URL);
    fixture.clickOnSitePage("Settings");
    fixture.clickOnRecordRelatedAction(ADD_APPLICATION_ACTION);

    fixture.populateFieldWithValue("Application Name", APPLICATION_NAME);
    fixture.populateFieldWithValue("Acronym", ACRONYM);
    fixture.populateFieldWithValue("UUID", UUID_VALUE);
    fixture.populateFieldWithValue("Environment Key", ENV_KEY);
    fixture.populateFieldWithValue("Admin Group ID", ADMIN_GROUP_ID);
    fixture.clickOnButton("Submit");

    int row = locateRowByName(APPLICATION_NAME);
    assertTrue(row > 0,
        "Newly created application '" + APPLICATION_NAME + "' was not found in the Applications grid");

    assertEquals(APPLICATION_NAME, fixture.getGridColumnRowValue(APPLICATIONS_GRID, "Name", "[" + row + "]"));
    assertEquals(ACRONYM, fixture.getGridColumnRowValue(APPLICATIONS_GRID, "Acronym", "[" + row + "]"));
    assertEquals(ENV_KEY, fixture.getGridColumnRowValue(APPLICATIONS_GRID, "Env Key", "[" + row + "]"));
    assertEquals(UUID_VALUE, fixture.getGridColumnRowValue(APPLICATIONS_GRID, "UUID", "[" + row + "]"));
  }

  @Test
  void addApplicationCancelDoesNotSaveApplication() {
    fixture.navigateToSite(IADC_SITE_URL);
    fixture.clickOnSitePage("Settings");
    fixture.clickOnRecordRelatedAction(ADD_APPLICATION_ACTION);

    fixture.populateFieldWithValue("Application Name", CANCEL_APPLICATION_NAME);
    fixture.clickOnButton("Cancel");

    int row = locateRowByName(CANCEL_APPLICATION_NAME);
    assertTrue(row < 0,
        "Application '" + CANCEL_APPLICATION_NAME + "' should not have been saved after clicking Cancel, but was found in the Applications grid");
  }

  private static int locateRowByName(String targetName) {
    int totalCount = fixture.getGridTotalCount(APPLICATIONS_GRID);
    int checked = 0;
    while (checked < totalCount) {
      int rowCount = fixture.getGridRowCount(APPLICATIONS_GRID);
      for (int i = 1; i <= rowCount; i++) {
        String value = fixture.getGridColumnRowValue(APPLICATIONS_GRID, "Name", "[" + i + "]");
        if (targetName.equals(value)) {
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
