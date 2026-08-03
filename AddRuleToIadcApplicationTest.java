package autogen;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.appiancorp.ps.automatedtest.fixture.SitesFixture;

// IV-198: On the summary tab for an IADC application, add a new "Add Rule" action that launches
// a form (Name, Application, Object Type, Object Attribute, Test Type, Test Value, Description)
// with Cancel/Submit buttons; Submit saves the rule to the application's Rules Grid.
public class AddRuleToIadcApplicationTest {
  private static final String TEST_SITE_URL = "https://ignytedemo.appiancloud.com/suite";
  private static final String IADC_SITE_URL = "ignyte-appian-developer-copilo";
  private static final String TEST_USERNAME = "automated.tester";
  private static final String TEST_BROWSER = "CHROME";
  private static final String TEST_SITE_VERSION = "24.3";
  private static final String TEST_SITE_LOCALE = "en_US";
  private static final Integer TEST_TIMEOUT = 60;

  private static final String TEST_APPLICATION_NAME = "Ignyte Appian Developer Copilot";
  private static final String ADD_RULE_ACTION = "Add Rule";
  private static final String DEACTIVATE_RULE_ACTION = "Deactivate Test";
  private static final String RULES_GRID_INDEX = "[1]";
  private static final int MAX_PAGES_TO_SCAN = 25;
  private static final String RULE_NAME_PREFIX = "IV198_";
  private static final String OBJECT_TYPE_VALUE = "Expression Rule";
  private static final String OBJECT_ATTRIBUTE_VALUE = "Name";
  private static final String TEST_TYPE_VALUE = "Contains";

  private static final Logger LOG = LogManager.getLogger(AddRuleToIadcApplicationTest.class);
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

  // No cleanup needed: this test only checks for the presence of a related action and does not
  // persist any data.
  @Test
  public void testAddRuleActionIsPresentOnApplicationSummary() {
    navigateToApplicationRecord();

    assertTrue(
        fixture.verifyRecordRelatedActionIsPresent(ADD_RULE_ACTION),
        "The [" + ADD_RULE_ACTION + "] related action should be present on the ["
            + TEST_APPLICATION_NAME + "] application's Summary tab.");
  }

  // No cleanup needed: this test only checks which fields render on the form and never clicks
  // Submit, so nothing is persisted.
  @Test
  public void testAddRuleFormDisplaysAllRequiredFields() {
    navigateToApplicationRecord();
    fixture.clickOnRecordRelatedAction(ADD_RULE_ACTION);

    assertTrue(fixture.verifyFieldIsPresent("Name"), "The [Name] field should be present on the Add Rule form.");
    assertTrue(fixture.verifyFieldIsPresent("Application"), "The [Application] field should be present on the Add Rule form.");
    assertTrue(fixture.verifyFieldIsPresent("Object Type"), "The [Object Type] field should be present on the Add Rule form.");
    assertTrue(fixture.verifyFieldIsPresent("Object Attribute"), "The [Object Attribute] field should be present on the Add Rule form.");
    assertTrue(fixture.verifyFieldIsPresent("Test Type"), "The [Test Type] field should be present on the Add Rule form.");
    assertTrue(fixture.verifyFieldIsPresent("Test Value"), "The [Test Value] field should be present on the Add Rule form.");
    assertTrue(fixture.verifyFieldIsPresent("Description"), "The [Description] field should be present on the Add Rule form.");
  }

  @Test
  public void testSubmitOnAddRuleFormSavesRuleToRulesGrid() {
    String ruleName = RULE_NAME_PREFIX + fixture.getRandomAlphabetString(8);
    String testValue = fixture.getRandomAlphabetString(10);
    String description = "Automated test rule " + fixture.getRandomAlphabetString(6);

    navigateToApplicationRecord();
    fixture.clickOnRecordRelatedAction(ADD_RULE_ACTION);
    populateAddRuleForm(ruleName, testValue, description);
    fixture.clickOnButton("Submit");

    assertTrue(
        ruleIsPresentInRulesGrid(ruleName),
        "Rule [" + ruleName + "] should show up in the Rules Grid after submitting the Add Rule form.");

    deactivateRuleNamed(ruleName);
  }

  // No cleanup needed: clicking Cancel does not save the rule, so nothing is persisted.
  @Test
  public void testCancelOnAddRuleFormDoesNotSaveRule() {
    String ruleName = RULE_NAME_PREFIX + fixture.getRandomAlphabetString(8);
    String testValue = fixture.getRandomAlphabetString(10);
    String description = "Automated test rule " + fixture.getRandomAlphabetString(6);

    navigateToApplicationRecord();
    fixture.clickOnRecordRelatedAction(ADD_RULE_ACTION);
    populateAddRuleForm(ruleName, testValue, description);
    fixture.clickOnButton("Cancel");

    assertFalse(
        ruleIsPresentInRulesGrid(ruleName),
        "Rule [" + ruleName + "] should not be saved to the Rules Grid after clicking Cancel.");
  }

  private void navigateToApplicationRecord() {
    fixture.navigateToSite(IADC_SITE_URL);
    fixture.clickOnSitePage("Settings");
    fixture.clickOnRecord(TEST_APPLICATION_NAME);
  }

  private void populateAddRuleForm(String ruleName, String testValue, String description) {
    populateRequiredField("Name", ruleName);
    populateRequiredField("Application", TEST_APPLICATION_NAME);
    populateOptionalField("Object Type", OBJECT_TYPE_VALUE);
    populateRequiredField("Object Attribute", OBJECT_ATTRIBUTE_VALUE);
    populateRequiredField("Test Type", TEST_TYPE_VALUE);
    populateOptionalField("Test Value", testValue);
    populateOptionalField("Description", description);
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

  private boolean ruleIsPresentInRulesGrid(String ruleName) {
    navigateToApplicationRecord();

    for (int page = 1; page <= MAX_PAGES_TO_SCAN; page++) {
      int rowCount = fixture.getGridRowCount(RULES_GRID_INDEX);
      for (int row = 1; row <= rowCount; row++) {
        String name = fixture.getGridColumnRowValue(RULES_GRID_INDEX, "Name", "[" + row + "]");
        if (ruleName.equals(name)) {
          return true;
        }
      }

      try {
        fixture.clickOnGridNavigation(RULES_GRID_INDEX, "next");
      } catch (RuntimeException e) {
        return false;
      }
    }

    return false;
  }

  private void deactivateRuleNamed(String ruleName) {
    navigateToApplicationRecord();

    for (int page = 1; page <= MAX_PAGES_TO_SCAN; page++) {
      int rowCount = fixture.getGridRowCount(RULES_GRID_INDEX);
      for (int row = 1; row <= rowCount; row++) {
        String name = fixture.getGridColumnRowValue(RULES_GRID_INDEX, "Name", "[" + row + "]");
        if (ruleName.equals(name)) {
          fixture.clickOnRecordActionFieldMenuAction("[" + row + "]", DEACTIVATE_RULE_ACTION);
          return;
        }
      }

      try {
        fixture.clickOnGridNavigation(RULES_GRID_INDEX, "next");
      } catch (RuntimeException e) {
        break;
      }
    }

    fail("Could not find rule [" + ruleName + "] in the Rules Grid to clean it up via ["
        + DEACTIVATE_RULE_ACTION + "].");
  }
}
