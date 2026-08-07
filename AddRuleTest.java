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
// to create a new IADC Object Test rule, with Cancel/Submit buttons. On Submit, the new rule
// should show up in the Rules Grid on the application's Summary tab.
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

  private static final String RULE_NAME_PREFIX = "IV198_";
  private static final String APPLICATION_VALUE = "Ignyte Appian Developer Copilot";
  private static final String OBJECT_TYPE_VALUE = "Expression Rule";
  private static final String OBJECT_ATTRIBUTE_VALUE = "Name";
  private static final String TEST_TYPE_VALUE = "Starts with";
  private static final String DESCRIPTION_VALUE =
      "Automated test rule created by IV-198 AddRuleTest.";

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
        "The [Add Rule] related action should be present on the IADC Application's Summary "
            + "tab.");

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

    // Nothing was persisted - only field presence was checked - so no cleanup is needed.
  }

  @Test
  public void testCancelOnAddRuleFormDoesNotSaveRule() {
    String ruleName = RULE_NAME_PREFIX + fixture.getRandomAlphabetString(8);

    navigateToApplicationSummary();
    fixture.clickOnRecordRelatedAction("Add Rule");
    populateRequiredField("Name", ruleName);
    fixture.clickOnButton("Cancel");

    int rowIndex = findRuleRowIndex(ruleName);
    assertEquals(
        -1,
        rowIndex,
        "Rule [" + ruleName + "] should not be saved to the Rules Grid after clicking Cancel.");

    // Nothing was persisted - Cancel does not save - so no cleanup is needed.
  }

  @Test
  public void testSubmitOnAddRuleFormSavesRuleToRulesGrid() {
    String ruleName = RULE_NAME_PREFIX + fixture.getRandomAlphabetString(8);

    navigateToApplicationSummary();
    fixture.clickOnRecordRelatedAction("Add Rule");
    populateRequiredField("Name", ruleName);
    populateRequiredField("Application", APPLICATION_VALUE);
    populateRequiredField("Object Type", OBJECT_TYPE_VALUE);
    populateRequiredField("Object Attribute", OBJECT_ATTRIBUTE_VALUE);
    populateRequiredField("Test Type", TEST_TYPE_VALUE);
    populateRequiredField("Test Value", ruleName);
    populateRequiredField("Description", DESCRIPTION_VALUE);
    fixture.clickOnButton("Submit");

    int rowIndex = findRuleRowIndex(ruleName);
    assertTrue(
        rowIndex > 0,
        "Rule [" + ruleName
            + "] should show up in the Rules Grid after submitting the Add Rule form.");

    // Re-locate the row fresh right before deactivating it - do not reuse the row index found
    // above, since the grid's contents/paging could have changed since then.
    int freshRowIndex = findRuleRowIndex(ruleName);
    assertTrue(
        freshRowIndex > 0,
        "Rule [" + ruleName + "] should still be locatable in the Rules Grid for cleanup.");
    fixture.clickOnRecordActionFieldMenuAction("[" + freshRowIndex + "]", "Deactivate Test");

    // "Deactivate Test" is the only available cleanup mechanism for an IADC Object Test rule (no
    // delete exists). It sets the rule's status to Inactive but does not remove it from the Rules
    // Grid.
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

  // Re-navigates to the Rules Grid fresh (grids reset to page 1 on a fresh page load) and pages
  // through it looking for a rule with the given Name. Returns the row's 1-based position on the
  // currently-rendered grid page if found (leaving the grid on that page), or -1 if not found
  // after scanning MAX_PAGES_TO_SCAN pages.
  private int findRuleRowIndex(String ruleName) {
    navigateToApplicationSummary();

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
}
