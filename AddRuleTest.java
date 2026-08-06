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

// IV-198: Add a new "Add Rule" action to the summary tab for an IADC application that launches
// a form to create a new IADC Object Test (Rule), with Cancel/Submit buttons. Submitting should
// show the new rule in the Rules Grid on the application's summary tab.
public class AddRuleTest {
  private static final String TEST_SITE_URL = "https://ignytedemo.appiancloud.com/suite";
  private static final String IADC_SITE_URL = "ignyte-appian-developer-copilo";
  private static final String TEST_USERNAME = "automated.tester";
  private static final String TEST_BROWSER = "CHROME";
  private static final String TEST_SITE_VERSION = "24.3";
  private static final String TEST_SITE_LOCALE = "en_US";
  private static final Integer TEST_TIMEOUT = 60;

  private static final String APPLICATION_NAME = "Ignyte Appian Developer Copilot";
  private static final String RULES_GRID_INDEX = "[1]";
  private static final int MAX_PAGES_TO_SCAN = 25;

  private static final String RULE_NAME_PREFIX = "IV198_";
  private static final String OBJECT_TYPE_VALUE = "Connected System";
  private static final String OBJECT_ATTRIBUTE_VALUE = "Name";
  private static final String TEST_TYPE_VALUE = "Equal to";

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
  public void testAddRuleActionLaunchesFormWithExpectedFields() {
    navigateToApplicationSummary();

    assertTrue(
        fixture.verifyRecordRelatedActionIsPresent("Add Rule"),
        "The [Add Rule] action should be present on the IADC Application's summary tab.");

    fixture.clickOnRecordRelatedAction("Add Rule");

    assertTrue(
        fixture.verifyFieldIsPresent("Name"),
        "The [Name] field should be present on the Add Rule form.");
    assertTrue(
        fixture.verifyFieldIsPresent("Application"),
        "The [Application] field should be present on the Add Rule form.");
    assertTrue(
        fixture.verifyFieldIsPresent("Object Type"),
        "The [Object Type] field should be present on the Add Rule form.");
    assertTrue(
        fixture.verifyFieldIsPresent("Object Attribute"),
        "The [Object Attribute] field should be present on the Add Rule form.");
    assertTrue(
        fixture.verifyFieldIsPresent("Test Type"),
        "The [Test Type] field should be present on the Add Rule form.");
    assertTrue(
        fixture.verifyFieldIsPresent("Test Value"),
        "The [Test Value] field should be present on the Add Rule form.");
    assertTrue(
        fixture.verifyFieldIsPresent("Description"),
        "The [Description] field should be present on the Add Rule form.");

    fixture.clickOnButton("Cancel");
  }

  @Test
  public void testCancelOnAddRuleFormDoesNotSaveRule() {
    String ruleName = RULE_NAME_PREFIX + fixture.getRandomAlphabetString(8);

    navigateToApplicationSummary();
    fixture.clickOnRecordRelatedAction("Add Rule");
    populateRequiredField("Name", ruleName);
    populateOptionalField("Object Type", OBJECT_TYPE_VALUE);
    populateRequiredField("Object Attribute", OBJECT_ATTRIBUTE_VALUE);
    populateRequiredField("Test Type", TEST_TYPE_VALUE);
    fixture.clickOnButton("Cancel");

    assertFalse(
        findRuleRow(ruleName) != null,
        "Rule [" + ruleName + "] should not be saved to the Rules Grid after clicking Cancel.");

    // Nothing was persisted - Cancel does not save - so no cleanup is needed.
  }

  @Test
  public void testSubmitOnAddRuleFormSavesRuleToRulesGrid() {
    String ruleName = RULE_NAME_PREFIX + fixture.getRandomAlphabetString(8);
    String testValue = fixture.getRandomAlphabetString(10);

    navigateToApplicationSummary();
    fixture.clickOnRecordRelatedAction("Add Rule");
    populateRequiredField("Name", ruleName);
    populateRequiredField("Application", APPLICATION_NAME);
    populateOptionalField("Object Type", OBJECT_TYPE_VALUE);
    populateRequiredField("Object Attribute", OBJECT_ATTRIBUTE_VALUE);
    populateRequiredField("Test Type", TEST_TYPE_VALUE);
    populateOptionalField("Test Value", testValue);
    fixture.clickOnButton("Submit");

    String rowIndex = findRuleRow(ruleName);
    assertTrue(
        rowIndex != null,
        "Rule [" + ruleName
            + "] should show up in the Rules Grid on the IADC Application's summary tab after "
            + "submitting the Add Rule form.");

    assertTrue(
        fixture.verifyGridColumnRowContainsValue(
            RULES_GRID_INDEX, "Application", rowIndex, APPLICATION_NAME),
        "Rule [" + ruleName + "] should show Application [" + APPLICATION_NAME
            + "] in the Rules Grid.");
    assertTrue(
        fixture.verifyGridColumnRowContainsValue(
            RULES_GRID_INDEX, "Attribute", rowIndex, OBJECT_ATTRIBUTE_VALUE),
        "Rule [" + ruleName + "] should show Attribute [" + OBJECT_ATTRIBUTE_VALUE
            + "] in the Rules Grid.");
    assertTrue(
        fixture.verifyGridColumnRowContainsValue(
            RULES_GRID_INDEX, "Operator", rowIndex, TEST_TYPE_VALUE),
        "Rule [" + ruleName + "] should show Operator [" + TEST_TYPE_VALUE
            + "] in the Rules Grid.");
    assertTrue(
        fixture.verifyGridColumnRowContainsValue(
            RULES_GRID_INDEX, "Operand", rowIndex, testValue),
        "Rule [" + ruleName + "] should show Operand [" + testValue + "] in the Rules Grid.");

    deactivateRule(ruleName);
  }

  private void navigateToApplicationSummary() {
    fixture.navigateToSite(IADC_SITE_URL);
    fixture.clickOnSitePage("Settings");
    fixture.clickOnRecord(APPLICATION_NAME);
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

  private String findRuleRow(String ruleName) {
    navigateToApplicationSummary();

    for (int page = 1; page <= MAX_PAGES_TO_SCAN; page++) {
      int rowCount = fixture.getGridRowCount(RULES_GRID_INDEX);
      for (int row = 1; row <= rowCount; row++) {
        String name = fixture.getGridColumnRowValue(RULES_GRID_INDEX, "Name", "[" + row + "]");
        if (ruleName.equals(name)) {
          return "[" + row + "]";
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

  private void deactivateRule(String ruleName) {
    String rowIndex = findRuleRow(ruleName);
    if (rowIndex == null) {
      fail("Could not locate rule [" + ruleName + "] in the Rules Grid to deactivate it during "
          + "cleanup.");
      return;
    }

    // The Rules Grid's per-row action menu is a distinct record action field for each visible
    // row, rendered after the single "Quick Actions" record action field in the sidebar - so a
    // row's menu is at page-wide record-action-field index (row index + 1).
    int rowNumber = Integer.parseInt(rowIndex.replaceAll("[\\[\\]]", ""));
    fixture.clickOnRecordActionFieldMenuAction("[" + (rowNumber + 1) + "]", "Deactivate Test");
  }
}
