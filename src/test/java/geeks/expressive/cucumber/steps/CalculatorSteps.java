package geeks.expressive.cucumber.steps;

import geeks.expressive.cucumber.*;
import static org.testng.Assert.assertEquals;

import java.util.Stack;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Calculator steps.
 *
 * @author pabstec
 */
public class CalculatorSteps {
  private final Stack<Integer> stack = new Stack<Integer>();
  private static final Logger LOGGER = Logger.getLogger(CalculatorSteps.class.getName());
  private static final Level DEBUG_LEVEL = Level.FINE;

  @Before()
  public void clearStack() {
    stack.clear();
  }

  @Given("^\"([0-9]+)\" is entered$")
  public void numberIsEntered(int number) {
    stack.push(number);
    LOGGER.log(DEBUG_LEVEL, "stack after entering number: " + stack);
  }

  @When("^I push \"\\+\"$")
  public void add() {
    LOGGER.log(DEBUG_LEVEL, "stack before +: " + stack);
    stack.push(stack.pop() + stack.pop());
    LOGGER.log(DEBUG_LEVEL, "stack after +: " + stack);
  }

  @Then("^the result should be \"([0-9]+)\"$")
  public void theResultShouldBe(int expectedResult) {
    int result = stack.peek();
    assertEquals(result, expectedResult, "result");
    assertEquals(stack.size(), 1, "stack size");
  }

  @Transform("^([0-9]+)$")
  public int integer(String number) {
    return Integer.parseInt(number);
  }
}
