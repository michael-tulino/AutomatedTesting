package autogen;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.appiancorp.ps.automatedtest.fixture.SitesFixture;

// IV-198: Add a new "Add Rule" action to an IADC Application's summary page that launches a form
// to create a new Test Rule, with Cancel/Submit buttons.
public class AddObjectTestTest {
  private static final String TEST_SITE_URL = "https://ignytedemo.appiancloud.com/suite";
  private static final String IADC_SITE_URL = "ignyte-appian-developer-copilo";
  private static final String TEST_USERNAME = "automated.tester";
  private static final String TEST_BROWSER = "CHROME";
  private static final String TEST_SITE_VERSION = "24.3";
  private static final String TEST_SITE_LOCALE = "en_US";
  private static final Integer TEST_TIMEOUT = 60;

  private static final String IADC_SETTINGS_PAGE = "Settings";
  private static final String ISAAC_SANDBOX_APPLICATION_NAME = "Isaac Sandbox";

  private static final String ACTION_ADD_RULE = "Add Rule";
  private static final String ACTION_UPDATE_TEST = "Update Test";
  private static final String ACTION_DEACTIVATE_TEST = "Deactivate Test";

  private static final String FIELD_NAME = "Name";
  private static final String FIELD_OBJECT_TYPE = "Object Type";
  private static final String FIELD_OBJECT_ATTRIBUTE = "Object Attribute";
  private static final String FIELD_TEST_TYPE = "Test Type";
  private static final String FIELD_TEST_VALUE = "Test Value";
  private static final String FIELD_DESCRIPTION = "Description";

  private static final String BUTTON_SUBMIT = "Submit";
  private static final String BUTTON_CANCEL = "Cancel";

  private static final String RULES_GRID_INDEX = "[1]";
  private static final int MAX_PAGES_TO_SCAN = 25;
  private static final String COLUMN_NAME = "Name";
  private static final String COLUMN_APPLICATION = "Application";
  private static final String COLUMN_OBJECT_TYPE = "Object Type";
  private static final String COLUMN_ATTRIBUTE = "Attribute";
  private static final String COLUMN_OPERATOR = "Operator";
  private static final String COLUMN_OPERAND = "Operand";

  private static final String RULE_NAME_PREFIX = "IV198_";
  private static final String RULE_OBJECT_TYPE = "Expression Rule";
  private static final String RULE_OBJECT_ATTRIBUTE = "Name";
  private static final String RULE_TEST_TYPE = "Contains";

  private static final Logger LOG = LogManager.getLogger(AddObjectTestTest.class);
  private static SitesFixture fixture;

