package autogen;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.appiancorp.ps.automatedtest.fixture.SitesFixture;

public class AddRuleToIsaacSandboxTest {

  private static final String TEST_BROWSER = "CHROME";
  private static final String TEST_SITE_VERSION = "24.3";
  private static final String TEST_SITE_URL = "https://ignytedemo.appiancloud.com/suite";
  private static final String TEST_SITE_LOCALE = "en_US";
  private static final String TEST_USERNAME = "automated.tester";
  private static final Integer TEST_TIMEOUT = 60;
  private static final String IADC_SITE_URL = "ignyte-appian-developer-copilo";

  private static final String APPLICATIONS_GRID = "[1]";
  private static final String RULES_GRID = "[1]";
  private static final String COLUMN_NAME = "Name";

  private static final String ISAAC_SANDBOX_APPLICATION_NAME = "Isaac Sandbox";
  private static final String ADD_RULE_ACTION = "Add Rule";
  private static final String UPDATE_TEST_ACTION = "Update Test";
  private static final String DEACTIVATE_TEST_ACTION = "Deactivate Test";

  private static final String FIELD_NAME = "Name";
  private static final String FIELD_OBJECT_TYPE = "Object Type";
  private static final String FIELD_OBJECT_ATTRIBUTE = "Object Attribute";
  private static final String FIELD_TEST_TYPE = "Test Type";
  private static final String FIELD_APPLICATION = "Application";
  private static final String FIELD_TEST_VALUE = "Test Value";
  private static final String FIELD_DESCRIPTION = "Description";

  private static final String RULE_NAME = "Expression Rules Have 1+ Automated Tests";
  private static final String RULE_APPLICATION = "Isaac Sandbox";
  private static final String RULE_OBJECT_TYPE = "Expression Rule";
  private static final String RULE_OBJECT_ATTRIBUTE = "Unit Test Count";
  private static final String RULE_TEST_TYPE = "> (Greater Than)";
  private static final String RULE_TEST_VALUE = "0";
  private static final String RULE_DESCRIPTION = "Expression Rules Have 1+ Automated Tests";

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
  void addRuleSavesWithoutValidationErrorAndAppearsInRulesList() {
    fixture.navigateToSite(IADC_SITE_URL);
    fixture.clickOnSitePage("Settings");

    int applicationRow = locateRowByColumnValue(APPLICATIONS_GRID, COLUMN_NAME, ISAAC_SANDBOX_APPLICATION_NAME);
    assertTrue(applicationRow > 0,
        "'" + ISAAC_SANDBOX_APPLICATION_NAME + "' was not found in the Applications grid");

    fixture.clickOnGridColumnRow(APPLICATIONS_GRID, COLUMN_NAME, "[" + applicationRow + "]");

    assertTrue(fixture.verifyRecordRelatedActionIsPresent(ADD_RULE_ACTION),
        "'" + ADD_RULE_ACTION + "' related action should be available on the " + ISAAC_SANDBOX_APPLICATION_NAME
            + " application summary page");

    fixture.clickOnRecordRelatedAction(ADD_RULE_ACTION);

    assertTrue(fixture.verifyFieldContainsValue(FIELD_APPLICATION, RULE_APPLICATION),
        "Application field should default to " + RULE_APPLICATION + " when adding a rule from its summary page");

    fixture.populateFieldWithValue(FIELD_NAME, RULE_NAME);
    fixture.populateFieldWithValue(FIELD_OBJECT_TYPE, RULE_OBJECT_TYPE);
    fixture.populateFieldWithValue(FIELD_OBJECT_ATTRIBUTE, RULE_OBJECT_ATTRIBUTE);
    fixture.populateFieldWithValue(FIELD_TEST_TYPE, RULE_TEST_TYPE);
    fixture.populateFieldWithValue(FIELD_TEST_VALUE, RULE_TEST_VALUE);
    fixture.populateFieldWithValue(FIELD_DESCRIPTION, RULE_DESCRIPTION);

    fixture.clickOnButton("Submit");

    assertFalse(fixture.errorIsPresent(), "Saving the rule should not produce a validation error");

    int ruleRow = locateRowByColumnValue(RULES_GRID, COLUMN_NAME, RULE_NAME);
    assertTrue(ruleRow > 0,
        "Newly created rule '" + RULE_NAME + "' was not found in the " + ISAAC_SANDBOX_APPLICATION_NAME
            + " Rules grid");

    fixture.clickOnRecordActionFieldMenuAction("[" + ruleRow + "]", UPDATE_TEST_ACTION);

    assertTrue(fixture.verifyFieldContainsValue(FIELD_NAME, RULE_NAME), "Saved rule Name should be " + RULE_NAME);
    assertTrue(fixture.verifyFieldContainsValue(FIELD_APPLICATION, RULE_APPLICATION),
        "Saved rule Application should be " + RULE_APPLICATION);
    assertTrue(fixture.verifyFieldContainsValue(FIELD_OBJECT_TYPE, RULE_OBJECT_TYPE),
        "Saved rule Object Type should be " + RULE_OBJECT_TYPE);
    assertTrue(fixture.verifyFieldContainsValue(FIELD_OBJECT_ATTRIBUTE, RULE_OBJECT_ATTRIBUTE),
        "Saved rule Object Attribute should be " + RULE_OBJECT_ATTRIBUTE);
    assertTrue(fixture.verifyFieldContainsValue(FIELD_TEST_TYPE, RULE_TEST_TYPE),
        "Saved rule Test Type should be " + RULE_TEST_TYPE);
    assertTrue(fixture.verifyFieldContainsValue(FIELD_TEST_VALUE, RULE_TEST_VALUE),
        "Saved rule Test Value should be " + RULE_TEST_VALUE);
    assertTrue(fixture.verifyFieldContainsValue(FIELD_DESCRIPTION, RULE_DESCRIPTION),
        "Saved rule Description should be " + RULE_DESCRIPTION);

    fixture.clickOnButton("Cancel");

    int cleanupRow = locateRowByColumnValue(RULES_GRID, COLUMN_NAME, RULE_NAME);
    if (cleanupRow > 0) {
      fixture.clickOnRecordActionFieldMenuAction("[" + cleanupRow + "]", DEACTIVATE_TEST_ACTION);
    }
  }

  private static int locateRowByColumnValue(String gridName, String columnName, String targetValue) {
    int totalCount = fixture.getGridTotalCount(gridName);
    int checked = 0;
    while (checked < totalCount) {
      int rowCount = fixture.getGridRowCount(gridName);
      for (int i = 1; i <= rowCount; i++) {
        String value = fixture.getGridColumnRowValue(gridName, columnName, "[" + i + "]");
        if (targetValue.equals(value)) {
          return i;
        }
      }
      checked += rowCount;
      if (checked < totalCount) {
        fixture.clickOnGridNavigation(gridName, "next");
      }
    }
    return -1;
  }
}
