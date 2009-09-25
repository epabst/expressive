package geeks.expressive.cucumber;

import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.IOException;

/**
 * A test to show support for doing what Cucumber does.
 *
 * @author pabstec
 */
public class TestCucumberSupport {
  @Test
  public void shouldSupportCucumber() throws IOException {
    JCucumber cucumber = new JCucumber();
    cucumber.run(getClass().getResource("features/Addition.feature"));
    CucumberResult result = cucumber.getResult(); 
    assertEquals(result.getTestCount(), 2);
    assertEquals(result.getFailedCount(), 0);
    assertSubstring(result.getOutput(), "Feature: Addition Using the Calculator");
    assertSubstring(result.getOutput(), "Scenario: 1+1");
    assertSubstring(result.getOutput(), "Then the result should be \"3\"");
  }

  private void assertSubstring(String string, String expectedSubstring) {
    assertTrue(string.contains(expectedSubstring), "Expected '" + expectedSubstring + "' within '" + string + "'");
  }
}
