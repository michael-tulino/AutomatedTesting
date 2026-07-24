package autogen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.appiancorp.ps.automatedtest.fixture.SitesFixture;

public class AddRuleToIsaacSandboxTest {

  protected static String TEST_BROWSER = "CHROME";
  protected static String TEST_SITE_VERSION = "24.3";
  protected static String TEST_SITE_URL = "https://ignytedemo.appiancloud.com/suite";
  protected static String IADC_SITE_URL = "ignyte-appian-developer-copilo";
  protected static String TEST_SITE_LOCALE = "en_US";
  protected static String TEST_USERNAME = "automated.tester";
  protected static Integer TEST_TIMEOUT = 60;

  private static final String RULES_GRID = "[1]";
  private static final String APPLICATION_NAME = "Isaac Sandbox";
  private static final String RULE_NAME = "Expression Rules Have 1+ Automated Tests";
  private static final String OBJECT_TYPE = "Expression Rule";
  private static final String OBJECT_ATTRIBUTE = "Unit Test Count";
  private static final String TEST_TYPE = "> (Greater Than)";
  private static final String TEST_VALUE = "0";

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
    fixture.navigateToSite(IADC_SITE_URL);
  }

  @AfterAll
  public static void teardown() {
    fixture.tearDown();
  }

  @Test
  public void addRuleSavesWithoutValidationErrorAndAppearsInRulesList() {
    fixture.clickOnSitePage("Settings");
    fixture.clickOnRecord(APPLICATION_NAME);
    fixture.clickOnRecordRelatedAction("Add Rule");

    fixture.populateFieldWith("Name", new String[] {RULE_NAME});
    fixture.populateFieldWith("Object Type", new String[] {OBJECT_TYPE});
    fixture.populateFieldWith("Application", new String[] {APPLICATION_NAME});
    fixture.populateFieldWith("Object Attribute", new String[] {OBJECT_ATTRIBUTE});
    fixture.populateFieldWith("Test Type", new String[] {TEST_TYPE});
    fixture.populateFieldWith("Test Value", new String[] {TEST_VALUE});
    fixture.populateFieldWith("Description", new String[] {RULE_NAME});

    fixture.clickOnButton("Submit");

    String ruleRow = locateRuleRow();
    assertEquals(RULE_NAME, fixture.getGridColumnRowValue(RULES_GRID, "Name", ruleRow));
    assertEquals(APPLICATION_NAME, fixture.getGridColumnRowValue(RULES_GRID, "Application", ruleRow));
    assertEquals(OBJECT_TYPE, fixture.getGridColumnRowValue(RULES_GRID, "Object Type", ruleRow));
    assertEquals(OBJECT_ATTRIBUTE, fixture.getGridColumnRowValue(RULES_GRID, "Attribute", ruleRow));
    assertEquals(TEST_TYPE, fixture.getGridColumnRowValue(RULES_GRID, "Operator", ruleRow));
    assertEquals(TEST_VALUE, fixture.getGridColumnRowValue(RULES_GRID, "Operand", ruleRow));

    String cleanupRow = locateRuleRow();
    fixture.clickOnRecordActionFieldMenuAction(cleanupRow, "Deactivate Test");
  }

  private String locateRuleRow() {
    fixture.sortGridByColumn(RULES_GRID, "Name");
    int scanned = 0;
    int totalCount = fixture.getGridTotalCount(RULES_GRID);
    while (scanned < totalCount) {
      int pageRowCount = fixture.getGridRowCount(RULES_GRID);
      for (int row = 1; row <= pageRowCount; row++) {
        String name = fixture.getGridColumnRowValue(RULES_GRID, "Name", String.valueOf(row));
        if (RULE_NAME.equals(name)) {
          return String.valueOf(row);
        }
      }
      scanned += pageRowCount;
      if (scanned < totalCount) {
        fixture.clickOnGridNavigation(RULES_GRID, "next");
      }
    }
    fail("Could not locate rule \"" + RULE_NAME + "\" in the Rules grid for " + APPLICATION_NAME);
    return null;
  }
}
