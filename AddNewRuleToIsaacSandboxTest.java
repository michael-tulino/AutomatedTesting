import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.appiancorp.ps.automatedtest.fixture.SitesFixture;

/**
 * Selenium test for Jira IV-198: "Add A New Rule to Isaac Sandbox - Ignyte Appian Developer Copilot".
 *
 * Acceptance criteria (Given the "Add Test Rule" dialog is open, when I create the rule, then the
 * saved rule has):
 *   1. Name = "Expression Rules Have 1+ Automated Tests"
 *   2. Application = Isaac Sandbox
 *   3. Object Type = Expression Rule
 *   4. Object Attribute = Unit Test Count
 *   5. Test Type = > (Greater Than)
 *   6. Test Value = 0
 *   7. Description = "Expression Rules Have 1+ Automated Tests"
 *   8. On Submit, the rule saves without validation error and appears in the rules list for the
 *      Isaac Sandbox application.
 *
 * "Isaac Sandbox" is a data value (an IADC Application record) selected inside the rule dialog, not
 * a separate UI host - the "Add Test Rule" dialog and rules list live in the "Ignyte Appian Developer
 * Copilot" (IADC) application (listInterfaces scoped to the "Isaac Sandbox" application itself
 * returned only unrelated Car/Tree demo interfaces), so all interface/record lookups were made
 * against IADC (uuid 3ff6eaa8-5978-428c-b8cd-95150e082a32).
 *
 * SAIL trace (IADC_uiFormCreateUpdateObjectTest / IADC_uiComponentObjectTest / IADC_uiGridObjectTests):
 *   - All seven dialog fields are unconditionally editable (readOnly is hardcoded to false), except
 *     the Object Attribute dropdown, which is disabled until Object Type has a value and whose
 *     choices are filtered by Object Type - so Object Type must be populated before Object Attribute.
 *   - Every asserted grid column (Name, Application, Object Type, Attribute, Operand) is a direct
 *     property() reference to the record's own field or a directly-related record's name field, so
 *     the rendered text is the raw stored value. "Operator" renders objectTestType.label, the same
 *     field the Test Type dropdown uses for its choiceLabels, so the picked label
 *     ("> (Greater Than)", confirmed verbatim in the IADC Object Test Type record data) is exactly
 *     what the grid renders.
 *   - "Object Type" = "Expression Rule" (IADC Object Type id 7) and "Object Attribute" =
 *     "Unit Test Count" (IADC Object Attribute id 107, scoped to objectTypeId 7) were confirmed as
 *     exact record values via listRecordData, not assumed from the Jira text.
 *   - On Submit, IADC_uiFormCreateUpdateObjectTest unconditionally sets the new rule's statusId to
 *     Active, and the Rules grid filters to isActive=true, so the created rule is visible without
 *     changing any grid filter.
 *   - The Rules grid (IADC_uiGridObjectTests) is a native paging record grid with no default sort and
 *     no search box, and may already contain other rows from real or prior test data, so the created
 *     rule is located by paging through and matching on its unique Name rather than by row count or
 *     assumed position.
 *   - Description has no grid column, so it is verified by reopening the persisted rule via its
 *     "Update Test" row action, which reloads the record from IADC_qrtObjectTest - an independent
 *     boundary, not an echo of what was just typed.
 *   - The Rules grid has refreshAfter="RECORD_ACTION", so no manual refresh is needed after Submit.
 *   - "Deactivate Test" (used for cleanup) is a fully unattended process (Update Status -> Write
 *     Object Test -> End; confirmed via listProcessModelNodes - no user input node), so clicking the
 *     row action executes immediately with no confirmation dialog to dismiss.
 *
 * All eight criteria describe a single persisted record produced by one Submit, so they are verified
 * together in one test case.
 */
public class AddNewRuleToIsaacSandboxTest {

  protected static String TEST_BROWSER = "CHROME";
  protected static String TEST_SITE_VERSION = "24.3";
  protected static String TEST_SITE_URL = "https://ignytedemo.appiancloud.com/suite";
  protected static String TEST_SITE_LOCALE = "en_US";
  protected static String TEST_USERNAME = "automated.tester"; //Make sure this username has an entry in users.properties
  protected static Integer TEST_TIMEOUT = 60;

  // Site navigation (from listSites, scoped to the IADC application)
  private static final String IADC_SITE_URL = "ignyte-appian-developer-copilo";
  private static final String IADC_SETTINGS_PAGE = "Settings";
  private static final String ISAAC_SANDBOX_APPLICATION_NAME = "Isaac Sandbox";

  // Related actions (from listRecordTypeActions on IADC Application and IADC Object Test)
  private static final String ACTION_ADD_RULE = "Add Rule";
  private static final String ACTION_UPDATE_TEST = "Update Test";
  private static final String ACTION_DEACTIVATE_TEST = "Deactivate Test";

  // "Add Test Rule" dialog field labels (from getInterface: IADC_uiComponentObjectTest)
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
  // (from getInterface: IADC_uiGridObjectTests). The grid has no title, so it is referenced by
  // position - it is the only grid on the Application Summary page.
  private static final String RULES_GRID = "[1]";
  private static final String COLUMN_NAME = "Name";
  private static final String COLUMN_APPLICATION = "Application";
  private static final String COLUMN_OBJECT_TYPE = "Object Type";
  private static final String COLUMN_ATTRIBUTE = "Attribute";
  private static final String COLUMN_OPERATOR = "Operator";
  private static final String COLUMN_OPERAND = "Operand";

