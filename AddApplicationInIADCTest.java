package autogen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.appiancorp.ps.automatedtest.fixture.SitesFixture;

public class AddApplicationInIADCTest {

  private static final String TEST_BROWSER = "CHROME";
  private static final String TEST_SITE_VERSION = "24.3";
  private static final String TEST_SITE_URL = "https://ignytedemo.appiancloud.com/suite";
  private static final String TEST_SITE_LOCALE = "en_US";
  private static final String TEST_USERNAME = "automated.tester";
  private static final Integer TEST_TIMEOUT = 60;
  private static final String IADC_SITE_URL = "ignyte-appian-developer-copilo";

  private static final String SETTINGS_PAGE = "Settings";
  private static final String ADD_APPLICATION_ACTION = "Add Application";
  private static final String APPLICATIONS_GRID = "[1]";

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
  void addApplicationSavesAndAppearsInApplicationsGridWithExpectedFieldValues() {
    fixture.navigateToSite(IADC_SITE_URL);
    fixture.clickOnSitePage(SETTINGS_PAGE);

    assertTrue(fixture.verifyActionIsPresent(ADD_APPLICATION_ACTION),
        "'" + ADD_APPLICATION_ACTION + "' action should be present on the Settings tab");

    fixture.clickOnAction(ADD_APPLICATION_ACTION);

    String applicationName = "Automated Test App " + fixture.getRandomAlphabetString(8);
    String acronym = fixture.getRandomAlphabetString(4);
    String uuidValue = "uuid-" + fixture.getRandomAlphabetString(12);
    String environmentKey = "env" + fixture.getRandomAlphabetString(6);
    String adminGroupId = String.valueOf(fixture.getRandomIntegerFromTo(1000, 9999));

    fixture.populateFieldWithValue("Application Name", applicationName);
    fixture.populateFieldWithValue("Acronym", acronym);
    fixture.populateFieldWithValue("UUID", uuidValue);
    fixture.populateFieldWithValue("Environment Key", environmentKey);
    fixture.populateFieldWithValue("Admin Group ID", adminGroupId);
    fixture.clickOnButton("Submit");

    int row = locateRowByApplicationName(applicationName);
    assertTrue(row > 0,
        "Newly created application '" + applicationName + "' was not found in the Applications grid");

    assertEquals(applicationName, fixture.getGridColumnRowValue(APPLICATIONS_GRID, "Name", "[" + row + "]"));
    assertEquals(acronym, fixture.getGridColumnRowValue(APPLICATIONS_GRID, "Acronym", "[" + row + "]"));
    assertEquals(uuidValue, fixture.getGridColumnRowValue(APPLICATIONS_GRID, "UUID", "[" + row + "]"));
    assertEquals(environmentKey, fixture.getGridColumnRowValue(APPLICATIONS_GRID, "Env Key", "[" + row + "]"));
  }

  @Test
  void cancelAddApplicationDoesNotSaveApplication() {
    fixture.navigateToSite(IADC_SITE_URL);
    fixture.clickOnSitePage(SETTINGS_PAGE);
    fixture.clickOnAction(ADD_APPLICATION_ACTION);

    String applicationName = "Automated Test App " + fixture.getRandomAlphabetString(8);
    String acronym = fixture.getRandomAlphabetString(4);
    String uuidValue = "uuid-" + fixture.getRandomAlphabetString(12);

    fixture.populateFieldWithValue("Application Name", applicationName);
    fixture.populateFieldWithValue("Acronym", acronym);
    fixture.populateFieldWithValue("UUID", uuidValue);
    fixture.clickOnButton("Cancel");

    int row = locateRowByApplicationName(applicationName);
    assertEquals(-1, row,
        "Cancelled application '" + applicationName + "' should not have been saved into the Applications grid");
  }

  private static int locateRowByApplicationName(String targetName) {
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
