import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.appiancorp.ps.automatedtest.fixture.SitesFixture;

/**
 * Selenium test for Jira IV-198: "Add A New Rule to Isaac Sandbox - Ignyte Appian Developer Copilot".
 *
 * Application under test: Ignyte Appian Developer Copilot (IADC), Isaac Sandbox application.
 *
 * Business Description:
 * As a developer using the IADC tool for my Isaac Sandbox application, I want to add a new rule.
 *
 * Acceptance Criteria (restated as discrete test cases below):
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
 * Interface sources (via listInterfaces / getInterface on the "Ignyte Appian Developer Copilot" application):
 *   - IADC_uiFormCreateUpdateObjectTest / IADC_uiComponentObjectTest ("Add Test Rule" dialog fields)
 *   - IADC_uiViewApplicationSummary (Application Summary page, "Add Rule" quick action)
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
  private static final String ADD_RULE_RELATED_ACTION = "Add Rule";

  // "Add Test Rule" dialog field labels (from getInterface: IADC_uiComponentObjectTest)
  private static final String FIELD_NAME = "Name";
  private static final String FIELD_APPLICATION = "Application";
  private static final String FIELD_OBJECT_TYPE = "Object Type";
  private static final String FIELD_OBJECT_ATTRIBUTE = "Object Attribute";
  private static final String FIELD_TEST_TYPE = "Test Type";
  private static final String FIELD_TEST_VALUE = "Test Value";
  private static final String FIELD_DESCRIPTION = "Description";

  private static final String BUTTON_SUBMIT = "Submit";

  // Rules grid ("Rules" section on the Application Summary page) column labels (from getInterface: IADC_uiGridObjectTests)
  private static final String RULES_GRID = "[1]";
  private static final String COLUMN_NAME = "Name";
  private static final String COLUMN_APPLICATION = "Application";
  private static final String COLUMN_OBJECT_TYPE = "Object Type";
  private static final String COLUMN_ATTRIBUTE = "Attribute";
  private static final String COLUMN_OPERATOR = "Operator";
  private static final String COLUMN_OPERAND = "Operand";

  // Test rule values (from Jira acceptance criteria, confirmed against IADC Object Test Type /
  // IADC Object Attribute lookup data: objectTypeId 7 = "Expression Rule", objectAttributeId 107 =
  // "Unit Test Count" for that type, objectTestTypeId 19 label = "> (Greater Than)")
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

    openAddTestRuleDialogForIsaacSandbox();

    testCase1_NameIsSetOnTheRule();
    testCase2_ApplicationIsSetToIsaacSandbox();
    testCase3_ObjectTypeIsSetToExpressionRule();
    testCase4_ObjectAttributeIsSetToUnitTestCount();
    testCase5_TestTypeIsSetToGreaterThan();
    testCase6_TestValueIsSetToZero();
    testCase7_DescriptionIsSetOnTheRule();

    submitTheRule();

    testCase8_RuleSavesWithoutValidationErrorAndAppearsInTheRulesList();

    fixture.tearDown();
  }

  /**
   * Precondition for every test case below: navigate to the Isaac Sandbox application's
   * Summary page inside the Ignyte Appian Developer Copilot site and open the "Add Test Rule"
   * dialog via the "Add Rule" quick action (record action f5d616ae-0dfd-4bd4-9f4d-dc44b49456a9
   * on the IADC Application record type).
   */
  private static void openAddTestRuleDialogForIsaacSandbox() {
    LOG.debug("Navigating to Isaac Sandbox application summary");
    fixture.navigateToSite(IADC_SITE_URL);
    fixture.clickOnSitePage(IADC_SETTINGS_PAGE);
    fixture.clickOnLink(ISAAC_SANDBOX_APPLICATION_NAME);
    fixture.waitForWorking();

    LOG.debug("Opening the Add Test Rule dialog");
    fixture.clickOnRecordRelatedAction(ADD_RULE_RELATED_ACTION);
    fixture.waitForWorking();
    fixture.verifyButtonIsPresent(BUTTON_SUBMIT);
  }

  /**
   * Test Case 1: Given the "Add Test Rule" dialog is open, when I populate Name with
   * "Expression Rules Have 1+ Automated Tests", then the Name field holds that value.
   */
  private static void testCase1_NameIsSetOnTheRule() {
    fixture.populateFieldWith(FIELD_NAME, new String[] {RULE_NAME});
    fixture.verifyFieldContainsValue(FIELD_NAME, RULE_NAME);
  }

  /**
   * Test Case 2: Given the "Add Test Rule" dialog is open, when I set Application to
   * Isaac Sandbox, then the Application field holds that value.
   */
  private static void testCase2_ApplicationIsSetToIsaacSandbox() {
    fixture.populateFieldWith(FIELD_APPLICATION, new String[] {ISAAC_SANDBOX_APPLICATION_NAME});
    fixture.verifyFieldContainsValue(FIELD_APPLICATION, ISAAC_SANDBOX_APPLICATION_NAME);
  }

  /**
   * Test Case 3: Given the "Add Test Rule" dialog is open, when I set Object Type to
   * Expression Rule, then the Object Type field holds that value.
   */
  private static void testCase3_ObjectTypeIsSetToExpressionRule() {
    fixture.populateFieldWith(FIELD_OBJECT_TYPE, new String[] {RULE_OBJECT_TYPE});
    fixture.verifyFieldContainsValue(FIELD_OBJECT_TYPE, RULE_OBJECT_TYPE);
  }

  /**
   * Test Case 4: Given Object Type is set to Expression Rule, when I set Object Attribute to
   * Unit Test Count, then the Object Attribute field holds that value.
   * Note: Object Attribute must be populated after Object Type, since its choices are filtered
   * by the selected Object Type and the field is disabled until Object Type has a value.
   */
  private static void testCase4_ObjectAttributeIsSetToUnitTestCount() {
    fixture.populateFieldWith(FIELD_OBJECT_ATTRIBUTE, new String[] {RULE_OBJECT_ATTRIBUTE});
    fixture.verifyFieldContainsValue(FIELD_OBJECT_ATTRIBUTE, RULE_OBJECT_ATTRIBUTE);
  }

  /**
   * Test Case 5: Given the "Add Test Rule" dialog is open, when I set Test Type to
   * "> (Greater Than)", then the Test Type field holds that value.
   */
  private static void testCase5_TestTypeIsSetToGreaterThan() {
    fixture.populateFieldWith(FIELD_TEST_TYPE, new String[] {RULE_TEST_TYPE});
    fixture.verifyFieldContainsValue(FIELD_TEST_TYPE, RULE_TEST_TYPE);
  }

  /**
   * Test Case 6: Given the "Add Test Rule" dialog is open, when I set Test Value to "0",
   * then the Test Value field holds that value.
   */
  private static void testCase6_TestValueIsSetToZero() {
    fixture.populateFieldWith(FIELD_TEST_VALUE, new String[] {RULE_TEST_VALUE});
    fixture.verifyFieldContainsValue(FIELD_TEST_VALUE, RULE_TEST_VALUE);
  }

  /**
   * Test Case 7: Given the "Add Test Rule" dialog is open, when I populate Description with
   * "Expression Rules Have 1+ Automated Tests", then the Description field holds that value.
   */
  private static void testCase7_DescriptionIsSetOnTheRule() {
    fixture.populateFieldWith(FIELD_DESCRIPTION, new String[] {RULE_DESCRIPTION});
    fixture.verifyFieldContainsValue(FIELD_DESCRIPTION, RULE_DESCRIPTION);
  }

  private static void submitTheRule() {
    LOG.debug("Submitting the new test rule");
    fixture.clickOnButton(BUTTON_SUBMIT);
    fixture.waitForWorking();
  }

  /**
   * Test Case 8: Given all rule fields were populated and Submit was clicked, then the dialog
   * closes without a validation error and the new rule appears in the Rules list for the
   * Isaac Sandbox application with the values entered above.
   */
  private static void testCase8_RuleSavesWithoutValidationErrorAndAppearsInTheRulesList() {
    if (fixture.errorIsPresent()) {
      throw new AssertionError("Submitting the Add Test Rule dialog produced an unexpected error.");
    }
    if (fixture.verifyButtonIsPresent(BUTTON_SUBMIT)) {
      throw new AssertionError("Add Test Rule dialog is still open; a validation error likely blocked submission.");
    }

    int rowNum = findGridRowByColumnValue(RULES_GRID, COLUMN_NAME, RULE_NAME);
    if (rowNum == -1) {
      throw new AssertionError("New rule \"" + RULE_NAME + "\" was not found in the Rules list for Isaac Sandbox.");
    }

    fixture.verifyGridColumnRowContainsValue(RULES_GRID, COLUMN_NAME, String.valueOf(rowNum), RULE_NAME);
    fixture.verifyGridColumnRowContainsValue(RULES_GRID, COLUMN_APPLICATION, String.valueOf(rowNum), ISAAC_SANDBOX_APPLICATION_NAME);
    fixture.verifyGridColumnRowContainsValue(RULES_GRID, COLUMN_OBJECT_TYPE, String.valueOf(rowNum), RULE_OBJECT_TYPE);
    fixture.verifyGridColumnRowContainsValue(RULES_GRID, COLUMN_ATTRIBUTE, String.valueOf(rowNum), RULE_OBJECT_ATTRIBUTE);
    fixture.verifyGridColumnRowContainsValue(RULES_GRID, COLUMN_OPERATOR, String.valueOf(rowNum), RULE_TEST_TYPE);
    fixture.verifyGridColumnRowContainsValue(RULES_GRID, COLUMN_OPERAND, String.valueOf(rowNum), RULE_TEST_VALUE);
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
