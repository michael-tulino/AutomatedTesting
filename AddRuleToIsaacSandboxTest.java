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

public class AddRuleToIsaacSandboxTest {
  private static final String TEST_SITE_URL = "https://ignytedemo.appiancloud.com/suite";
  private static final String IADC_SITE_URL = "ignyte-appian-developer-copilo";
  private static final String TEST_USERNAME = "automated.tester";
  private static final String TEST_BROWSER = "CHROME";
  private static final String TEST_SITE_VERSION = "24.3";
  private static final String TEST_SITE_LOCALE = "en_US";
  private static final Integer TEST_TIMEOUT = 60;

  private static final String APPLICATION_NAME = "Isaac Sandbox";
  private static final String RULES_GRID_NAME = "[1]";
  private static final String RULE_NAME_PREFIX = "IV198_";
  private static final String OBJECT_TYPE = "Constant";
  private static final String OBJECT_ATTRIBUTE = "Name";
  private static final String TEST_TYPE = "Equal to";
  private static final String TEST_VALUE = "AutomatedTestValue";
  private static final String TEST_DESCRIPTION = "Automated test rule created by the IV-198 Selenium test.";

  private static final Logger LOG = LogManager.getLogger(AddRuleToIsaacSandboxTest.class);
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
  public void testAddRuleFormFieldsAndSubmitAddsRuleToRulesGrid() {
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
    populateFieldIfEditable("Name", ruleName, true);
    populateFieldIfEditable("Application", APPLICATION_NAME, false);
    populateFieldIfEditable("Object Type", OBJECT_TYPE, true);
    populateFieldIfEditable("Object Attribute", OBJECT_ATTRIBUTE, true);
    populateFieldIfEditable("Test Type", TEST_TYPE, true);
    populateFieldIfEditable("Test Value", TEST_VALUE, true);
    populateFieldIfEditable("Description", TEST_DESCRIPTION, true);
  }

  private void populateFieldIfEditable(String fieldName, String value, boolean requiredEditable) {
    String currentValue = fixture.getFieldValue(fieldName);
    if (value.equals(currentValue)) {
      LOG.debug("SKIP POPULATE (already set) [" + fieldName + "]");
      return;
    }

    try {
      fixture.populateFieldWithValue(fieldName, value);
    } catch (IllegalArgumentException e) {
      if (requiredEditable) {
        fail("Required field [" + fieldName + "] was not editable: " + e.getMessage());
      }
      LOG.debug("SKIP POPULATE (read-only) [" + fieldName + "]");
    }
  }

  private boolean ruleIsPresentInRulesGrid(String ruleName) {
    boolean canGoToPreviousPage = true;
    while (canGoToPreviousPage) {
      try {
        fixture.clickOnGridNavigation(RULES_GRID_NAME, "previous");
      } catch (RuntimeException e) {
        canGoToPreviousPage = false;
      }
    }

    while (true) {
      int rowCount = fixture.getGridRowCount(RULES_GRID_NAME);
      for (int row = 1; row <= rowCount; row++) {
        String name = fixture.getGridColumnRowValue(RULES_GRID_NAME, "Name", "[" + row + "]");
        if (ruleName.equals(name)) {
          return true;
        }
      }

      try {
        fixture.clickOnGridNavigation(RULES_GRID_NAME, "next");
      } catch (RuntimeException e) {
        return false;
      }
    }
  }
}