  @BeforeAll
  public static void setup() {
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
  public static void cleanup() {
    fixture.tearDown();
  }

  // No cleanup needed: this test only checks for the action's presence and persists nothing.
  @Test
  public void testAddRuleActionIsPresentOnApplicationSummary() {
    navigateToIsaacSandboxApplicationSummary();

    assertTrue(
        fixture.verifyRecordRelatedActionIsPresent(ACTION_ADD_RULE),
        "The [" + ACTION_ADD_RULE + "] action should be present on an IADC Application's summary.");
  }

  @Test
  public void testSubmitOnAddRuleFormSavesRuleToRulesGrid() {
    String ruleName = RULE_NAME_PREFIX + fixture.getRandomAlphabetString(8);
    String testValue = fixture.getRandomAlphabetString(6);
    String description = "Automated test rule " + ruleName;

    navigateToIsaacSandboxApplicationSummary();
    fixture.clickOnRecordRelatedAction(ACTION_ADD_RULE);
    fixture.waitForWorking();
    populateAddRuleForm(ruleName, testValue, description);
    fixture.clickOnButton(BUTTON_SUBMIT);
    fixture.waitForWorking();

    navigateToIsaacSandboxApplicationSummary();
    String row = findRuleRowByName(ruleName);
    assertTrue(
        row != null,
        "Rule [" + ruleName + "] should show up in the Rules Grid after submitting the Add Rule form.");

    fixture.verifyGridColumnRowContainsValue(RULES_GRID_INDEX, COLUMN_APPLICATION, row, ISAAC_SANDBOX_APPLICATION_NAME);
    fixture.verifyGridColumnRowContainsValue(RULES_GRID_INDEX, COLUMN_OBJECT_TYPE, row, RULE_OBJECT_TYPE);
    fixture.verifyGridColumnRowContainsValue(RULES_GRID_INDEX, COLUMN_ATTRIBUTE, row, RULE_OBJECT_ATTRIBUTE);
    fixture.verifyGridColumnRowContainsValue(RULES_GRID_INDEX, COLUMN_OPERATOR, row, RULE_TEST_TYPE);
    fixture.verifyGridColumnRowContainsValue(RULES_GRID_INDEX, COLUMN_OPERAND, row, testValue);

    // Description has no grid column, so verify it by reopening the persisted rule.
    fixture.clickOnRecordActionFieldMenuAction(row, ACTION_UPDATE_TEST);
    fixture.waitForWorking();
    fixture.verifyFieldContainsValue(FIELD_DESCRIPTION, description);
    fixture.clickOnButton(BUTTON_CANCEL);
    fixture.waitForWorking();

    // Cleanup: deactivate the rule created above. Re-locate it fresh rather than reusing the row
    // reference captured earlier, since the grid may have changed page/position since then.
    navigateToIsaacSandboxApplicationSummary();
    String cleanupRow = findRuleRowByName(ruleName);
    fixture.clickOnRecordActionFieldMenuAction(cleanupRow, ACTION_DEACTIVATE_TEST);
    fixture.waitForWorking();
  }

  // No cleanup needed: Cancel does not save the rule, so nothing is persisted by this test.
  @Test
  public void testCancelOnAddRuleFormDoesNotSaveRule() {
    String ruleName = RULE_NAME_PREFIX + fixture.getRandomAlphabetString(8);
    String testValue = fixture.getRandomAlphabetString(6);
    String description = "Automated test rule " + ruleName;

    navigateToIsaacSandboxApplicationSummary();
    fixture.clickOnRecordRelatedAction(ACTION_ADD_RULE);
    fixture.waitForWorking();
    populateAddRuleForm(ruleName, testValue, description);
    fixture.clickOnButton(BUTTON_CANCEL);
    fixture.waitForWorking();

    navigateToIsaacSandboxApplicationSummary();
    assertTrue(
        findRuleRowByName(ruleName) == null,
        "Rule [" + ruleName + "] should not be saved to the Rules Grid after clicking Cancel.");
  }

  private void navigateToIsaacSandboxApplicationSummary() {
    fixture.navigateToSite(IADC_SITE_URL);
    fixture.clickOnSitePage(IADC_SETTINGS_PAGE);
    fixture.clickOnLink(ISAAC_SANDBOX_APPLICATION_NAME);
    fixture.waitForWorking();
  }

  private void populateAddRuleForm(String ruleName, String testValue, String description) {
    // Object Type must be populated before Object Attribute: the Object Attribute dropdown is
    // disabled until Object Type has a value, and its choices are filtered by Object Type.
    populateRequiredField(FIELD_NAME, ruleName);
    populateRequiredField(FIELD_OBJECT_TYPE, RULE_OBJECT_TYPE);
    populateRequiredField(FIELD_OBJECT_ATTRIBUTE, RULE_OBJECT_ATTRIBUTE);
    populateRequiredField(FIELD_TEST_TYPE, RULE_TEST_TYPE);
    populateOptionalField(FIELD_TEST_VALUE, testValue);
    populateOptionalField(FIELD_DESCRIPTION, description);
    // Application is not populated here: the Add Rule action is launched from the Isaac Sandbox
    // application's own summary page, which pre-supplies Application from that context.
  }

  private void populateRequiredField(String fieldName, String value) {
    String currentValue = fixture.getFieldValue(fieldName);
    if (value.equals(currentValue)) {
      LOG.debug("SKIP POPULATE (already set) [" + fieldName + "]");
      return;
    }

    try {
      fixture.populateFieldWithValue(fieldName, value);
    } catch (RuntimeException e) {
      fail("Required field [" + fieldName + "] was not editable: " + e.getMessage());
    }
  }

  private void populateOptionalField(String fieldName, String value) {
    String currentValue = fixture.getFieldValue(fieldName);
    if (value.equals(currentValue)) {
      LOG.debug("SKIP POPULATE (already set) [" + fieldName + "]");
      return;
    }

    try {
      fixture.populateFieldWithValue(fieldName, value);
    } catch (RuntimeException e) {
      LOG.debug("SKIP POPULATE (not editable) [" + fieldName + "]: " + e.getMessage());
    }
  }

  private String findRuleRowByName(String ruleName) {
    for (int page = 1; page <= MAX_PAGES_TO_SCAN; page++) {
      int rowCount = fixture.getGridRowCount(RULES_GRID_INDEX);
      for (int row = 1; row <= rowCount; row++) {
        String rowRef = "[" + row + "]";
        String name = fixture.getGridColumnRowValue(RULES_GRID_INDEX, COLUMN_NAME, rowRef);
        if (ruleName.equals(name)) {
          return rowRef;
        }
      }

      try {
        fixture.clickOnGridNavigation(RULES_GRID_INDEX, "next");
      } catch (RuntimeException e) {
        return null;
      }
    }

    return null;
  }
}
