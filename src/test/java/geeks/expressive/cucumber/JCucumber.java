package geeks.expressive.cucumber;

import org.reflections.Reflections;
import static org.testng.Assert.assertEquals;

import java.net.URL;
import java.io.*;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import geeks.expressive.Expressive;
import geeks.expressive.AnnotationMethodRegexAssociation;
import geeks.expressive.ObjectFactory;
import geeks.expressive.AnnotationMethodSpecifier;
import geeks.expressive.cucumber.steps.CalculatorSteps;

/**
 * A simplistic implementation of Cucumber.
 *
 * @author pabstec
 */
public class JCucumber {
  private static final AnnotationMethodRegexAssociation COMMAND_ASSOCIATION = new AnnotationMethodRegexAssociation(Command.class);
  private final Writer outputWriter;

  public JCucumber(Writer outputWriter) {
    this.outputWriter = outputWriter;
  }

  public ResultPublisher run(URL featureResource) throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(featureResource.openStream(), "UTF-8"));
    ObjectFactory objectFactory = new ObjectFactory();
    ResultPublisher resultPublisher = new ResultPublisher(outputWriter);
    objectFactory.addInstance(resultPublisher);
    Expressive expressive = new Expressive(objectFactory);
    Reflections internalCucumberReflections = Expressive.toReflections(Parser.class);
    expressive.execute(reader, COMMAND_ASSOCIATION, new AnnotationMethodRegexAssociation(Transform.class), internalCucumberReflections);
    Parser parser = objectFactory.getInstance(Parser.class);
    parser.setMode(Mode.IN_FEATURE);
    parser.setMode(Mode.NONE);
    return resultPublisher;
  }

  public static class Parser {
    private final Expressive expressive = new Expressive(new ObjectFactory());
    private Mode mode = Mode.NONE;
    private final ResultPublisher resultPublisher;
    private final Reflections reflections;
    private static final AnnotationMethodRegexAssociation GIVEN_ASSOCIATION = new AnnotationMethodRegexAssociation(Given.class);
    private static final AnnotationMethodRegexAssociation WHEN_ASSOCIATION = new AnnotationMethodRegexAssociation(When.class);
    private static final AnnotationMethodRegexAssociation THEN_ASSOCIATION = new AnnotationMethodRegexAssociation(Then.class);
    private static final AnnotationMethodRegexAssociation TRANSFORM_ASSOCIATION = new AnnotationMethodRegexAssociation(Transform.class);
    private static final AnnotationMethodSpecifier BEFORE_SPECIFIER = new AnnotationMethodSpecifier(Before.class);

    public Parser(ResultPublisher resultPublisher) {
      reflections = Expressive.toReflections(CalculatorSteps.class.getPackage());
      this.resultPublisher = resultPublisher;
    }

    private void setMode(Mode mode) {
      if (this.mode == Mode.IN_SCENARIO_AFTER_WHEN && mode == Mode.IN_FEATURE) {
        resultPublisher.succeeded();
        expressive.executeEvent(BEFORE_SPECIFIER, reflections);
      }
      this.mode = mode;
    }

    @Command("^(Feature: .*)$")
    public void feature(String feature) {
      assertMode(Mode.NONE);
      resultPublisher.writeln(feature);
      setMode(Mode.IN_FEATURE);
    }

    @Command("^( *Scenario: .*)$")
    public void scenario(String scenario) {
      setMode(Mode.IN_FEATURE);
      resultPublisher.startScenario(scenario);
      setMode(Mode.IN_SCENARIO_BEFORE_WHEN);
    }

    @Command("^( *Given (.*))$")
    public void given(String string, String step) {
      assertMode(Mode.IN_SCENARIO_BEFORE_WHEN);
      executeStepAndWriteString(string, step, GIVEN_ASSOCIATION);
      setMode(Mode.IN_SCENARIO_BEFORE_WHEN);
    }

    private void executeStepAndWriteString(String stepLine, String step, AnnotationMethodRegexAssociation annotationAssociation) {
      try {
        expressive.execute(step, annotationAssociation, TRANSFORM_ASSOCIATION, reflections);
        resultPublisher.stepPassed(stepLine);
      } catch (Exception e) {
        resultPublisher.stepFailed(stepLine, e);
      }
    }

    @Command("^( *When (.*))$")
    public void when(String string, String step) {
      assertMode(Mode.IN_SCENARIO_BEFORE_WHEN);
      executeStepAndWriteString(string, step, WHEN_ASSOCIATION);
      setMode(Mode.IN_SCENARIO_AFTER_WHEN);
    }

    @Command("^( *Then (.*))$")
    public void then(String string, String step) {
      assertMode(Mode.IN_SCENARIO_AFTER_WHEN);
      executeStepAndWriteString(string, step, THEN_ASSOCIATION);
    }

    @Command("^(\\s*And (.*))$")
    public void and(String string, String step) {
      if (mode != Mode.IN_SCENARIO_AFTER_WHEN) {
        assertMode(Mode.IN_SCENARIO_BEFORE_WHEN);
        given(string, step);
      } else {
        then(string, step);
      }
    }

    @Command("^\\s*$")
    public void blankLine() {
      //do nothing
    }

    private void assertMode(Mode expectedMode) {
      assertEquals(mode, expectedMode);
    }

    @Command(Expressive.EVERYTHING_ELSE_REGEX)
    public void everythingElse(String line) {
      assertMode(Mode.IN_FEATURE);
      resultPublisher.writeln(line);
    }
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
