import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.appiancorp.ps.automatedtest.fixture.SitesFixture;

/**
 * Selenium test for Jira IV-198: "Add A New Rule to Isaac Sandbox - Ignyte Appian Developer Copilot".
 *
 * Application under test: Ignyte Appian Developer Copilot (IADC), scoped to the Isaac Sandbox application.
 *
 * Business Description:
 * As a developer using the IADC tool for my Isaac Sandbox application, I want to add a new rule.
 *
 * Acceptance Criteria (restated):
 * Given the "Add Test Rule" dialog is open, when I create the rule, then the saved rule has:
 *   1. Name = "Expression Rules Have 1+ Automated Tests"
 *   2. Application = Isaac Sandbox
 *   3. Object Type = Expression Rule
 *   4. Object Attribute = Unit Test Count
 *   5. Test Type = > (Greater Than)
 *   6. Test Value = 0
 *   7. Description = "Expression Rules Have 1+ Automated Tests"
 *   8. On Submit, the rule saves without validation error and appears in the rules list
 *      for the Isaac Sandbox application.
 *
 * Criteria 1-8 all describe a single underlying outcome (one persisted rule, verified after
 * submission), so they are grouped into one test case below. Fields 1-6 are verified by reloading
 * the Rules grid (a fresh record query, independent of the values just typed); Description has no
 * grid column, so it is verified by reopening the persisted rule via its "Update Test" action,
 * which reloads the record from the record type.
 *
 * Interface sources (via listInterfaces / getInterface on the "Ignyte Appian Developer Copilot" application):
 *   - IADC_uiViewApplicationSummary (Application Summary page, "Add Rule" quick action)
 *   - IADC_uiFormCreateUpdateObjectTest / IADC_uiComponentObjectTest ("Add/Update Test Rule" dialog fields)
 *   - IADC_uiGridObjectTests (Rules grid columns: Name, Application, Object Type, Attribute, Operator, Operand, Status)
 *   - IADC_uiGridApplications (Applications grid on the Settings page)
 */
public class IsaacSandboxAddTestRuleTest {

  protected static String TEST_BROWSER = "CHROME";
  protected static String TEST_SITE_VERSION = "24.3";
  protected static String TEST_SITE_URL = "https://yourAppianSite.com";
  protected static String TEST_SITE_LOCALE = "en_US";
  protected static String TEST_USERNAME = "admin.user"; //Make sure this username has an entry in users.properties
  protected static Integer TEST_TIMEOUT = 60;

  // Site navigation (from listSites, scoped to the IADC application)
  private static final String IADC_SITE_URL = "ignyte-appian-developer-copilo";
  private static final String IADC_SETTINGS_PAGE = "Settings";
  private static final String ISAAC_SANDBOX_APPLICATION_NAME = "Isaac Sandbox";

  // Application Summary "Quick Actions" / Rules grid row actions (from listRecordTypeActions
  // on IADC Application and IADC Object Test)
  private static final String ACTION_ADD_RULE = "Add Rule";
  private static final String ACTION_UPDATE_TEST = "Update Test";

  // "Add/Update Test Rule" dialog field labels (from getInterface: IADC_uiComponentObjectTest)
  private static final String FIELD_NAME = "Name";
  private static final String FIELD_APPLICATION = "Application";
  private static final String FIELD_OBJECT_TYPE = "Object Type";
  private static final String FIELD_OBJECT_ATTRIBUTE = "Object Attribute";
  private static final String FIELD_TEST_TYPE = "Test Type";
  private static final String FIELD_TEST_VALUE = "Test Value";
  private static final String FIELD_DESCRIPTION = "Description";

  private static final String BUTTON_SUBMIT = "Submit";
  private static final String BUTTON_CANCEL = "Cancel";

  // Rules grid ("Rules" section on the Application Summary page) column labels
  // (from getInterface: IADC_uiGridObjectTests)
  private static final String RULES_GRID = "[1]";
  private static final String COLUMN_NAME = "Name";
  private static final String COLUMN_APPLICATION = "Application";
  private static final String COLUMN_OBJECT_TYPE = "Object Type";
  private static final String COLUMN_ATTRIBUTE = "Attribute";
  private static final String COLUMN_OPERATOR = "Operator";
  private static final String COLUMN_OPERAND = "Operand";

  // Test rule values (from Jira acceptance criteria)
  private static final String RULE_NAME = "Expression Rules Have 1+ Automated Tests";
  private static final String RULE_OBJECT_TYPE = "Expression Rule";
  private static final String RULE_OBJECT_ATTRIBUTE = "Unit Test Count";
  private static final String RULE_TEST_TYPE = "> (Greater Than)";
  private static final String RULE_TEST_VALUE = "0";
  private static final String RULE_DESCRIPTION = "Expression Rules Have 1+ Automated Tests";

  private static final Logger LOG = LogManager.getLogger(IsaacSandboxAddTestRuleTest.class);
  public static SitesFixture fixture;

  public static void main(String[] args) {
    fixture = new SitesFixture();
    fixture.setTakeErrorScreenshotsTo(true);
    fixture.setupWithBrowser(TEST_BROWSER);
    fixture.setAppianUrlTo(TEST_SITE_URL);
    fixture.setTimeoutSecondsTo(TEST_TIMEOUT);
    fixture.setAppianVersionTo(TEST_SITE_VERSION);
    fixture.setAppianLocaleTo(TEST_SITE_LOCALE);
    fixture.loginWithUsername(TEST_USERNAME);

    try {
      testCase1_RuleIsCreatedAndPersistedWithAllSpecifiedFields();
    } finally {
      fixture.tearDown();
    }
  }

