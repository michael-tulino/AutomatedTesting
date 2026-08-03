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

// IV-198: Add a new "Add Rule" action to the summary tab of an IADC Application that launches
// a form to create a new IADC Object Test rule against that Application, with Cancel/Submit
// buttons.
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

  private static final String ADD_RULE_ACTION_NAME = "Add Rule";
  private static final String DEACTIVATE_TEST_ACTION_NAME = "Deactivate Test";

  private static final String TEST_NAME_PREFIX = "IV198_";
  private static final String OBJECT_TYPE_NAME = "Expression Rule";
  private static final String OBJECT_ATTRIBUTE_NAME = "Name";
  private static final String TEST_TYPE_NAME = "Contains";
  private static final String TEST_VALUE = "Test";
  private static final String TEST_DESCRIPTION = "IV-198 automated test rule.";

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
    String applicationName = getFirstApplicationName();
    navigateToApplicationSummary(applicationName);

    assertTrue(
        fixture.verifyRecordRelatedActionIsPresent(ADD_RULE_ACTION_NAME),
        "The [" + ADD_RULE_ACTION_NAME + "] action should be present on the ["
            + applicationName + "] Application's Summary tab.");
  }

  @Test
  public void testAddRuleFormHasExpectedFields() {
    String applicationName = getFirstApplicationName();
    navigateToApplicationSummary(applicationName);
    fixture.clickOnRecordRelatedAction(ADD_RULE_ACTION_NAME);

    assertTrue(fixture.verifyFieldIsPresent("Name"), "The Add Rule form should have a [Name] field.");
    assertTrue(
        fixture.verifyFieldIsPresent("Application"),
        "The Add Rule form should have an [Application] field.");
    assertTrue(
        fixture.verifyFieldIsPresent("Object Type"),
        "The Add Rule form should have an [Object Type] field.");
    assertTrue(
        fixture.verifyFieldIsPresent("Object Attribute"),
        "The Add Rule form should have an [Object Attribute] field.");
    assertTrue(
        fixture.verifyFieldIsPresent("Test Type"),
        "The Add Rule form should have a [Test Type] field.");
    assertTrue(
        fixture.verifyFieldIsPresent("Test Value"),
        "The Add Rule form should have a [Test Value] field.");
    assertTrue(
        fixture.verifyFieldIsPresent("Description"),
        "The Add Rule form should have a [Description] field.");
  }

  @Test
  public void testSubmitOnAddRuleFormSavesRuleToRulesGrid() {
    String applicationName = getFirstApplicationName();
    String testName = TEST_NAME_PREFIX + fixture.getRandomAlphabetString(8);

    navigateToApplicationSummary(applicationName);
    fixture.clickOnRecordRelatedAction(ADD_RULE_ACTION_NAME);
    populateAddRuleForm(testName, applicationName);
    fixture.clickOnButton("Submit");

    navigateToApplicationSummary(applicationName);
    int row = findRuleRowByName(testName);
    assertTrue(
        row != -1,
        "Rule [" + testName + "] should show up in the Rules Grid on the [" + applicationName
            + "] Application's Summary tab after submitting the Add Rule form.");
    assertEquals(
        OBJECT_TYPE_NAME,
        fixture.getGridColumnRowValue(RULES_GRID_INDEX, "Object Type", "[" + row + "]"),
        "Rule [" + testName + "] should show the Object Type selected on the Add Rule form.");
    assertEquals(
        TEST_VALUE,
        fixture.getGridColumnRowValue(RULES_GRID_INDEX, "Operand", "[" + row + "]"),
        "Rule [" + testName + "] should show the Test Value entered on the Add Rule form.");

    // Cleanup: deactivate the created rule via its row's action menu. Record action fields on
    // this page are numbered in page order, and the "Quick Actions" sidebar field (containing
    // Add Rule/Update Application/Clone Test Rules) renders before the Rules Grid, so this row's
    // menu is one position past its row number.
    fixture.clickOnRecordActionFieldMenuAction(String.valueOf(row + 1), DEACTIVATE_TEST_ACTION_NAME);
  }

  @Test
  public void testCancelOnAddRuleFormDoesNotSaveRule() {
    String applicationName = getFirstApplicationName();
    String testName = TEST_NAME_PREFIX + fixture.getRandomAlphabetString(8);

    navigateToApplicationSummary(applicationName);
    fixture.clickOnRecordRelatedAction(ADD_RULE_ACTION_NAME);
    populateAddRuleForm(testName, applicationName);
    fixture.clickOnButton("Cancel");

    navigateToApplicationSummary(applicationName);
    assertEquals(
        -1,
        findRuleRowByName(testName),
        "Rule [" + testName + "] should not be saved to the Rules Grid after clicking Cancel.");
  }

  private String getFirstApplicationName() {
    navigateToSettingsTab();
    return fixture.getGridColumnRowValue(APPLICATIONS_GRID_INDEX, "Name", "[1]");
  }

  private void navigateToSettingsTab() {
    fixture.navigateToSite(IADC_SITE_URL);
    fixture.clickOnSitePage("Settings");
  }

  private void navigateToApplicationSummary(String applicationName) {
    navigateToSettingsTab();
    fixture.clickOnRecord(applicationName);
    fixture.clickOnRecordView("Summary");
  }

  private void populateAddRuleForm(String testName, String applicationName) {
    populateRequiredField("Name", testName);
    populateRequiredField("Object Type", OBJECT_TYPE_NAME);
    populateRequiredField("Object Attribute", OBJECT_ATTRIBUTE_NAME);
    populateRequiredField("Test Type", TEST_TYPE_NAME);
    populateRequiredField("Application", applicationName);
    populateOptionalField("Test Value", TEST_VALUE);
    populateOptionalField("Description", TEST_DESCRIPTION);
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

  private int findRuleRowByName(String testName) {
    for (int page = 1; page <= MAX_PAGES_TO_SCAN; page++) {
      int rowCount = fixture.getGridRowCount(RULES_GRID_INDEX);
      for (int row = 1; row <= rowCount; row++) {
        String name = fixture.getGridColumnRowValue(RULES_GRID_INDEX, "Name", "[" + row + "]");
        if (testName.equals(name)) {
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
