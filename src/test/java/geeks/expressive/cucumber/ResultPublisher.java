package geeks.expressive.cucumber;

import java.io.Writer;
import java.io.PrintWriter;

/**
 * A Cucumber result publisher.
 *
 * @author pabstec
 */
public class ResultPublisher {
  private int testCount = 0;
  private int failedCount = 0;
  private final PrintWriter writer;
  private String scenarioName;

  public ResultPublisher(Writer outputWriter) {
    writer = new PrintWriter(outputWriter);
  }

  public void stepPassed(String string) {
    writeln("PASSED: " + string);
  }
  
  public void stepFailed(String string, Exception exception) {
    writeln("FAILED: " + string);
    exception.printStackTrace(writer);
  }

  public void writeln(String string) {
    writer.println(string);
  }

  public void startScenario(String scenarioName) {
    this.scenarioName = scenarioName;
    writeln("********************");
    writeln(scenarioName);
    writeln("********************");
  }

  public void succeeded() {
    writeln("  " + scenarioName + " succeeded :)");
    testCount++;
  }

  public void failed() {
    writeln("  " + scenarioName + " failed :(");
    testCount++;
    failedCount++;
  }

  public int getTestCount() {
    return testCount;
  }

  public int getFailedCount() {
    return failedCount;
  }
}