  /**
   * Test Case 1 (covers Acceptance Criteria 1-8): Given the "Add Test Rule" dialog is open for
   * the Isaac Sandbox application, when I populate Name, Application, Object Type, Object
   * Attribute, Test Type, Test Value, and Description and submit, then the dialog closes without
   * a validation error, the new rule appears in the Rules list for Isaac Sandbox with the
   * specified Name/Application/Object Type/Object Attribute/Test Type/Test Value, and reopening
   * the persisted rule shows the specified Description.
   */
  private static void testCase1_RuleIsCreatedAndPersistedWithAllSpecifiedFields() {
    openAddTestRuleDialogForIsaacSandbox();

    fixture.populateFieldWith(FIELD_NAME, new String[] {RULE_NAME});
    fixture.populateFieldWith(FIELD_APPLICATION, new String[] {ISAAC_SANDBOX_APPLICATION_NAME});
    fixture.populateFieldWith(FIELD_OBJECT_TYPE, new String[] {RULE_OBJECT_TYPE});
    fixture.populateFieldWith(FIELD_OBJECT_ATTRIBUTE, new String[] {RULE_OBJECT_ATTRIBUTE});
    fixture.populateFieldWith(FIELD_TEST_TYPE, new String[] {RULE_TEST_TYPE});
    fixture.populateFieldWith(FIELD_TEST_VALUE, new String[] {RULE_TEST_VALUE});
    fixture.populateFieldWith(FIELD_DESCRIPTION, new String[] {RULE_DESCRIPTION});

    LOG.debug("Submitting the new test rule");
    fixture.clickOnButton(BUTTON_SUBMIT);
    fixture.waitForWorking();

    // AC 8: saves without validation error (dialog closes; Submit button is no longer present)
    fixture.verifyButtonIsNotPresent(BUTTON_SUBMIT);

    // AC 8: rule appears in the Rules list for Isaac Sandbox
    int rowNum = findGridRowByColumnValue(RULES_GRID, COLUMN_NAME, RULE_NAME);
    if (rowNum == -1) {
      throw new AssertionError("New rule \"" + RULE_NAME + "\" was not found in the Rules list for Isaac Sandbox.");
    }

    // AC 1-6: field values on the persisted rule, verified from the reloaded Rules grid
    fixture.verifyGridColumnRowContainsValue(RULES_GRID, COLUMN_NAME, String.valueOf(rowNum), RULE_NAME);
    fixture.verifyGridColumnRowContainsValue(RULES_GRID, COLUMN_APPLICATION, String.valueOf(rowNum), ISAAC_SANDBOX_APPLICATION_NAME);
    fixture.verifyGridColumnRowContainsValue(RULES_GRID, COLUMN_OBJECT_TYPE, String.valueOf(rowNum), RULE_OBJECT_TYPE);
    fixture.verifyGridColumnRowContainsValue(RULES_GRID, COLUMN_ATTRIBUTE, String.valueOf(rowNum), RULE_OBJECT_ATTRIBUTE);
    fixture.verifyGridColumnRowContainsValue(RULES_GRID, COLUMN_OPERATOR, String.valueOf(rowNum), RULE_TEST_TYPE);
    fixture.verifyGridColumnRowContainsValue(RULES_GRID, COLUMN_OPERAND, String.valueOf(rowNum), RULE_TEST_VALUE);

    // AC 7: Description has no grid column, so verify it by reopening the persisted rule
    LOG.debug("Reopening the persisted rule to verify Description");
    fixture.clickOnRecordActionFieldMenuAction(String.valueOf(rowNum), ACTION_UPDATE_TEST);
    fixture.waitForWorking();
    fixture.verifyFieldContainsValue(FIELD_DESCRIPTION, RULE_DESCRIPTION);
    fixture.clickOnButton(BUTTON_CANCEL);
  }

  /**
   * Precondition for the test case above: navigate to the Isaac Sandbox application's Summary
   * page inside the Ignyte Appian Developer Copilot site and open the "Add Test Rule" dialog via
   * the "Add Rule" quick action on the IADC Application record type.
   */
  private static void openAddTestRuleDialogForIsaacSandbox() {
    LOG.debug("Navigating to Isaac Sandbox application summary");
    fixture.navigateToSite(IADC_SITE_URL);
    fixture.clickOnSitePage(IADC_SETTINGS_PAGE);
    fixture.clickOnLink(ISAAC_SANDBOX_APPLICATION_NAME);
    fixture.waitForWorking();

    LOG.debug("Opening the Add Test Rule dialog");
    fixture.clickOnRecordRelatedAction(ACTION_ADD_RULE);
    fixture.waitForWorking();
    fixture.verifyButtonIsPresent(BUTTON_SUBMIT);
  }

  /**
   * Scans the rows of the given grid (assumed to fit on a single page) looking for a row whose
   * columnName cell matches value exactly. Returns the 1-based row number, or -1 if not found.
   */
  private static int findGridRowByColumnValue(String gridName, String columnName, String value) {
    int rowCount = fixture.countGridRows(gridName);
    for (int row = 1; row <= rowCount; row++) {
      String cellValue = fixture.getGridColumnRowValue(gridName, columnName, String.valueOf(row));
      if (value.equals(cellValue)) {
        return row;
      }
    }
    return -1;
  }
}
