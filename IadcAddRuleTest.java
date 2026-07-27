package autogen;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.appiancorp.ps.automatedtest.fixture.SitesFixture;

public class IadcAddRuleTest {

  private static final Logger LOG = LogManager.getLogger(IadcAddRuleTest.class);

  private static final String TEST_SITE_URL = "https://ignytedemo.appiancloud.com/suite";
  private static final String IADC_SITE_URL = "ignyte-appian-developer-copilo";
  private static final String TEST_USERNAME = "automated.tester";
  private static final String TEST_BROWSER = "CHROME";
  private static final String TEST_SITE_VERSION = "24.3";
  private static final String TEST_SITE_LOCALE = "en_US";
  private static final Integer TEST_TIMEOUT = 60;

  private static final String APPLICATION_RECORD_NAME = "Isaac Sandbox";
  private static final String RULES_GRID = "[1]";

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
    fixture.navigateToSite(IADC_SITE_URL);
  }

  @AfterAll
  public static void tearDownFixture() {
    fixture.tearDown();
  }

  @Test
  public void testAddRuleFormFieldsAndSubmitAddsRuleToRulesGrid() {
    navigateToApplicationSummary();
    assertTrue(fixture.verifyRecordRelatedActionIsPresent("Add Rule"));
    fixture.clickOnRecordRelatedAction("Add Rule");

    assertTrue(fixture.verifyFieldIsPresent("Name"));
    assertTrue(fixture.verifyFieldIsPresent("Application"));
    assertTrue(fixture.verifyFieldIsPresent("Object Type"));
    assertTrue(fixture.verifyFieldIsPresent("Object Attribute"));
    assertTrue(fixture.verifyFieldIsPresent("Test Type"));
    assertTrue(fixture.verifyFieldIsPresent("Test Value"));
    assertTrue(fixture.verifyFieldIsPresent("Description"));
    assertTrue(fixture.verifyButtonIsPresent("Submit"));
    assertTrue(fixture.verifyButtonIsPresent("Cancel"));

    String ruleName = "IADC Add Rule Test " + fixture.getRandomAlphabetString(8);

    fixture.populateFieldWith("Name", new String[] { ruleName });
    fixture.populateFieldWith("Object Type", new String[] { "Expression Rule" });
    fixture.populateFieldWith("Object Attribute", new String[] { "Name" });
    fixture.populateFieldWith("Test Type", new String[] { "Equal to" });
    fixture.populateFieldWith("Test Value", new String[] { "AutomatedRuleValue" });
    fixture.populateFieldWith("Description", new String[] { "Created by automated Selenium test suite." });

    String currentApplicationValue = fixture.getFieldValue("Application");
    if (!APPLICATION_RECORD_NAME.equals(currentApplicationValue)) {
      fixture.populateFieldWith("Application", new String[] { APPLICATION_RECORD_NAME });
    }

    fixture.clickOnButton("Submit");

    int row = findRuleRowByName(ruleName);
    assertTrue(row > 0, "Expected rule \"" + ruleName + "\" to appear in the Rules grid after submitting the Add Rule form, but it was not found.");
    assertTrue(fixture.verifyGridColumnRowContains(RULES_GRID, "Application", String.valueOf(row), new String[] { APPLICATION_RECORD_NAME }));
    assertTrue(fixture.verifyGridColumnRowContains(RULES_GRID, "Object Type", String.valueOf(row), new String[] { "Expression Rule" }));
    assertTrue(fixture.verifyGridColumnRowContains(RULES_GRID, "Attribute", String.valueOf(row), new String[] { "Name" }));
    assertTrue(fixture.verifyGridColumnRowContains(RULES_GRID, "Operator", String.valueOf(row), new String[] { "Equal to" }));
    assertTrue(fixture.verifyGridColumnRowContains(RULES_GRID, "Operand", String.valueOf(row), new String[] { "AutomatedRuleValue" }));

    int cleanupRow = findRuleRowByName(ruleName);
    if (cleanupRow > 0) {
      fixture.clickOnRecordActionFieldMenuAction(String.valueOf(cleanupRow), "Deactivate Test");
    } else {
      LOG.warn("Could not re-locate rule \"" + ruleName + "\" in the Rules grid for cleanup; it may require manual deactivation.");
    }
  }

  @Test
  public void testCancelOnAddRuleFormDoesNotSaveRule() {
    navigateToApplicationSummary();
    fixture.clickOnRecordRelatedAction("Add Rule");

    String ruleName = "IADC Cancel Rule Test " + fixture.getRandomAlphabetString(8);
    fixture.populateFieldWith("Name", new String[] { ruleName });

    fixture.clickOnButton("Cancel");

    assertTrue(fixture.verifyButtonIsNotPresent("Submit"));

    int row = findRuleRowByName(ruleName);
    assertEquals(-1, row, "Expected rule \"" + ruleName + "\" NOT to appear in the Rules grid after clicking Cancel, but it was found.");
  }

  private void navigateToApplicationSummary() {
    fixture.clickOnMenu("Records");
    fixture.clickOnRecordType("Applications");
    fixture.clickOnRecord(APPLICATION_RECORD_NAME);
  }

  private void resetRulesGridToFirstPage() {
    boolean canGoBack = true;
    while (canGoBack) {
      try {
        fixture.clickOnGridNavigation(RULES_GRID, "previous");
      } catch (RuntimeException e) {
        canGoBack = false;
      }
    }
  }

  private int findRuleRowByName(String ruleName) {
    resetRulesGridToFirstPage();
    while (true) {
      int rowCount = fixture.getGridRowCount(RULES_GRID);
      for (int row = 1; row <= rowCount; row++) {
        String name = fixture.getGridColumnRowValue(RULES_GRID, "Name", String.valueOf(row));
        if (ruleName.equals(name)) {
          return row;
        }
      }
      try {
        fixture.clickOnGridNavigation(RULES_GRID, "next");
      } catch (RuntimeException e) {
        return -1;
      }
    }
  }
}
