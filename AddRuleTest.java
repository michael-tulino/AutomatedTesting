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

// IV-198: Add a new "Add Rule" action to the Summary tab of an IADC Application that launches a
// form to create a new Test Rule, with Cancel/Submit buttons.
public class AddRuleTest {
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

  private static final String RULE_NAME_PREFIX = "IV198_";
  private static final String RULE_OBJECT_TYPE = "Expression Rule";
  private static final String RULE_OBJECT_ATTRIBUTE = "Description";
  private static final String RULE_TEST_TYPE = "Contains";
  private static final String RULE_TEST_VALUE = "Test";
  private static final String RULE_DESCRIPTION = "Automated test rule for IV-198.";

  private static final Logger LOG = LogManager.getLogger(AddRuleTest.class);
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
    navigateToApplicationSummary();

    assertTrue(
        fixture.verifyRecordRelatedActionIsPresent("Add Rule"),
        "The [Add Rule] action should be present on an IADC Application's Summary tab.");
  }

  @Test
  public void testSubmitOnAddRuleFormSavesRuleToRulesGrid() {
    String ruleName = RULE_NAME_PREFIX + fixture.getRandomAlphabetString(8);

    navigateToApplicationSummary();
    fixture.clickOnRecordRelatedAction("Add Rule");
    populateAddRuleForm(ruleName);
    fixture.clickOnButton("Submit");

    navigateToApplicationSummary();
    assertTrue(
        ruleIsPresentInRulesGrid(ruleName),
        "Rule [" + ruleName
            + "] should show up in the Rules Grid after submitting the Add Rule form.");

    deactivateRule(ruleName);
  }

  @Test
  public void testCancelOnAddRuleFormDoesNotSaveRule() {
    String ruleName = RULE_NAME_PREFIX + fixture.getRandomAlphabetString(8);

    navigateToApplicationSummary();
    fixture.clickOnRecordRelatedAction("Add Rule");
    populateAddRuleForm(ruleName);
    fixture.clickOnButton("Cancel");

    navigateToApplicationSummary();
    assertFalse(
        ruleIsPresentInRulesGrid(ruleName),
        "Rule [" + ruleName + "] should not be saved to the Rules Grid after clicking Cancel.");

    // Nothing was persisted - Cancel does not save - so no cleanup is needed.
  }

  private String navigateToApplicationSummary() {
    fixture.navigateToSite(IADC_SITE_URL);
    fixture.clickOnSitePage("Settings");
    String applicationName = fixture.getGridColumnRowValue(APPLICATIONS_GRID_INDEX, "Name", "[1]");
    fixture.clickOnRecord(applicationName);
    fixture.clickOnRecordView("Summary");
    return applicationName;
  }

  private void populateAddRuleForm(String ruleName) {
    populateRequiredField("Name", ruleName);
    // Object Type must be set before Object Attribute, which stays disabled until it is.
    populateRequiredField("Object Type", RULE_OBJECT_TYPE);
    populateRequiredField("Object Attribute", RULE_OBJECT_ATTRIBUTE);
    populateRequiredField("Test Type", RULE_TEST_TYPE);
    populateOptionalField("Test Value", RULE_TEST_VALUE);
    populateOptionalField("Description", RULE_DESCRIPTION);
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

  private void deactivateRule(String ruleName) {
    navigateToApplicationSummary();

    for (int page = 1; page <= MAX_PAGES_TO_SCAN; page++) {
      int rowCount = fixture.getGridRowCount(RULES_GRID_INDEX);
      for (int row = 1; row <= rowCount; row++) {
        String name = fixture.getGridColumnRowValue(RULES_GRID_INDEX, "Name", "[" + row + "]");
        if (ruleName.equals(name)) {
          fixture.clickOnRecordActionFieldMenuAction("[" + row + "]", "Deactivate Test");
          // Deactivate Test has no start form, so Appian shows its default confirmation dialog.
          fixture.clickOnButton("Yes");
          return;
        }
      }

      try {
        fixture.clickOnGridNavigation(RULES_GRID_INDEX, "next");
      } catch (RuntimeException e) {
        break;
      }
    }

    LOG.debug("SKIP CLEANUP (rule not found for deactivation) [" + ruleName + "]");
  }
}
