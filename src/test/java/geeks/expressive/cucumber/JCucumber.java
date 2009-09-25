package geeks.expressive.cucumber;

import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.injectors.ConstructorInjection;
import org.reflections.Reflections;
import static org.testng.Assert.assertEquals;

import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import geeks.expressive.Expressive;
import geeks.expressive.AnnotationMethodRegexAssociation;
import geeks.expressive.MethodRegexAssociation;

/**
 * A simplistic implementation of Cucumber.
 *
 * @author pabstec
 */
public class JCucumber {
  private Mode mode = Mode.NONE;
  private final CucumberResult result = new CucumberResult();
  private static final AnnotationMethodRegexAssociation COMMAND_ASSOCIATION = new AnnotationMethodRegexAssociation(Command.class);

  public void run(URL featureResource) throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(featureResource.openStream(), "UTF-8"));
    Expressive expressive = new Expressive(new DefaultPicoContainer(new ConstructorInjection()));
    Reflections internalCucumberReflections = Expressive.toReflections(JCucumber.class);
    expressive.execute(reader, COMMAND_ASSOCIATION, MethodRegexAssociation.NONE, internalCucumberReflections);
  }

  public CucumberResult getResult() {
    return result;
  }

  private void setMode(Mode mode) {
    this.mode = mode;
  }

  @Command("^(Feature: .*)$")
  public void feature(String feature) {
    assertMode(Mode.NONE);
    result.writeln("********************");
    result.writeln(feature);
    result.writeln("********************");
    setMode(Mode.IN_FEATURE);
  }

  @Command("^( *Scenario: .*)$")
  public void scenario(String scenario) {
    setMode(Mode.IN_FEATURE);
    result.writeln(scenario);
    setMode(Mode.IN_SCENARIO_BEFORE_WHEN);
  }

  @Command("^( *Given (.*))$")
  public void given(String string, String step) {
    assertMode(Mode.IN_SCENARIO_BEFORE_WHEN);
    result.writeln(string);
    setMode(Mode.IN_SCENARIO_BEFORE_WHEN);
    //todo run step
  }

  @Command("^( *When (.*))$")
  public void when(String string, String step) {
    assertMode(Mode.IN_SCENARIO_BEFORE_WHEN);
    result.writeln(string);
    //todo run step
    setMode(Mode.IN_SCENARIO_AFTER_WHEN);
  }

  private void assertMode(Mode expectedMode) {
    assertEquals(mode, expectedMode);
  }

  @Command("^( *Then (.*))$")
  public void then(String string, String step) {
    assertMode(Mode.IN_SCENARIO_AFTER_WHEN);
    result.writeln(string);
    //todo run step
  }

  @Command("^(\\s*And (.*))$")
  public void and(String string, String step) {
    if (mode != Mode.IN_SCENARIO_AFTER_WHEN) {
      assertMode(Mode.IN_SCENARIO_BEFORE_WHEN);
      given(string, step);
    } else {
      then(string, step);
    }
    result.writeln(string);
    //todo run step
  }

  @Command("^(.*)$")
  public void noKeyword(String line) {
    assertMode(Mode.IN_FEATURE);
    result.writeln(line);
  }

  @Target({ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  public static @interface Command {
    /**
     * The regular expression to match against for the target method to be used.
     * @return the regular expression string
     */
    public abstract String value();
  }

  private static enum Mode {
    NONE, IN_FEATURE, IN_SCENARIO_BEFORE_WHEN, IN_SCENARIO_AFTER_WHEN 
  }
}
