import com.appiancorp.ps.automatedtest.fixture.SitesFixture;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*
 * IV-198: Add A New Rule to Isaac Sandbox - Ignyte Appian Developer Copilot
 *
 * Business Description:
 * As a developer using the IADC tool for my Isaac Sandbox application, I want to add a new rule.
 *
 * Acceptance Criteria:
 * Given the "Add Test Rule" dialog is open, when I create the rule, then the saved rule has:
 *  1. Name = "Expression Rules Have 1+ Automated Tests"
 *  2. Application = Isaac Sandbox
 *  3. Object Type = Expression Rule
 *  4. Object Attribute = Unit Test Count
 *  5. Test Type = > (Greater Than)
 *  6. Test Value = 0
 *  7. Description = "Expression Rules Have 1+ Automated Tests"
 *  8. On Submit, the rule saves without validation error and appears in the rules list
 *     for the Isaac Sandbox application.
 *
 * All eight criteria describe one persisted record produced by a single Submit, so they are
 * verified together in one test case: fields 1-6 and the row's Active status are checked
 * against the refreshed Rules grid on the Isaac Sandbox application record, and the
 * Description (not shown as a grid column) is checked by reopening the saved rule through
 * its "Update Test" action - both are independent reload boundaries, not the values just typed.
 */
class IV198Test {

  protected static String TEST_BROWSER = "CHROME";
  protected static String TEST_SITE_VERSION = "24.3";
  protected static String TEST_SITE_URL = "https://yourAppianSite.com";
  protected static String TEST_SITE_LOCALE = "en_US";
  protected static String TEST_USERNAME = "admin.user"; //Make sure this username has an entry in users.properties
  protected static Integer TEST_TIMEOUT = 60;

  private static final String RECORD_TYPE_NAME = "IADC Application";
  private static final String APPLICATION_RECORD_NAME = "Isaac Sandbox";

  private static final String ADD_RULE_ACTION = "Add Rule";
  private static final String UPDATE_TEST_ACTION = "Update Test";
  private static final String DEACTIVATE_TEST_ACTION = "Deactivate Test";

  private static final String RULE_NAME = "Expression Rules Have 1+ Automated Tests";
  private static final String RULE_APPLICATION = "Isaac Sandbox";
  private static final String RULE_OBJECT_TYPE = "Expression Rule";
  private static final String RULE_OBJECT_ATTRIBUTE = "Unit Test Count";
  private static final String RULE_TEST_TYPE = "> (Greater Than)";
  private static final String RULE_TEST_VALUE = "0";
  private static final String RULE_DESCRIPTION = "Expression Rules Have 1+ Automated Tests";
  private static final String RULE_STATUS_ACTIVE = "Active";

  private static final String RULES_GRID = "[1]";

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
    fixture.clickOnRecordType(RECORD_TYPE_NAME);
    fixture.clickOnRecord(APPLICATION_RECORD_NAME);
  }

  @Test
  void addTestRule_savesAllFieldsAndAppearsInRulesList() {
    int rowCountBefore = fixture.getGridRowCount(RULES_GRID);

    fixture.clickOnRecordRelatedAction(ADD_RULE_ACTION);

    fixture.verifyFieldIsPresent("Name");
    fixture.verifyFieldIsPresent("Application");
    fixture.verifyFieldIsPresent("Object Type");
    fixture.verifyFieldIsPresent("Test Type");
    fixture.verifyFieldIsPresent("Test Value");
    fixture.verifyFieldIsPresent("Description");

    fixture.populateFieldWith("Name", new String[] { RULE_NAME });
    fixture.populateFieldWith("Application", new String[] { RULE_APPLICATION });
    fixture.populateFieldWith("Object Type", new String[] { RULE_OBJECT_TYPE });
    fixture.populateFieldWith("Object Attribute", new String[] { RULE_OBJECT_ATTRIBUTE });
    fixture.populateFieldWith("Test Type", new String[] { RULE_TEST_TYPE });
    fixture.populateFieldWith("Test Value", new String[] { RULE_TEST_VALUE });
    fixture.populateFieldWith("Description", new String[] { RULE_DESCRIPTION });

    fixture.clickOnButton("Submit");

    assertFalse(fixture.errorIsPresent(), "Rule should save without a validation error");

    int rowCountAfter = fixture.getGridRowCount(RULES_GRID);
    assertEquals(rowCountBefore + 1, rowCountAfter,
        "A new row should appear in the Rules grid for the Isaac Sandbox application after saving");

    String newRow = "[" + rowCountAfter + "]";
    assertTrue(fixture.verifyGridColumnRowContainsValue(RULES_GRID, "Name", newRow, RULE_NAME));
    assertTrue(fixture.verifyGridColumnRowContainsValue(RULES_GRID, "Application", newRow, RULE_APPLICATION));
    assertTrue(fixture.verifyGridColumnRowContainsValue(RULES_GRID, "Object Type", newRow, RULE_OBJECT_TYPE));
    assertTrue(fixture.verifyGridColumnRowContainsValue(RULES_GRID, "Attribute", newRow, RULE_OBJECT_ATTRIBUTE));
    assertTrue(fixture.verifyGridColumnRowContainsValue(RULES_GRID, "Operator", newRow, RULE_TEST_TYPE));
    assertTrue(fixture.verifyGridColumnRowContainsValue(RULES_GRID, "Operand", newRow, RULE_TEST_VALUE));
    assertTrue(fixture.verifyGridColumnRowContainsValue(RULES_GRID, "Status", newRow, RULE_STATUS_ACTIVE));

    fixture.clickOnRecordActionFieldMenuAction(newRow, UPDATE_TEST_ACTION);
    assertEquals(RULE_DESCRIPTION, fixture.getFieldValue("Description"),
        "Description should persist on the reloaded, saved rule");
    fixture.clickOnButton("Cancel");

    // Test cleanup: deactivate the rule created above so it does not affect future runs.
    fixture.clickOnRecordActionFieldMenuAction(newRow, DEACTIVATE_TEST_ACTION);
  }

  @AfterAll
  static void tearDown() {
    fixture.tearDown();
  }
}
