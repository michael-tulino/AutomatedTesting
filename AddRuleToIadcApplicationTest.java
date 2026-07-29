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

// IV-198: Add a new "Add Rule" action to the IADC Application summary tab that launches a form
// to create a new Test Rule, with Name/Application/Object Type/Object Attribute/Test
// Type/Test Value/Description fields and Cancel/Submit buttons.
// IV-228: Rules involving connected systems require review. Selecting Object Type = Connected
// System sets a new rule's status to "Pending Review," and the Add Rule form no longer allows
// entering a Description.
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
  private static final String APPLICATION_UNDER_TEST = "Ignyte Appian Developer Copilot";
  private static final String RULE_NAME_PREFIX = "IV198_";
  private static final String OBJECT_TYPE = "Expression Rule";
  private static final String OBJECT_ATTRIBUTE = "Name";
  private static final String TEST_TYPE = "Contains";
  private static final String CONNECTED_SYSTEM_OBJECT_TYPE = "Connected System";
  private static final String PENDING_REVIEW_STATUS = "Pending Review";

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
    navigateToApplicationSummary(APPLICATION_UNDER_TEST);

    assertTrue(
        fixture.verifyRecordRelatedActionIsPresent("Add Rule"),
        "The [Add Rule] action should be present on the [" + APPLICATION_UNDER_TEST
            + "] Application Summary.");
  }

  @Test
  public void testSubmitOnAddRuleFormSavesRuleToRulesGrid() {
    String ruleName = RULE_NAME_PREFIX + fixture.getRandomAlphabetString(8);
    String testValue = fixture.getRandomAlphabetString(10);

    navigateToApplicationSummary(APPLICATION_UNDER_TEST);
    fixture.clickOnRecordRelatedAction("Add Rule");
    populateAddRuleForm(ruleName, testValue, OBJECT_TYPE);
    fixture.clickOnButton("Submit");

    navigateToApplicationSummary(APPLICATION_UNDER_TEST);
    int ruleRow = findRuleRowByName(ruleName);
    assertTrue(
        ruleRow > 0,
        "Rule [" + ruleName + "] should show up in the Rules Grid on the ["
            + APPLICATION_UNDER_TEST + "] Application Summary after submitting the Add Rule form.");

    // Re-locate the row rather than reusing the index above, in case anything shifted the grid.
    int cleanupRow = findRuleRowByName(ruleName);
    if (cleanupRow > 0) {
      fixture.clickOnRecordActionFieldMenuAction("[" + cleanupRow + "]", "Deactivate Test");
    } else {
      fail("Could not re-locate rule [" + ruleName + "] for cleanup after confirming it was present.");
    }
  }

  @Test
  public void testCancelOnAddRuleFormDoesNotSaveRule() {
    String ruleName = RULE_NAME_PREFIX + fixture.getRandomAlphabetString(8);
    String testValue = fixture.getRandomAlphabetString(10);

    navigateToApplicationSummary(APPLICATION_UNDER_TEST);
    fixture.clickOnRecordRelatedAction("Add Rule");
    populateAddRuleForm(ruleName, testValue, OBJECT_TYPE);
    fixture.clickOnButton("Cancel");

    navigateToApplicationSummary(APPLICATION_UNDER_TEST);
    assertFalse(
        findRuleRowByName(ruleName) > 0,
        "Rule [" + ruleName + "] should not be saved to the Rules Grid after clicking Cancel.");
  }

  // IV-228: Selecting Object Type = Connected System on the Add Rule form must set the new
  // rule's status to "Pending Review" instead of the default Active status.
  @Test
  public void testAddRuleWithConnectedSystemObjectTypeSetsStatusToPendingReview() {
    String ruleName = RULE_NAME_PREFIX + fixture.getRandomAlphabetString(8);
    String testValue = fixture.getRandomAlphabetString(10);

    navigateToApplicationSummary(APPLICATION_UNDER_TEST);
    fixture.clickOnRecordRelatedAction("Add Rule");
    populateAddRuleForm(ruleName, testValue, CONNECTED_SYSTEM_OBJECT_TYPE);
    fixture.clickOnButton("Submit");

    navigateToApplicationSummary(APPLICATION_UNDER_TEST);
    int ruleRow = findRuleRowByName(ruleName);
    assertTrue(
        ruleRow > 0,
        "Rule [" + ruleName + "] should show up in the Rules Grid on the ["
            + APPLICATION_UNDER_TEST + "] Application Summary after submitting the Add Rule form.");

    String status = fixture.getGridColumnRowValue(RULES_GRID_INDEX, "Status", "[" + ruleRow + "]");
    assertTrue(
        PENDING_REVIEW_STATUS.equals(status),
        "Rule [" + ruleName + "] with Object Type [" + CONNECTED_SYSTEM_OBJECT_TYPE
            + "] should have a status of [" + PENDING_REVIEW_STATUS + "], but was [" + status
            + "].");

    // Re-locate the row rather than reusing the index above, in case anything shifted the grid.
    int cleanupRow = findRuleRowByName(ruleName);
    if (cleanupRow > 0) {
      fixture.clickOnRecordActionFieldMenuAction("[" + cleanupRow + "]", "Deactivate Test");
    } else {
      fail("Could not re-locate rule [" + ruleName + "] for cleanup after confirming it was present.");
    }
  }

  // IV-228: The Add Rule form must no longer allow entering a Description for a new rule.
  @Test
  public void testDescriptionFieldIsNotPresentOnAddRuleForm() {
    navigateToApplicationSummary(APPLICATION_UNDER_TEST);
    fixture.clickOnRecordRelatedAction("Add Rule");

    assertTrue(
        fixture.verifyFieldIsNotPresent("Description"),
        "The [Description] field should not be present on the Add Rule form.");

    fixture.clickOnButton("Cancel");
  }

  private void navigateToApplicationSummary(String applicationName) {
    fixture.navigateToSite(IADC_SITE_URL);
    fixture.clickOnSitePage("Settings");

    for (int page = 1; page <= MAX_PAGES_TO_SCAN; page++) {
      int rowCount = fixture.getGridRowCount(APPLICATIONS_GRID_INDEX);
      for (int row = 1; row <= rowCount; row++) {
        String name =
            fixture.getGridColumnRowValue(APPLICATIONS_GRID_INDEX, "Name", "[" + row + "]");
        if (applicationName.equals(name)) {
          fixture.clickOnRecord(applicationName);
          return;
        }
      }

      try {
        fixture.clickOnGridNavigation(APPLICATIONS_GRID_INDEX, "next");
      } catch (RuntimeException e) {
        fail("Did not find Application [" + applicationName + "] after scanning " + page + " pages.");
      }
    }

    fail("Did not find Application [" + applicationName + "] after scanning " + MAX_PAGES_TO_SCAN
        + " pages.");
  }

  private void populateAddRuleForm(String ruleName, String testValue, String objectType) {
    populateRequiredField("Name", ruleName);
    populateRequiredField("Application", APPLICATION_UNDER_TEST);
    populateRequiredField("Object Type", objectType);
    populateRequiredField("Object Attribute", OBJECT_ATTRIBUTE);
    populateRequiredField("Test Type", TEST_TYPE);
    populateOptionalField("Test Value", testValue);
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

  private int findRuleRowByName(String ruleName) {
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
