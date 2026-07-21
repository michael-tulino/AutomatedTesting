package autogen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.appiancorp.ps.automatedtest.fixture.SitesFixture;

public class AddNewRuleToIsaacSandboxTest {

  private static final String TEST_BROWSER = "CHROME";
  private static final String TEST_SITE_VERSION = "24.3";
  private static final String TEST_SITE_URL = "https://ignytedemo.appiancloud.com/suite";
  private static final String TEST_SITE_LOCALE = "en_US";
  private static final String TEST_USERNAME = "automated.tester";
  private static final Integer TEST_TIMEOUT = 60;
  private static final String IADC_SITE_URL = "ignyte-appian-developer-copilo";

  private static final String APPLICATION_NAME = "Isaac Sandbox";
  private static final String ADD_RULE_ACTION = "Add Rule";
  private static final String DEACTIVATE_RULE_ACTION = "Deactivate Test";
  private static final String RULES_GRID = "[1]";

  private static final String RULE_NAME = "Expression Rules Have 1+ Automated Tests";
  private static final String OBJECT_TYPE = "Expression Rule";
  private static final String OBJECT_ATTRIBUTE = "Unit Test Count";
  private static final String TEST_TYPE = "> (Greater Than)";
  private static final String TEST_VALUE = "0";

  private static final String CONNECTED_SYSTEM_RULE_NAME = "Connected Systems Have a Name";
  private static final String CONNECTED_SYSTEM_OBJECT_TYPE = "Connected System";
  private static final String CONNECTED_SYSTEM_OBJECT_ATTRIBUTE = "Name";
  private static final String CONNECTED_SYSTEM_TEST_TYPE = "Not Empty";
  private static final String PENDING_REVIEW_STATUS = "Pending Review";

  private static SitesFixture fixture;

  @BeforeAll
  static void setUp() {
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
  static void tearDown() {
    fixture.tearDown();
  }

  @Test
  void addRuleToIsaacSandboxSavesWithExpectedFieldsAndAppearsInRulesList() {
    fixture.navigateToSite(IADC_SITE_URL);
    fixture.clickOnSitePage("Settings");
    fixture.clickOnLink(APPLICATION_NAME);
    fixture.clickOnRecordRelatedAction(ADD_RULE_ACTION);

    assertTrue(fixture.verifyFieldIsNotPresent("Description"),
        "Description field should no longer be available when adding a new rule");

    fixture.populateFieldWithValue("Name", RULE_NAME);
    fixture.populateFieldWith("Object Type", new String[] { OBJECT_TYPE });
    fixture.populateFieldWith("Object Attribute", new String[] { OBJECT_ATTRIBUTE });
    fixture.populateFieldWith("Test Type", new String[] { TEST_TYPE });
    fixture.populateFieldWithValue("Test Value", TEST_VALUE);
    fixture.clickOnButton("Submit");

    int row = locateRowByName(RULE_NAME);
    assertTrue(row > 0,
        "Newly created rule '" + RULE_NAME + "' was not found in the Rules grid for " + APPLICATION_NAME);

    assertEquals(RULE_NAME, fixture.getGridColumnRowValue(RULES_GRID, "Name", "[" + row + "]"));
    assertEquals(APPLICATION_NAME, fixture.getGridColumnRowValue(RULES_GRID, "Application", "[" + row + "]"));
    assertEquals(OBJECT_TYPE, fixture.getGridColumnRowValue(RULES_GRID, "Object Type", "[" + row + "]"));
    assertEquals(OBJECT_ATTRIBUTE, fixture.getGridColumnRowValue(RULES_GRID, "Attribute", "[" + row + "]"));
    assertEquals(TEST_TYPE, fixture.getGridColumnRowValue(RULES_GRID, "Operator", "[" + row + "]"));
    assertEquals(TEST_VALUE, fixture.getGridColumnRowValue(RULES_GRID, "Operand", "[" + row + "]"));

    fixture.clickOnRecordActionFieldMenuAction("[" + row + "]", DEACTIVATE_RULE_ACTION);
  }

  @Test
  void addRuleWithConnectedSystemObjectTypeIsSetToPendingReviewStatus() {
    fixture.navigateToSite(IADC_SITE_URL);
    fixture.clickOnSitePage("Settings");
    fixture.clickOnLink(APPLICATION_NAME);
    fixture.clickOnRecordRelatedAction(ADD_RULE_ACTION);

    fixture.populateFieldWithValue("Name", CONNECTED_SYSTEM_RULE_NAME);
    fixture.populateFieldWith("Object Type", new String[] { CONNECTED_SYSTEM_OBJECT_TYPE });
    fixture.populateFieldWith("Object Attribute", new String[] { CONNECTED_SYSTEM_OBJECT_ATTRIBUTE });
    fixture.populateFieldWith("Test Type", new String[] { CONNECTED_SYSTEM_TEST_TYPE });
    fixture.clickOnButton("Submit");

    int row = locateRowByName(CONNECTED_SYSTEM_RULE_NAME);
    assertTrue(row > 0,
        "Newly created rule '" + CONNECTED_SYSTEM_RULE_NAME + "' was not found in the Rules grid for " + APPLICATION_NAME);

    assertEquals(PENDING_REVIEW_STATUS, fixture.getGridColumnRowValue(RULES_GRID, "Status", "[" + row + "]"));

    fixture.clickOnRecordActionFieldMenuAction("[" + row + "]", DEACTIVATE_RULE_ACTION);
  }

  private static int locateRowByName(String targetName) {
    int totalCount = fixture.getGridTotalCount(RULES_GRID);
    int checked = 0;
    while (checked < totalCount) {
      int rowCount = fixture.getGridRowCount(RULES_GRID);
      for (int i = 1; i <= rowCount; i++) {
        String value = fixture.getGridColumnRowValue(RULES_GRID, "Name", "[" + i + "]");
        if (targetName.equals(value)) {
          return i;
        }
      }
      checked += rowCount;
      if (checked < totalCount) {
        fixture.clickOnGridNavigation(RULES_GRID, "next");
      }
    }
    return -1;
  }
}