  // Test rule values (from Jira acceptance criteria; Object Type/Object Attribute/Test Type text
  // confirmed verbatim against the IADC Object Type / IADC Object Attribute / IADC Object Test Type
  // record data via listRecordData)
  private static final String RULE_NAME = "Expression Rules Have 1+ Automated Tests";
  private static final String RULE_OBJECT_TYPE = "Expression Rule";
  private static final String RULE_OBJECT_ATTRIBUTE = "Unit Test Count";
  private static final String RULE_TEST_TYPE = "> (Greater Than)";
  private static final String RULE_TEST_VALUE = "0";
  private static final String RULE_DESCRIPTION = "Expression Rules Have 1+ Automated Tests";

  private static final Logger LOG = LogManager.getLogger(AddNewRuleToIsaacSandboxTest.class);
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
   * Test Case 1 (covers Acceptance Criteria 1-8): Given the "Add Test Rule" dialog is open for the
   * Isaac Sandbox application, when I populate Name, Application, Object Type, Object Attribute,
   * Test Type, Test Value, and Description and submit, then the dialog closes without a validation
   * error, the new rule appears in the Rules list for Isaac Sandbox with the specified fields, and
   * reopening the persisted rule shows the specified Description. Cleanup deactivates the created
   * rule.
   */
  private static void testCase1_RuleIsCreatedAndPersistedWithAllSpecifiedFields() {
    openAddTestRuleDialogForIsaacSandbox();

    // Object Type must be populated before Object Attribute: the Object Attribute dropdown is
    // disabled until Object Type has a value, and its choices are filtered by Object Type.
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

    // AC 8: rule appears in the Rules list for Isaac Sandbox. The grid refreshes automatically after
    // the related action (refreshAfter="RECORD_ACTION"). The grid may already contain other active
    // rules and has no default sort, so the created row is located by its unique Name rather than by
    // row count or position, paging through all rows if necessary.
    String newRow = findGridRowByColumnValue(RULES_GRID, COLUMN_NAME, RULE_NAME);

    // AC 1-6: field values on the persisted rule, verified from the reloaded Rules grid
    fixture.verifyGridColumnRowContainsValue(RULES_GRID, COLUMN_NAME, newRow, RULE_NAME);
    fixture.verifyGridColumnRowContainsValue(RULES_GRID, COLUMN_APPLICATION, newRow, ISAAC_SANDBOX_APPLICATION_NAME);
    fixture.verifyGridColumnRowContainsValue(RULES_GRID, COLUMN_OBJECT_TYPE, newRow, RULE_OBJECT_TYPE);
    fixture.verifyGridColumnRowContainsValue(RULES_GRID, COLUMN_ATTRIBUTE, newRow, RULE_OBJECT_ATTRIBUTE);
    fixture.verifyGridColumnRowContainsValue(RULES_GRID, COLUMN_OPERATOR, newRow, RULE_TEST_TYPE);
    fixture.verifyGridColumnRowContainsValue(RULES_GRID, COLUMN_OPERAND, newRow, RULE_TEST_VALUE);

    // AC 7: Description has no grid column, so verify it by reopening the persisted rule
    LOG.debug("Reopening the persisted rule to verify Description");
    fixture.clickOnRecordActionFieldMenuAction(newRow, ACTION_UPDATE_TEST);
    fixture.waitForWorking();
    fixture.verifyFieldContainsValue(FIELD_DESCRIPTION, RULE_DESCRIPTION);
    fixture.clickOnButton(BUTTON_CANCEL);
    fixture.waitForWorking();

    // Test cleanup: deactivate the rule created above so it does not affect future runs.
    // "Deactivate Test" is a fully unattended process (no form), so clicking the row action executes
    // immediately with no confirmation dialog to dismiss.
    LOG.debug("Cleanup: deactivating the rule created by this test");
    fixture.clickOnRecordActionFieldMenuAction(newRow, ACTION_DEACTIVATE_TEST);
    fixture.waitForWorking();
  }

  /**
   * Precondition for the test case above: navigate to the Isaac Sandbox application's Summary page
   * inside the Ignyte Appian Developer Copilot site and open the "Add Test Rule" dialog via the
   * "Add Rule" quick action on the IADC Application record type.
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
   * Locates the row in a paging grid whose value in columnName exactly matches targetValue, paging
   * forward through subsequent pages if necessary rather than assuming the target is on the current
   * page or at a particular position. The grid may already contain other rows from real or prior
   * test data, so this never relies on row/item counts to find the created record.
   *
   * Assumes the grid is currently showing its first page (true immediately after the navigation in
   * openAddTestRuleDialogForIsaacSandbox() and the subsequent auto-refresh on Submit), so it never
   * clicks a "first" navigation control that may not be rendered when the grid has only one page.
   *
   * @return the matching row reference (e.g. "[3]") on whichever page it was found on; the grid is
   *     left on that page so callers can act on the row immediately.
   */
  private static String findGridRowByColumnValue(String gridName, String columnName, String targetValue) {
    int totalCount = fixture.getGridTotalCount(gridName);
    int rowsScanned = 0;
    while (true) {
      int rowsOnPage = fixture.getGridRowCount(gridName);
      for (int row = 1; row <= rowsOnPage; row++) {
        String rowRef = "[" + row + "]";
        if (targetValue.equals(fixture.getGridColumnRowValue(gridName, columnName, rowRef))) {
          return rowRef;
        }
      }
      rowsScanned += rowsOnPage;
      if (rowsScanned >= totalCount) {
        throw new AssertionError("Could not locate a row in grid " + gridName + " where column \""
            + columnName + "\" = \"" + targetValue + "\" across " + totalCount + " total row(s).");
      }
      fixture.clickOnGridNavigation(gridName, "next");
    }
  }
}
