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

// IV-198: On the summary tab for an IADC Application, add a new "Add Rule" action that launches
// a form (Name, Application, Object Type, Object Attribute, Test Type, Test Value, Description)
// with Cancel/Submit buttons. Submitting saves the rule and it shows up in the Rules Grid on the
// Application's tab; Cancel submits without saving.
public class AddRuleToIadcApplicationTest {
  private static final String TEST_SITE_URL = "https://ignytedemo.appiancloud.com/suite";
  private static final String IADC_SITE_URL = "ignyte-appian-developer-copilo";
  private static final String TEST_USERNAME = "automated.tester";
  private static final String TEST_BROWSER = "CHROME";
  private static final String TEST_SITE_VERSION = "24.3";
  private static final String TEST_SITE_LOCALE = "en_US";
  private static final Integer TEST_TIMEOUT = 60;

  private static final String APPLICATIONS_GRID_INDEX = "[1]";
  private static final String RULES_GRID_INDEX = "[1]";
  private static final int MAX_PAGES_TO_SCAN = 25;
  private static final String TARGET_APPLICATION_NAME = "Ignyte Appian Developer Copilot";
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

  @Test
  public void testAddRuleActionIsPresentOnApplicationSummary() {
    navigateToTargetApplicationSummary();

    assertTrue(
        fixture.verifyRecordRelatedActionIsPresent("Add Rule"),
        "The [Add Rule] action should be present on the [" + TARGET_APPLICATION_NAME
            + "] Application's summary tab.");
  }

  @Test
  public void testSubmitOnAddRuleFormSavesRuleToRulesGrid() {
    String ruleName = RULE_NAME_PREFIX + fixture.getRandomAlphabetString(8);
    String testValue = fixture.getRandomAlphabetString(10);
    String description = "Automated test rule " + ruleName;

    navigateToTargetApplicationSummary();
    fixture.clickOnRecordRelatedAction("Add Rule");
    populateAddRuleForm(ruleName, testValue, description);
    fixture.clickOnButton("Submit");

    navigateToTargetApplicationSummary();
    int row = findRuleRowInRulesGrid(ruleName);
    assertTrue(
        row > 0,
        "Rule [" + ruleName + "] should show up in the Rules Grid on the ["
            + TARGET_APPLICATION_NAME + "] Application's tab after submitting the Add Rule form.");

    deactivateRule(ruleName);
  }

  @Test
  public void testCancelOnAddRuleFormDoesNotSaveRule() {
    String ruleName = RULE_NAME_PREFIX + fixture.getRandomAlphabetString(8);
    String testValue = fixture.getRandomAlphabetString(10);
    String description = "Automated test rule " + ruleName;

    navigateToTargetApplicationSummary();
    fixture.clickOnRecordRelatedAction("Add Rule");
    populateAddRuleForm(ruleName, testValue, description);
    fixture.clickOnButton("Cancel");

    navigateToTargetApplicationSummary();
    int row = findRuleRowInRulesGrid(ruleName);
    assertFalse(
        row > 0,
        "Rule [" + ruleName + "] should not be saved to the Rules Grid after clicking Cancel.");
  }

  private void navigateToTargetApplicationSummary() {
    fixture.navigateToSite(IADC_SITE_URL);
    fixture.clickOnSitePage("Settings");

    int row = findApplicationRowInApplicationsGrid(TARGET_APPLICATION_NAME);
    if (row <= 0) {
      fail("Could not find Application [" + TARGET_APPLICATION_NAME
          + "] in the Applications Grid after scanning " + MAX_PAGES_TO_SCAN + " pages.");
    }
    fixture.clickOnGridColumnRow(APPLICATIONS_GRID_INDEX, "Name", "[" + row + "]");
  }

  private void populateAddRuleForm(String ruleName, String testValue, String description) {
    populateRequiredField(ruleName, "Name");
    populateRequiredDropdown(TARGET_APPLICATION_NAME, "Application");
    populateRequiredDropdown(OBJECT_TYPE_VALUE, "Object Type");
    populateRequiredDropdown(OBJECT_ATTRIBUTE_VALUE, "Object Attribute");
    populateRequiredDropdown(TEST_TYPE_VALUE, "Test Type");
    populateOptionalField(testValue, "Test Value");
    populateOptionalField(description, "Description");
  }

  private void populateRequiredField(String value, String fieldName) {
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

  private void populateRequiredDropdown(String value, String fieldName) {
    String currentValue = fixture.getFieldValue(fieldName);
    if (value.equals(currentValue)) {
      LOG.debug("SKIP POPULATE (already set) [" + fieldName + "]");
      return;
    }

    try {
      fixture.populateFieldWith(fieldName, new String[] {value});
    } catch (RuntimeException e) {
      fail("Required field [" + fieldName + "] was not editable: " + e.getMessage());
    }
  }

  private void populateOptionalField(String value, String fieldName) {
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

  private int findApplicationRowInApplicationsGrid(String applicationName) {
    for (int page = 1; page <= MAX_PAGES_TO_SCAN; page++) {
      int rowCount = fixture.getGridRowCount(APPLICATIONS_GRID_INDEX);
      for (int row = 1; row <= rowCount; row++) {
        String name =
            fixture.getGridColumnRowValue(APPLICATIONS_GRID_INDEX, "Name", "[" + row + "]");
        if (applicationName.equals(name)) {
          return row;
        }
      }

      try {
        fixture.clickOnGridNavigation(APPLICATIONS_GRID_INDEX, "next");
      } catch (RuntimeException e) {
        return -1;
      }
    }

    return -1;
  }

  private int findRuleRowInRulesGrid(String ruleName) {
    for (int page = 1; page <= MAX_PAGES_TO_SCAN; page++) {
      int rowCount = fixture.getGridRowCount(RULES_GRID_INDEX);
      for (int row = 1; row <= rowCount; row++) {
        String name = fixture.getGridColumnRowValue(RULES_GRID_INDEX, "Name", "[" + row + "]");
        if (ruleName.equals(name)) {
          return row;
        }
      }

      try {
        fixture.clickOnGridNavigation(RULES_GRID_INDEX, "next");
      } catch (RuntimeException e) {
        return -1;
      }
    }

    return -1;
  }

  private void deactivateRule(String ruleName) {
    navigateToTargetApplicationSummary();
    int row = findRuleRowInRulesGrid(ruleName);
    if (row <= 0) {
      LOG.debug("SKIP CLEANUP (rule not found) [" + ruleName + "]");
      return;
    }

    fixture.clickOnRecordActionFieldMenuAction("[" + row + "]", "Deactivate Test");
  }
}
