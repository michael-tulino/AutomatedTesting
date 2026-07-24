import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.appiancorp.ps.automatedtest.fixture.SitesFixture;

public class AddRuleToIsaacSandboxTest {

  private static final String TEST_SITE_URL = "https://ignytedemo.appiancloud.com/suite";
  private static final String IADC_SITE_URL = "ignyte-appian-developer-copilo";
  private static final String TEST_USERNAME = "automated.tester";
  private static final String TEST_BROWSER = "CHROME";
  private static final String TEST_SITE_VERSION = "24.3";
  private static final String TEST_SITE_LOCALE = "en_US";
  private static final Integer TEST_TIMEOUT = 60;

  private static final String APPLICATION_NAME = "Isaac Sandbox";
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
  }

  @AfterAll
  public static void tearDown() {
    fixture.tearDown();
  }

  private void navigateToApplicationSummary() {
    fixture.navigateToSite(IADC_SITE_URL);
    fixture.clickOnSitePage("Settings");
    fixture.clickOnRecord(APPLICATION_NAME);
  }

  /**
   * Pages through the Rules grid on the application summary tab looking for a row whose
   * Name column matches ruleName. Returns the 1-based row index it was found on, or -1 if
   * it isn't found after every page has been checked.
   */
  private int findRuleRowByName(String ruleName) {
    int totalCount = fixture.getGridTotalCount(RULES_GRID);
    int rowsChecked = 0;

    while (rowsChecked < totalCount) {
      int rowCount = fixture.getGridRowCount(RULES_GRID);
      for (int row = 1; row <= rowCount; row++) {
        if (ruleName.equals(fixture.getGridColumnRowValue(RULES_GRID, "Name", String.valueOf(row)))) {
          return row;
        }
      }
      rowsChecked += rowCount;
      if (rowsChecked < totalCount) {
        fixture.clickOnGridNavigation(RULES_GRID, "next");
      }
    }
    return -1;
  }

  @Test
  public void testAddRuleActionLaunchesFormWithExpectedFieldsAndButtons() {
    navigateToApplicationSummary();

    assertTrue(fixture.verifyRecordRelatedActionIsPresent("Add Rule"),
        "Expected an 'Add Rule' action to be present on the application summary tab");

    fixture.clickOnRecordRelatedAction("Add Rule");

    assertTrue(fixture.verifyFieldIsPresent("Name"), "Expected a 'Name' field on the Add Rule form");
    assertTrue(fixture.verifyFieldIsPresent("Application"), "Expected an 'Application' field on the Add Rule form");
    assertTrue(fixture.verifyFieldIsPresent("Object Type"), "Expected an 'Object Type' field on the Add Rule form");
    assertTrue(fixture.verifyFieldIsPresent("Object Attribute"), "Expected an 'Object Attribute' field on the Add Rule form");
    assertTrue(fixture.verifyFieldIsPresent("Test Type"), "Expected a 'Test Type' field on the Add Rule form");
    assertTrue(fixture.verifyFieldIsPresent("Test Value"), "Expected a 'Test Value' field on the Add Rule form");
    assertTrue(fixture.verifyFieldIsPresent("Description"), "Expected a 'Description' field on the Add Rule form");

    assertTrue(fixture.verifyButtonIsPresent("Cancel"), "Expected a 'Cancel' button on the Add Rule form");
    assertTrue(fixture.verifyButtonIsPresent("Submit"), "Expected a 'Submit' button on the Add Rule form");

    fixture.clickOnButton("Cancel");
  }

  @Test
  public void testCancelSubmitsWithoutSavingRule() {
    String ruleName = "IV-198 Cancel Test Rule " + fixture.getRandomAlphabetString(8);

    navigateToApplicationSummary();
    fixture.clickOnRecordRelatedAction("Add Rule");

    fixture.populateFieldWithValue("Name", ruleName);
    fixture.populateFieldWithValue("Object Type", "Constant");
    fixture.populateFieldWithValue("Object Attribute", "Name");
    fixture.populateFieldWithValue("Test Type", "Contains");
    fixture.populateFieldWithValue("Test Value", fixture.getRandomAlphabetString(6));

    fixture.clickOnButton("Cancel");

    assertTrue(fixture.verifyRecordRelatedActionIsPresent("Add Rule"),
        "Expected to be back on the application summary tab after Cancel");
    assertEquals(-1, findRuleRowByName(ruleName), "Cancel should not save the rule into the Rules grid");
  }

  @Test
  public void testSubmitSavesRuleAndItAppearsInRulesGrid() {
    String ruleName = "IV-198 Submit Test Rule " + fixture.getRandomAlphabetString(8);
    String testValue = fixture.getRandomAlphabetString(6);

    navigateToApplicationSummary();
    fixture.clickOnRecordRelatedAction("Add Rule");

    fixture.populateFieldWithValue("Name", ruleName);
    fixture.populateFieldWithValue("Object Type", "Constant");
    fixture.populateFieldWithValue("Object Attribute", "Name");
    fixture.populateFieldWithValue("Test Type", "Contains");
    fixture.populateFieldWithValue("Test Value", testValue);
    fixture.populateFieldWith("PARAGRAPH", "Description", new String[] { "IV-198 automated test rule" });

    fixture.clickOnButton("Submit");

    int rowIndex = findRuleRowByName(ruleName);
    assertTrue(rowIndex > 0, "Expected the newly created rule to show up in the Rules grid");

    String row = String.valueOf(rowIndex);
    assertTrue(fixture.verifyGridColumnRowContainsValue(RULES_GRID, "Application", row, APPLICATION_NAME));
    assertTrue(fixture.verifyGridColumnRowContainsValue(RULES_GRID, "Object Type", row, "Constant"));
    assertTrue(fixture.verifyGridColumnRowContainsValue(RULES_GRID, "Attribute", row, "Name"));
    assertTrue(fixture.verifyGridColumnRowContainsValue(RULES_GRID, "Operator", row, "Contains"));
    assertTrue(fixture.verifyGridColumnRowContainsValue(RULES_GRID, "Operand", row, testValue));
  }
}
