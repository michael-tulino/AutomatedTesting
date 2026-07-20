package TestPhase2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.appiancorp.ps.automatedtest.fixture.SitesFixture;

public class AddTestRuleToIsaacSandboxTest {

  private static final String TEST_BROWSER = "CHROME";
  private static final String TEST_SITE_VERSION = "24.3";
  private static final String TEST_SITE_URL = "https://ignytedemo.appiancloud.com/suite";
  private static final String IADC_SITE_URL = "ignyte-appian-developer-copilo";
  private static final String TEST_SITE_LOCALE = "en_US";
  private static final String TEST_USERNAME = "automated.tester";
  private static final Integer TEST_TIMEOUT = 60;

  private static final String RULES_GRID = "[1]";
  private static final String RULE_NAME = "Expression Rules Have 1+ Automated Tests";
  private static final String APPLICATION_NAME = "Isaac Sandbox";
  private static final String OBJECT_TYPE = "Expression Rule";
  private static final String OBJECT_ATTRIBUTE = "Unit Test Count";
  private static final String TEST_TYPE = "> (Greater Than)";
  private static final String TEST_VALUE = "0";

  private static final String CONNECTED_SYSTEM_RULE_NAME = "Connected Systems Have a Name";
  private static final String CONNECTED_SYSTEM_OBJECT_TYPE = "Connected System";
  private static final String CONNECTED_SYSTEM_OBJECT_ATTRIBUTE = "Name";
  private static final String CONNECTED_SYSTEM_TEST_TYPE = "Not Empty";
  private static final String PENDING_REVIEW_STATUS = "Pending Review";

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
  void addTestRuleSavesWithExpectedFieldsAndAppearsInRulesList() {
    fixture.navigateToSite(IADC_SITE_URL);
    fixture.clickOnSitePage("Settings");
    fixture.clickOnLink(APPLICATION_NAME);
    fixture.clickOnRecordRelatedAction("Add Rule");

    fixture.populateFieldWith("Name", new String[] { RULE_NAME });
    fixture.populateFieldWith("Object Type", new String[] { OBJECT_TYPE });
    fixture.populateFieldWith("Object Attribute", new String[] { OBJECT_ATTRIBUTE });
    fixture.populateFieldWith("Test Type", new String[] { TEST_TYPE });
    fixture.populateFieldWith("Test Value", new String[] { TEST_VALUE });
    assertTrue(fixture.verifyFieldIsNotPresent("Description"),
        "Description field should not be available when adding a new rule for " + APPLICATION_NAME);
    fixture.clickOnButton("Submit");

    int totalCount = fixture.getGridTotalCount(RULES_GRID);
    int rowsScanned = 0;
    String foundRow = null;
    while (foundRow == null && rowsScanned < totalCount) {
      int rowCount = fixture.getGridRowCount(RULES_GRID);
      for (int row = 1; row <= rowCount; row++) {
        String rowIndex = "[" + row + "]";
        if (RULE_NAME.equals(fixture.getGridColumnRowValue(RULES_GRID, "Name", rowIndex))) {
          foundRow = rowIndex;
          break;
        }
      }
      rowsScanned += rowCount;
      if (foundRow == null && rowsScanned < totalCount) {
        fixture.clickOnGridNavigation(RULES_GRID, "next");
      }
    }
    assertTrue(foundRow != null, "Saved rule '" + RULE_NAME + "' was not found in the rules list for " + APPLICATION_NAME);

    assertEquals(RULE_NAME, fixture.getGridColumnRowValue(RULES_GRID, "Name", foundRow));
    assertEquals(APPLICATION_NAME, fixture.getGridColumnRowValue(RULES_GRID, "Application", foundRow));
    assertEquals(OBJECT_TYPE, fixture.getGridColumnRowValue(RULES_GRID, "Object Type", foundRow));
    assertEquals(OBJECT_ATTRIBUTE, fixture.getGridColumnRowValue(RULES_GRID, "Attribute", foundRow));
    assertEquals(TEST_TYPE, fixture.getGridColumnRowValue(RULES_GRID, "Operator", foundRow));
    assertEquals(TEST_VALUE, fixture.getGridColumnRowValue(RULES_GRID, "Operand", foundRow));

    int cleanupTotalCount = fixture.getGridTotalCount(RULES_GRID);
    int cleanupRowsScanned = 0;
    String cleanupRow = null;
    fixture.clickOnGridNavigation(RULES_GRID, "first");
    while (cleanupRow == null && cleanupRowsScanned < cleanupTotalCount) {
      int rowCount = fixture.getGridRowCount(RULES_GRID);
      for (int row = 1; row <= rowCount; row++) {
        String rowIndex = "[" + row + "]";
        if (RULE_NAME.equals(fixture.getGridColumnRowValue(RULES_GRID, "Name", rowIndex))) {
          cleanupRow = rowIndex;
          break;
        }
      }
      cleanupRowsScanned += rowCount;
      if (cleanupRow == null && cleanupRowsScanned < cleanupTotalCount) {
        fixture.clickOnGridNavigation(RULES_GRID, "next");
      }
    }
    assertTrue(cleanupRow != null,
        "Saved rule '" + RULE_NAME + "' could not be re-located for cleanup in the rules list for " + APPLICATION_NAME);
    fixture.clickOnRecordActionFieldMenuAction(cleanupRow, "Deactivate Test");
  }

