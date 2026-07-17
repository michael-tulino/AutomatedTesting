import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.appiancorp.ps.automatedtest.fixture.SitesFixture;

/**
 * IV-198: Add A New Rule to Isaac Sandbox - Ignyte Appian Developer Copilot
 *
 * Business Description: As a developer using the IADC tool for my Isaac Sandbox
 * application, I want to add a new rule.
 *
 * Acceptance Criteria:
 * Given the "Add Test Rule" dialog is open, when I create the rule, then the saved rule has:
 * 1. Name = "Expression Rules Have 1+ Automated Tests"
 * 2. Application = Isaac Sandbox
 * 3. Object Type = Expression Rule
 * 4. Object Attribute = Unit Test Count
 * 5. Test Type = > (Greater Than)
 * 6. Test Value = 0
 * 7. Description = "Expression Rules Have 1+ Automated Tests"
 * 8. On Submit, the rule saves without validation error and appears in the rules
 *    list for the Isaac Sandbox application.
 */
public class AddNewRuleToIsaacSandboxTest {
  protected static String TEST_BROWSER = "CHROME";
  protected static String TEST_SITE_VERSION = "24.3";
  protected static String TEST_SITE_URL = "https://ignytedemo.appiancloud.com";
  protected static String TEST_SITE_LOCALE = "en_US";
  protected static String TEST_USERNAME = "admin.user"; //Make sure this username has an entry in users.properties
  protected static Integer TEST_TIMEOUT = 60;

  private static final String IADC_SITE_URL = "ignyte-appian-developer-copilo";

  private static final String RULE_NAME = "Expression Rules Have 1+ Automated Tests";
  private static final String RULE_APPLICATION = "Isaac Sandbox";
  private static final String RULE_OBJECT_TYPE = "Expression Rule";
  private static final String RULE_OBJECT_ATTRIBUTE = "Unit Test Count";
  private static final String RULE_TEST_TYPE = "> (Greater Than)";
  private static final String RULE_TEST_VALUE = "0";
  private static final String RULE_DESCRIPTION = "Expression Rules Have 1+ Automated Tests";

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
  }

  @AfterAll
  static void tearDown() {
    fixture.tearDown();
  }

  @Test
  void testAddNewRuleToIsaacSandbox() {
    fixture.navigateToSitePage(IADC_SITE_URL, "Home");

    // Navigate to the Isaac Sandbox application record and open the "Add Test Rule" dialog
    // (surfaced via the "Add Rule" related action on the IADC Application record).
    fixture.clickOnMenu("Records");
    fixture.clickOnRecordType("IADC Application");
    fixture.clickOnRecord(RULE_APPLICATION);
    fixture.clickOnRecordRelatedAction("Add Rule");

    // Object Type must be populated before Object Attribute: the Object Attribute dropdown
    // is disabled until Object Type has a value, so this ordering keeps it editable.
    fixture.populateFieldWith("Name", new String[] {RULE_NAME});
    fixture.populateFieldWith("Application", new String[] {RULE_APPLICATION});
    fixture.populateFieldWith("Object Type", new String[] {RULE_OBJECT_TYPE});
    fixture.populateFieldWith("Object Attribute", new String[] {RULE_OBJECT_ATTRIBUTE});
    fixture.populateFieldWith("Test Type", new String[] {RULE_TEST_TYPE});
    fixture.populateFieldWith("Test Value", new String[] {RULE_TEST_VALUE});
    fixture.populateFieldWith("Description", new String[] {RULE_DESCRIPTION});

    fixture.clickOnButton("Submit");

    // AC 8 (part 1): the rule saves without validation error, i.e. the dialog closes on submit.
    assertTrue(
        fixture.verifyButtonIsNotPresent("Submit"),
        "Add Test Rule dialog should close after a successful submit with no validation error");

    // AC 8 (part 2): the rule appears in the rules list for the Isaac Sandbox application.
    // The grid refreshes automatically after the related action (refreshAfter: RECORD_ACTION),
    // but the new row's page position is not guaranteed, so page through until it is located.
    int totalCount = fixture.getGridTotalCount(RULES_GRID);
    int rowsPerPage = fixture.getGridRowCount(RULES_GRID);
    int maxPages = (int) Math.ceil((double) totalCount / rowsPerPage);

    fixture.clickOnGridNavigation(RULES_GRID, "first");

    Integer foundRow = null;
    for (int page = 0; page < maxPages && foundRow == null; page++) {
      int rowsOnPage = fixture.getGridRowCount(RULES_GRID);
      for (int row = 1; row <= rowsOnPage; row++) {
        if (RULE_NAME.equals(fixture.getGridColumnRowValue(RULES_GRID, "Name", String.valueOf(row)))) {
          foundRow = row;
          break;
        }
      }
      if (foundRow == null && page < maxPages - 1) {
        fixture.clickOnGridNavigation(RULES_GRID, "next");
      }
    }

    assertTrue(foundRow != null, "Newly created rule was not found in the Rules grid for Isaac Sandbox");

    String rowNum = String.valueOf(foundRow);

    // AC 2-6: verify the persisted rule's fields as rendered from the reloaded grid row.
    assertEquals(RULE_APPLICATION, fixture.getGridColumnRowValue(RULES_GRID, "Application", rowNum));
    assertEquals(RULE_OBJECT_TYPE, fixture.getGridColumnRowValue(RULES_GRID, "Object Type", rowNum));
    assertEquals(RULE_OBJECT_ATTRIBUTE, fixture.getGridColumnRowValue(RULES_GRID, "Attribute", rowNum));
    assertEquals(RULE_TEST_TYPE, fixture.getGridColumnRowValue(RULES_GRID, "Operator", rowNum));
    assertEquals(RULE_TEST_VALUE, fixture.getGridColumnRowValue(RULES_GRID, "Operand", rowNum));

    // AC 7: Description is not shown as a grid column, so reopen the persisted record via the
    // row's "Update Test" action (an independent reload boundary) to verify it, then cancel out
    // without making changes.
    fixture.clickOnRecordActionFieldMenuAction(rowNum, "Update Test");
    assertEquals(RULE_DESCRIPTION, fixture.getFieldValue("Description"));
    fixture.clickOnButton("Cancel");

    // Cleanup: deactivate the rule created by this test so repeated runs do not accumulate
    // duplicate active rules in the Isaac Sandbox rules list.
    fixture.clickOnRecordActionFieldMenuAction(rowNum, "Deactivate Test");
  }
}
