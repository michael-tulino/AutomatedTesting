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

// IV-198: Add a new Rule to an IADC Application from its Settings summary tab.
public class AddRuleToIadcApplicationTest {
  private static final String TEST_SITE_URL = "https://ignytedemo.appiancloud.com/suite";
  private static final String IADC_SITE_URL = "ignyte-appian-developer-copilo";
  private static final String TEST_USERNAME = "automated.tester";
  private static final String TEST_BROWSER = "CHROME";
  private static final String TEST_SITE_VERSION = "24.3";
  private static final String TEST_SITE_LOCALE = "en_US";
  private static final Integer TEST_TIMEOUT = 60;

  private static final String APPLICATION_NAME = "Isaac Sandbox";
  private static final String RULES_GRID_INDEX = "[1]";
  private static final int MAX_PAGES_TO_SCAN = 25;
  private static final String RULE_NAME_PREFIX = "IV198_";
  private static final String OBJECT_TYPE = "Expression Rule";
  private static final String OBJECT_ATTRIBUTE = "Name";
  private static final String TEST_TYPE = "Equal to";
  private static final String TEST_VALUE = "AutomatedTestValue";
  private static final String DESCRIPTION = "Automated test rule created by the IV-198 Selenium test.";

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
  public void testSubmitOnAddRuleFormSavesRuleToRulesGrid() {
    String ruleName = RULE_NAME_PREFIX + fixture.getRandomAlphabetString(8);

    navigateToAddRuleForm();
    populateAddRuleForm(ruleName);
    fixture.clickOnButton("Submit");

    navigateToApplicationSummary();
    assertTrue(
        ruleIsPresentInRulesGrid(ruleName),
        "Rule [" + ruleName + "] should show up in the Rules Grid after submitting the Add Rule form.");
  }

  @Test
  public void testCancelOnAddRuleFormDoesNotSaveRule() {
    String ruleName = RULE_NAME_PREFIX + fixture.getRandomAlphabetString(8);

    navigateToAddRuleForm();
    populateAddRuleForm(ruleName);
    fixture.clickOnButton("Cancel");

    navigateToApplicationSummary();
    assertFalse(
        ruleIsPresentInRulesGrid(ruleName),
        "Rule [" + ruleName + "] should not be saved to the Rules Grid after clicking Cancel.");
  }

  private void navigateToAddRuleForm() {
    fixture.navigateToSite(IADC_SITE_URL);
    fixture.clickOnSitePage("Settings");
    fixture.clickOnRecord(APPLICATION_NAME);
    fixture.clickOnRecordRelatedAction("Add Rule");
  }

  private void navigateToApplicationSummary() {
    fixture.navigateToSite(IADC_SITE_URL);
    fixture.clickOnSitePage("Settings");
    fixture.clickOnRecord(APPLICATION_NAME);
  }

  private void populateAddRuleForm(String ruleName) {
    populateRequiredField("Name", ruleName);
    populateRequiredField("Application", APPLICATION_NAME);
    populateRequiredField("Object Type", OBJECT_TYPE);
    populateRequiredField("Object Attribute", OBJECT_ATTRIBUTE);
    populateRequiredField("Test Type", TEST_TYPE);
    populateOptionalField("Test Value", TEST_VALUE);
    populateOptionalField("Description", DESCRIPTION);
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
      LOG.debug("SKIP POPULATE (read-only) [" + fieldName + "]");
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
}
