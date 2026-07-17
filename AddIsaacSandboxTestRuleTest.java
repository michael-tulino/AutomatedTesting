package TestPhase1;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.appiancorp.ps.automatedtest.fixture.SitesFixture;

public class AddIsaacSandboxTestRuleTest {

  private static final String TEST_SITE_URL = "https://ignytedemo.appiancloud.com/suite";
  private static final String IADC_SITE_URL = "ignyte-appian-developer-copilo";
  private static final String TEST_USERNAME = "automated.tester";
  private static final String TEST_BROWSER = "CHROME";
  private static final String TEST_SITE_VERSION = "24.3";
  private static final String TEST_SITE_LOCALE = "en_US";
  private static final Integer TEST_TIMEOUT = 60;

  private static final String RECORD_TYPE_APPLICATION = "IADC Application";
  private static final String APPLICATION_NAME = "Isaac Sandbox";
  private static final String ADD_RULE_ACTION = "Add Rule";
  private static final String UPDATE_TEST_ACTION = "Update Test";
  private static final String DEACTIVATE_TEST_ACTION = "Deactivate Test";
  private static final String RULES_GRID = "[1]";

  private static final String RULE_NAME = "Expression Rules Have 1+ Automated Tests";
  private static final String OBJECT_TYPE = "Expression Rule";
  private static final String OBJECT_ATTRIBUTE = "Unit Test Count";
  private static final String TEST_TYPE = "> (Greater Than)";
  private static final String TEST_VALUE = "0";
  private static final String DESCRIPTION = "Expression Rules Have 1+ Automated Tests";

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

  /**
   * Covers IV-198 acceptance criteria: given the "Add Test Rule" dialog is open
   * for the Isaac Sandbox application, when the rule is created with Name,
   * Object Type, Object Attribute, Test Type, and Test Value populated, then
   * the saved rule persists all of those values plus the Description, and it
   * saves without validation error and appears in the Isaac Sandbox rules list.
   */
  @Test
  public void addTestRule_savesAllFieldsAndAppearsInRulesList() {
    fixture.clickOnMenu("Records");
    fixture.clickOnRecordType(RECORD_TYPE_APPLICATION);
    fixture.clickOnRecord(APPLICATION_NAME);

    fixture.clickOnRecordRelatedAction(ADD_RULE_ACTION);

    populateField("Name", RULE_NAME);
    populateField("Object Type", OBJECT_TYPE);
    populateField("Object Attribute", OBJECT_ATTRIBUTE);
    populateField("Test Type", TEST_TYPE);
    populateField("Test Value", TEST_VALUE);
    populateField("Description", DESCRIPTION);

    fixture.clickOnButton("Submit");

    int rowNum = findRowByName(RULE_NAME);
    if (rowNum == -1) {
      fail("Created rule \"" + RULE_NAME + "\" did not appear in the Isaac Sandbox rules list after submit.");
    }

    assertEquals(RULE_NAME, fixture.getGridColumnRowValue(RULES_GRID, "Name", String.valueOf(rowNum)));
    assertEquals(APPLICATION_NAME, fixture.getGridColumnRowValue(RULES_GRID, "Application", String.valueOf(rowNum)));
    assertEquals(OBJECT_TYPE, fixture.getGridColumnRowValue(RULES_GRID, "Object Type", String.valueOf(rowNum)));
    assertEquals(OBJECT_ATTRIBUTE, fixture.getGridColumnRowValue(RULES_GRID, "Attribute", String.valueOf(rowNum)));
    assertEquals(TEST_TYPE, fixture.getGridColumnRowValue(RULES_GRID, "Operator", String.valueOf(rowNum)));
    assertEquals(TEST_VALUE, fixture.getGridColumnRowValue(RULES_GRID, "Operand", String.valueOf(rowNum)));

    fixture.clickOnRecordActionFieldMenuAction(String.valueOf(rowNum), UPDATE_TEST_ACTION);
    assertEquals(DESCRIPTION, fixture.getFieldValue("Description"));
    fixture.clickOnButton("Cancel");

    fixture.clickOnRecordActionFieldMenuAction(String.valueOf(rowNum), DEACTIVATE_TEST_ACTION);
  }

  private int findRowByName(String ruleName) {
    int totalCount = fixture.getGridTotalCount(RULES_GRID);
    int rowsSeenAcrossPages = 0;
    while (true) {
      int rowCount = fixture.getGridRowCount(RULES_GRID);
      for (int row = 1; row <= rowCount; row++) {
        if (ruleName.equals(fixture.getGridColumnRowValue(RULES_GRID, "Name", String.valueOf(row)))) {
          return row;
        }
      }
      rowsSeenAcrossPages += rowCount;
      if (rowsSeenAcrossPages >= totalCount) {
        return -1;
      }
      fixture.clickOnGridNavigation(RULES_GRID, "next");
    }
  }

  private void populateField(String fieldName, String value) {
    try {
      fixture.populateFieldWith(fieldName, new String[] { value });
    } catch (Exception e) {
      System.out.println("Skipped populating field \"" + fieldName + "\" - it was not editable: " + e.getMessage());
    }
  }
}
