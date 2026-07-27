package autogen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.appiancorp.ps.automatedtest.exception.TimeoutTestException;
import com.appiancorp.ps.automatedtest.fixture.SitesFixture;

public class AddRuleToIsaacSandboxTest {

  protected static final String TEST_BROWSER = "CHROME";
  protected static final String TEST_SITE_VERSION = "24.3";
  protected static final String TEST_SITE_URL = "https://ignytedemo.appiancloud.com/suite";
  protected static final String IADC_SITE_URL = "ignyte-appian-developer-copilo";
  protected static final String TEST_SITE_LOCALE = "en_US";
  protected static final String TEST_USERNAME = "automated.tester";
  protected static final Integer TEST_TIMEOUT = 60;

  private static final String APPLICATION_NAME = "Isaac Sandbox";
  private static final String RULES_GRID = "[1]";
  private static final int PAGE_RESET_ATTEMPT_LIMIT = 25;

  private static SitesFixture fixture;

  @BeforeAll
  public static void setUp() {
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
  public static void tearDown() {
    fixture.tearDown();
  }

  private static void navigateToApplicationSummary() {
    fixture.navigateToSite(IADC_SITE_URL);
    fixture.clickOnSitePage("Settings");
    fixture.clickOnRecord(APPLICATION_NAME);
  }

  private static void populateAddRuleForm(String ruleName, String testValue, String description) {
    fixture.populateFieldWith("Name", new String[] { ruleName });
    fixture.populateFieldWith("Application", new String[] { APPLICATION_NAME });
    fixture.populateFieldWith("Object Type", new String[] { "Expression Rule" });
    fixture.populateFieldWith("Object Attribute", new String[] { "Name" });
    fixture.populateFieldWith("Test Type", new String[] { "Contains" });
    fixture.populateFieldWith("Test Value", new String[] { testValue });
    fixture.populateFieldWith("Description", new String[] { description });
  }

  /**
   * Returns null (rather than throwing) when no page contains a matching Name cell.
   */
  private static String locateRuleRowByName(String ruleName) {
    int totalCount = fixture.getGridTotalCount(RULES_GRID);
    int rowCount = fixture.getGridRowCount(RULES_GRID);

    if (totalCount > rowCount) {
      int resetAttempts = 0;
      while (resetAttempts < PAGE_RESET_ATTEMPT_LIMIT) {
        try {
          fixture.clickOnGridNavigation(RULES_GRID, "previous");
          resetAttempts++;
        } catch (TimeoutTestException e) {
          break;
        }
      }
      rowCount = fixture.getGridRowCount(RULES_GRID);
    }

    int seen = 0;
    while (true) {
      for (int row = 1; row <= rowCount; row++) {
        String name = fixture.getGridColumnRowValue(RULES_GRID, "Name", String.valueOf(row));
        if (ruleName.equals(name)) {
          return String.valueOf(row);
        }
      }
      seen += rowCount;
      if (seen >= totalCount) {
        return null;
      }
      fixture.clickOnGridNavigation(RULES_GRID, "next");
      rowCount = fixture.getGridRowCount(RULES_GRID);
    }
  }

  @Test
  public void testAddRuleFormFieldsAndSubmitAddsRuleToRulesGrid() {
    navigateToApplicationSummary();

    assertTrue(fixture.verifyRecordRelatedActionIsPresent("Add Rule"),
        "Add Rule action should be present on the application summary");
    fixture.clickOnRecordRelatedAction("Add Rule");

    assertTrue(fixture.verifyFieldIsPresent("Name"), "Name field should be present");
    assertTrue(fixture.verifyFieldIsPresent("Application"), "Application field should be present");
    assertTrue(fixture.verifyFieldIsPresent("Object Type"), "Object Type field should be present");
    assertTrue(fixture.verifyFieldIsPresent("Object Attribute"), "Object Attribute field should be present");
    assertTrue(fixture.verifyFieldIsPresent("Test Type"), "Test Type field should be present");
    assertTrue(fixture.verifyFieldIsPresent("Test Value"), "Test Value field should be present");
    assertTrue(fixture.verifyFieldIsPresent("Description"), "Description field should be present");
    assertTrue(fixture.verifyButtonIsPresent("Submit"), "Submit button should be present");
    assertTrue(fixture.verifyButtonIsPresent("Cancel"), "Cancel button should be present");

    String ruleName = "IV198_" + fixture.getRandomAlphabetString(8);
    String testValue = "TestValue_" + fixture.getRandomAlphabetString(5);
    String description = "IV-198 automated test rule";
    populateAddRuleForm(ruleName, testValue, description);

    fixture.clickOnButton("Submit");

    String row = locateRuleRowByName(ruleName);
    assertNotNull(row, "Submitted rule '" + ruleName + "' should appear in the Rules grid");
    assertEquals(APPLICATION_NAME, fixture.getGridColumnRowValue(RULES_GRID, "Application", row));
    assertEquals("Expression Rule", fixture.getGridColumnRowValue(RULES_GRID, "Object Type", row));
    assertEquals("Name", fixture.getGridColumnRowValue(RULES_GRID, "Attribute", row));
    assertEquals("Contains", fixture.getGridColumnRowValue(RULES_GRID, "Operator", row));
    assertEquals(testValue, fixture.getGridColumnRowValue(RULES_GRID, "Operand", row));
  }

  @Test
  public void testCancelOnAddRuleFormDoesNotSaveRule() {
    navigateToApplicationSummary();

    fixture.clickOnRecordRelatedAction("Add Rule");

    String ruleName = "IV198_" + fixture.getRandomAlphabetString(8);
    String testValue = "TestValue_" + fixture.getRandomAlphabetString(5);
    String description = "IV-198 cancel test rule";
    populateAddRuleForm(ruleName, testValue, description);

    fixture.clickOnButton("Cancel");

    assertTrue(fixture.verifyButtonIsNotPresent("Submit"), "Add Rule form should close after Cancel");

    String row = locateRuleRowByName(ruleName);
    assertNull(row, "Cancelled rule '" + ruleName + "' should not appear in the Rules grid");
  }
}
