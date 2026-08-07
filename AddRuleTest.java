package autogen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.appiancorp.ps.automatedtest.fixture.SitesFixture;

// IV-198: Add a new "Add Rule" action to an IADC Application's Summary tab that launches a form
// to create a new IADC Object Test rule, with Cancel/Submit buttons.
public class AddRuleTest {
  private static final String TEST_SITE_URL = "https://ignytedemo.appiancloud.com/suite";
  private static final String IADC_SITE_URL = "ignyte-appian-developer-copilo";
  private static final String TEST_USERNAME = "automated.tester";
  private static final String TEST_BROWSER = "CHROME";
  private static final String TEST_SITE_VERSION = "24.3";
  private static final String TEST_SITE_LOCALE = "en_US";
  private static final Integer TEST_TIMEOUT = 60;

  private static final String IADC_APPLICATION_RECORD_NAME = "Ignyte Appian Developer Copilot";
  private static final String RULES_GRID_INDEX = "[1]";
  private static final int MAX_PAGES_TO_SCAN = 30;
  private static final int NOT_FOUND = -1;

  private static final String RULE_NAME_PREFIX = "IV198_";
  private static final String OBJECT_TYPE_VALUE = "Interface";
  private static final String OBJECT_ATTRIBUTE_VALUE = "Name";
  private static final String TEST_TYPE_VALUE = "Starts with";
  private static final String TEST_VALUE_VALUE = "IV198";
  private static final String DESCRIPTION_VALUE = "IV-198 automated test rule.";

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
        "The [Add Rule] related action should be present on the Application's Summary tab.");

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

    // Nothing was persisted - this test only opens the form and inspects its fields - so no
    // cleanup is needed.
  }

  @Test
  public void testCancelOnAddRuleFormDoesNotSaveRule() {
    String ruleName = RULE_NAME_PREFIX + fixture.getRandomAlphabetString(8);

    navigateToApplicationSummary();
    fixture.clickOnRecordRelatedAction("Add Rule");
    populateRequiredField("Name", ruleName);
    fixture.clickOnButton("Cancel");

    navigateToApplicationSummary();
    int rowIndex = locateRuleRowIndexInRulesGrid(ruleName);
    assertEquals(
        NOT_FOUND,
        rowIndex,
        "Rule [" + ruleName + "] should not appear in the Rules Grid after clicking Cancel.");

    // Nothing was persisted - Cancel does not save - so no cleanup is needed.
  }

  @Test
  public void testSubmitOnAddRuleFormSavesRuleToRulesGrid() {
    String ruleName = RULE_NAME_PREFIX + fixture.getRandomAlphabetString(8);

    navigateToApplicationSummary();
    fixture.clickOnRecordRelatedAction("Add Rule");
    populateRequiredField("Name", ruleName);
    populateRequiredField("Application", IADC_APPLICATION_RECORD_NAME);
    populateRequiredField("Object Type", OBJECT_TYPE_VALUE);
    populateRequiredField("Object Attribute", OBJECT_ATTRIBUTE_VALUE);
    populateRequiredField("Test Type", TEST_TYPE_VALUE);
    populateOptionalField("Test Value", TEST_VALUE_VALUE);
    populateOptionalField("Description", DESCRIPTION_VALUE);
    fixture.clickOnButton("Submit");

    navigateToApplicationSummary();
    int rowIndex = locateRuleRowIndexInRulesGrid(ruleName);
    assertTrue(
        rowIndex != NOT_FOUND,
        "Rule [" + ruleName + "] should show up in the Rules Grid after submitting the Add Rule "
            + "form.");

    // Re-locate the rule fresh for cleanup rather than reusing the row index found above - its
    // position in the grid may have changed since then.
    navigateToApplicationSummary();
    int cleanupRowIndex = locateRuleRowIndexInRulesGrid(ruleName);
    if (cleanupRowIndex == NOT_FOUND) {
      fail("Could not re-locate rule [" + ruleName + "] in the Rules Grid for cleanup after "
          + "scanning " + MAX_PAGES_TO_SCAN + " pages.");
    }
    fixture.clickOnRecordActionFieldMenuAction(String.valueOf(cleanupRowIndex), "Deactivate Test");
  }

  private void navigateToApplicationSummary() {
    fixture.navigateToSite(IADC_SITE_URL);
    fixture.clickOnSitePage("Settings");
    fixture.clickOnRecord(IADC_APPLICATION_RECORD_NAME);
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

  private int locateRuleRowIndexInRulesGrid(String ruleName) {
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
        return NOT_FOUND;
      }
    }

    return NOT_FOUND;
  }
}