  @Test
  void addTestRuleWithConnectedSystemObjectTypeSetsStatusToPendingReview() {
    fixture.navigateToSite(IADC_SITE_URL);
    fixture.clickOnSitePage("Settings");
    fixture.clickOnLink(APPLICATION_NAME);
    fixture.clickOnRecordRelatedAction("Add Rule");

    fixture.populateFieldWith("Name", new String[] { CONNECTED_SYSTEM_RULE_NAME });
    fixture.populateFieldWith("Object Type", new String[] { CONNECTED_SYSTEM_OBJECT_TYPE });
    fixture.populateFieldWith("Object Attribute", new String[] { CONNECTED_SYSTEM_OBJECT_ATTRIBUTE });
    fixture.populateFieldWith("Test Type", new String[] { CONNECTED_SYSTEM_TEST_TYPE });
    fixture.clickOnButton("Submit");

    int totalCount = fixture.getGridTotalCount(RULES_GRID);
    int rowsScanned = 0;
    String foundRow = null;
    while (foundRow == null && rowsScanned < totalCount) {
      int rowCount = fixture.getGridRowCount(RULES_GRID);
      for (int row = 1; row <= rowCount; row++) {
        String rowIndex = "[" + row + "]";
        if (CONNECTED_SYSTEM_RULE_NAME.equals(fixture.getGridColumnRowValue(RULES_GRID, "Name", rowIndex))) {
          foundRow = rowIndex;
          break;
        }
      }
      rowsScanned += rowCount;
      if (foundRow == null && rowsScanned < totalCount) {
        fixture.clickOnGridNavigation(RULES_GRID, "next");
      }
    }
    assertTrue(foundRow != null,
        "Saved rule '" + CONNECTED_SYSTEM_RULE_NAME + "' was not found in the rules list for " + APPLICATION_NAME);

    assertEquals(PENDING_REVIEW_STATUS, fixture.getGridColumnRowValue(RULES_GRID, "Status", foundRow));

    int cleanupTotalCount = fixture.getGridTotalCount(RULES_GRID);
    int cleanupRowsScanned = 0;
    String cleanupRow = null;
    fixture.clickOnGridNavigation(RULES_GRID, "first");
    while (cleanupRow == null && cleanupRowsScanned < cleanupTotalCount) {
      int rowCount = fixture.getGridRowCount(RULES_GRID);
      for (int row = 1; row <= rowCount; row++) {
        String rowIndex = "[" + row + "]";
        if (CONNECTED_SYSTEM_RULE_NAME.equals(fixture.getGridColumnRowValue(RULES_GRID, "Name", rowIndex))) {
          cleanupRow = rowIndex;
          break;
        }
      }
      cleanupRowsScanned += rowCount;
      if (cleanupRow == null && cleanupRowsScanned < cleanupTotalCount) {
        fixture.clickOnGridNavigation(RULES_GRID, "next");
      }
    }
    assertTrue(cleanupRow != null,
        "Saved rule '" + CONNECTED_SYSTEM_RULE_NAME + "' could not be re-located for cleanup in the rules list for "
            + APPLICATION_NAME);
    fixture.clickOnRecordActionFieldMenuAction(cleanupRow, "Deactivate Test");
  }
}
