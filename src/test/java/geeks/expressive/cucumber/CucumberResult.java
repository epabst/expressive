package geeks.expressive.cucumber;

import java.io.StringWriter;

/**
 * A Cucumber result.
 *
 * @author pabstec
 */
public class CucumberResult {
  private int testCount = 0;
  private int failedCount = 0;
  private final StringWriter writer = new StringWriter(); 

  public void writeln(String string) {
    writer.write(string);
    writer.write("\n");
  }

  public void oneSucceeded() {
    testCount++;
  }

  public void oneFailed() {
    testCount++;
    failedCount++;
  }

  public int getTestCount() {
    return testCount;
  }

  public int getFailedCount() {
    return failedCount;
  }

  public String getOutput() {
    writer.flush();
    return writer.toString();
  }
}
