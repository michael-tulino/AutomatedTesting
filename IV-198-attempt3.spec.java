import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.appiancorp.ps.automatedtest.fixture.SitesFixture;

/**
 * Selenium test for Jira IV-198: "Add A New Rule to Isaac Sandbox - Ignyte Appian Developer Copilot".
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
 * Application note: the ticket's business description names "Isaac Sandbox" as the application a
 * rule is being scoped to, but the "Add Test Rule" dialog and rules list themselves live in the
 * "Ignyte Appian Developer Copilot" (IADC) application (listApplications/listInterfaces on the
 * "Isaac Sandbox" application turned up only unrelated Car/Tree demo interfaces). Isaac Sandbox is
 * a data value (an IADC Application record, applicationId=1) selected within IADC's rule dialog,
 * not the UI host, so all interface/record lookups below were made against IADC.
 *
 * Interface sources (via listInterfaces / getInterface on the "Ignyte Appian Developer Copilot"
 * application, uuid 3ff6eaa8-5978-428c-b8cd-95150e082a32):
 *   - IADC_uiDashboardApplications / IADC_uiGridApplications (Settings page Applications grid;
 *     "Isaac Sandbox" is a record-link to the application's summary view)
 *   - IADC_uiViewApplicationSummary ("Add Rule" quick action; embeds IADC_uiGridObjectTests)
 *   - IADC_uiFormCreateUpdateObjectTest / IADC_uiComponentObjectTest ("Add Test Rule" dialog
 *     fields; dialog label reads "Add " + "Test Rule" when objectTestId is not supplied)
 *   - IADC_uiGridObjectTests (Rules grid columns: Name, Application, Object Type, Attribute,
 *     Operator, Operand, Status)
 *
 * SAIL trace notes informing the assertions/interactions below:
 *   - Every asserted grid column (Name, Application, Object Type, Attribute, Operand) is a direct
 *     property() reference to the record's own field or a directly-related record's name field
 *     (testName, application.applicationName, objectType.name, objectAttribute.attributeName,
 *     expectedValue) - the rendered text is the raw stored value, so asserting the exact input
 *     values is safe. The "Operator" column renders objectTestType.label (a relationship lookup,
 *     not a raw field), and the "Test Type" dropdown's choiceLabels are that same label field, so
 *     the label text picked in the dropdown ("> (Greater Than)", confirmed present verbatim in
 *     the IADC Object Test Type record data) is exactly what the grid will render.
 *   - "Object Type" = "Expression Rule" and "Object Attribute" = "Unit Test Count" were confirmed
 *     as exact record values (IADC Object Type id 7; IADC Object Attribute id 107, scoped to
 *     objectTypeId 7) rather than assumed from the Jira text.
 *   - The "Object Attribute" dropdown is disabled (IADC_uiComponentObjectTest) until "Object Type"
 *     has a value, and choosing "Object Type" clears any previously-selected Object Attribute - so
 *     the test must populate Object Type before Object Attribute.
 *   - The "Add Rule" related action (IADC Application record type) passes the current record's
 *     applicationId into the dialog, pre-selecting "Application" = Isaac Sandbox; the field is
 *     still populated explicitly below both to exercise it and to keep the assertion input
 *     explicit rather than relying on an unasserted default.
 *   - On Submit, IADC_uiFormCreateUpdateObjectTest unconditionally sets the new rule's statusId to
 *     Active - consistent with the Rules grid's default "Status" user filter (defaultOption =
 *     Active), so the newly-created rule is visible without changing any grid filter.
 *   - The Rules grid (IADC_uiGridObjectTests) is fixed-filtered to isActive=true and to the
 *     current applicationId - listRecordData against IADC Object Test confirmed Isaac Sandbox
 *     (applicationId=1) currently has zero active test rules, so the created rule is the grid's
 *     only/last row; no grid paging or sorting workaround is required for this run. The grid has
 *     refreshAfter="RECORD_ACTION", so it reloads automatically after the related action completes
 *     without a manual refresh step.
 *   - "Deactivate Test" (used for cleanup) is an unattended process (Update Status -> Write Object
 *     Test -> End) with no form, so clicking the row action executes immediately; no
 *     confirmation dialog/button is expected afterward.
 *
 * All eight criteria describe a single persisted record produced by one Submit, so they are
 * verified together in one test case: fields 1-6 are checked against the refreshed Rules grid
 * (an independent reload, not the values just typed), and Description (not a grid column) is
 * checked by reopening the saved rule through its "Update Test" action, which reloads the record.
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
  // confirmed verbatim against the IADC Object Type / IADC Object Attribute / IADC Object Test
  // Type record data via listRecordData)
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
   * the persisted rule shows the specified Description. Cleanup deactivates the created rule.
   */
  private static void testCase1_RuleIsCreatedAndPersistedWithAllSpecifiedFields() {
    openAddTestRuleDialogForIsaacSandbox();

    int rowCountBefore = fixture.getGridRowCount(RULES_GRID);

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

    // AC 8: rule appears in the Rules list for Isaac Sandbox. The grid refreshes automatically
    // after the related action (refreshAfter="RECORD_ACTION"), and Isaac Sandbox had zero active
    // rules beforehand, so the new rule is the last/only row.
    int rowCountAfter = fixture.getGridRowCount(RULES_GRID);
    if (rowCountAfter != rowCountBefore + 1) {
      throw new AssertionError("Expected the Rules grid for Isaac Sandbox to gain exactly one row "
          + "after saving; had " + rowCountBefore + " before and " + rowCountAfter + " after.");
    }
    String newRow = "[" + rowCountAfter + "]";

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
    // "Deactivate Test" is an unattended action (no confirmation form), so it executes on click.
    LOG.debug("Cleanup: deactivating the rule created by this test");
    fixture.clickOnRecordActionFieldMenuAction(newRow, ACTION_DEACTIVATE_TEST);
    fixture.waitForWorking();
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
}
