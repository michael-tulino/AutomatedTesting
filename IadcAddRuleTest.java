package autogen;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.appiancorp.ps.automatedtest.fixture.SitesFixture;

/**
 * IV-198: Add A New Rule to Isaac Sandbox - Ignyte Appian Developer Copilot
 */
public class IadcAddRuleTest {

  private static final String TEST_SITE_URL = "https://ignytedemo.appiancloud.com/suite";
  private static final String IADC_SITE_URL = "ignyte-appian-developer-copilo";
  private static final String TEST_USERNAME = "automated.tester";
  private static final String TEST_BROWSER = "CHROME";
  private static final String TEST_SITE_VERSION = "24.3";
  private static final String TEST_SITE_LOCALE = "en_US";
  private static final Integer TEST_TIMEOUT = 60;

  private static final String APPLICATION_NAME = "Isaac Sandbox";
  private static final String APPLICATIONS_GRID = "[1]";
  private static final String RULES_GRID = "[1]";

  private static SitesFixture fixture;

  @BeforeAll
  public static void setUp() {
    fixture = new SitesFixture();
    fixture.setTakeErrorScreenshotsTo(true);
    fixture.setupWithBrowser(TEST_BROWSER);
    fixture.setAppianUrlTo(TEST_SITE_URL);
    fixture.setTimeoutSecondsTo(TEST_TIMEOUT);
    fixture.setAppianVersionTo(TEST_SITE_VERSION);
    fixture.setAppianLocaleTo(TEST_SITE_LOCALE);
    fixture.loginWithUsername(TEST_USERNAME);
    fixture.navigateToSite(IADC_SITE_URL);
  }

  @AfterAll
  public static void tearDown() {
    fixture.tearDown();
  }

  private void navigateToIsaacSandboxSummary() {
    fixture.clickOnSitePage("Settings");
    String row = findRowByColumnValue(APPLICATIONS_GRID, "Name", APPLICATION_NAME);
    assertNotNull(row, "Could not locate application \"" + APPLICATION_NAME + "\" in the Applications grid");
    fixture.clickOnGridColumnRow(APPLICATIONS_GRID, "Name", row);
  }

  private String findRowByColumnValue(String gridName, String columnName, String targetValue) {
    fixture.clickOnGridNavigation(gridName, "first");
    int totalCount = fixture.getGridTotalCount(gridName);
    int checked = 0;
    while (checked < totalCount) {
      int rowsOnPage = fixture.getGridRowCount(gridName);
      for (int row = 1; row <= rowsOnPage; row++) {
        String value = fixture.getGridColumnRowValue(gridName, columnName, String.valueOf(row));
        if (targetValue.equals(value)) {
          return String.valueOf(row);
        }
      }
      checked += rowsOnPage;
      if (checked < totalCount) {
        fixture.clickOnGridNavigation(gridName, "next");
      }
    }
    return null;
  }

  @Test
  public void testAddRuleFormFieldsAndSubmitAddsRuleToRulesGrid() {
    navigateToIsaacSandboxSummary();

    assertTrue(fixture.verifyActionIsPresent("Add Rule"),
        "\"Add Rule\" action should be present on the Isaac Sandbox summary tab");
    fixture.clickOnAction("Add Rule");

    assertTrue(fixture.verifyFieldIsPresent("Name"));
    assertTrue(fixture.verifyFieldIsPresent("Application"));
    assertTrue(fixture.verifyFieldIsPresent("Object Type"));
    assertTrue(fixture.verifyFieldIsPresent("Object Attribute"));
    assertTrue(fixture.verifyFieldIsPresent("Test Type"));
    assertTrue(fixture.verifyFieldIsPresent("Test Value"));
    assertTrue(fixture.verifyFieldIsPresent("Description"));
    assertTrue(fixture.verifyButtonIsPresent("Cancel"));
    assertTrue(fixture.verifyButtonIsPresent("Submit"));

    String ruleName = "IV198 Add Rule Test " + fixture.getRandomAlphabetString(6);

    fixture.populateFieldWith("Name", new String[] {ruleName});
    fixture.populateFieldWith("Object Type", new String[] {"Expression Rule"});
    fixture.populateFieldWith("Object Attribute", new String[] {"Name"});
    fixture.populateFieldWith("Test Type", new String[] {"Not Empty"});
    fixture.populateFieldWith("Test Value", new String[] {"TestValue123"});
    fixture.populateFieldWith("Description", new String[] {"Automated test rule created by IV-198 Selenium test."});

    fixture.clickOnButton("Submit");

    String row = findRowByColumnValue(RULES_GRID, "Name", ruleName);
    assertNotNull(row, "Newly added rule \"" + ruleName + "\" should appear in the Rules grid");
    assertTrue(fixture.verifyGridColumnRowContains(RULES_GRID, "Application", row, new String[] {APPLICATION_NAME}));
    assertTrue(fixture.verifyGridColumnRowContains(RULES_GRID, "Object Type", row, new String[] {"Expression Rule"}));
    assertTrue(fixture.verifyGridColumnRowContains(RULES_GRID, "Attribute", row, new String[] {"Name"}));
    assertTrue(fixture.verifyGridColumnRowContains(RULES_GRID, "Operator", row, new String[] {"Not Empty"}));
    assertTrue(fixture.verifyGridColumnRowContains(RULES_GRID, "Operand", row, new String[] {"TestValue123"}));
    assertTrue(fixture.verifyGridColumnRowContains(RULES_GRID, "Status", row, new String[] {"Active"}));

    // Deactivate the rule this test created so Isaac Sandbox doesn't accumulate test data.
    fixture.clickOnRecordActionFieldMenuAction(row, "Deactivate Test");
  }

  @Test
  public void testCancelOnAddRuleFormDoesNotSaveRule() {
    navigateToIsaacSandboxSummary();

    fixture.clickOnAction("Add Rule");

    String ruleName = "IV198 Cancel Test " + fixture.getRandomAlphabetString(6);
    fixture.populateFieldWith("Name", new String[] {ruleName});

    fixture.clickOnButton("Cancel");

    String row = findRowByColumnValue(RULES_GRID, "Name", ruleName);
    assertNull(row, "Rule \"" + ruleName + "\" should not have been saved after clicking Cancel");
  }
}
