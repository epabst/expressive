package geeks.expressive.cucumber;

import static org.testng.Assert.assertEquals;

import java.net.URL;
import java.io.*;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import geeks.expressive.*;

/**
 * A simplistic implementation of Cucumber.
 *
 * @author pabstec
 */
public class JCucumber {
  private static final AnnotationMethodRegexAssociation COMMAND_ASSOCIATION = new AnnotationMethodRegexAssociation(Command.class);
  private final ResultPublisher resultPublisher;

  public JCucumber(ResultPublisher resultPublisher) {
    this.resultPublisher = resultPublisher;
  }

  public void run(URL featureResource, Scope stepsScope) throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(featureResource.openStream(), "UTF-8"));
    Parser parser = new Parser(resultPublisher, stepsScope);
    ObjectFactory objectFactory = new DefaultObjectFactory();
    objectFactory.addInstance(parser);
    new Expressive(objectFactory).execute(reader, COMMAND_ASSOCIATION, Parser.TRANSFORM_ASSOCIATION,
            Scopes.asScope(Parser.class));
    parser.finished();
  }

  private static class Parser {
    private final Expressive expressive = new Expressive(new DefaultObjectFactory());
    private Mode mode = Mode.NONE;
    private final ResultPublisher resultPublisher;
    private final Scope stepsScope;
    private int stepFailedCountForScenario;
    private static final AnnotationMethodRegexAssociation GIVEN_ASSOCIATION = new AnnotationMethodRegexAssociation(Given.class);
    private static final AnnotationMethodRegexAssociation WHEN_ASSOCIATION = new AnnotationMethodRegexAssociation(When.class);
    private static final AnnotationMethodRegexAssociation THEN_ASSOCIATION = new AnnotationMethodRegexAssociation(Then.class);
    private static final AnnotationMethodRegexAssociation TRANSFORM_ASSOCIATION = new AnnotationMethodRegexAssociation(Transform.class);
    private static final AnnotationMethodSpecifier BEFORE_SPECIFIER = new AnnotationMethodSpecifier(Before.class);

    private Parser(ResultPublisher resultPublisher, Scope stepsScope) {
      this.stepsScope = stepsScope;
      this.resultPublisher = resultPublisher;
    }

    private void setMode(Mode mode) {
      if (this.mode == Mode.IN_SCENARIO_AFTER_WHEN && mode == Mode.IN_FEATURE) {
        if (stepFailedCountForScenario != 0) {
          resultPublisher.failed();
        }
        else {
          resultPublisher.succeeded();
        }
      }
      else if (this.mode == Mode.IN_FEATURE && mode == Mode.IN_SCENARIO_BEFORE_WHEN) {
        stepFailedCountForScenario = 0;
        expressive.executeEvent(BEFORE_SPECIFIER, stepsScope);
      }
      this.mode = mode;
    }

    //test using a non-public method
    @Command("^(Feature: .*)$")
    void feature(String feature) {
      assertMode(Mode.NONE);
      resultPublisher.writeln(feature);
      setMode(Mode.IN_FEATURE);
    }

    @Command("^(\\s*Scenario: .*)$")
    void scenario(String scenario) {
      setMode(Mode.IN_FEATURE);
      resultPublisher.startScenario(scenario);
      setMode(Mode.IN_SCENARIO_BEFORE_WHEN);
    }

    @Command("^(\\s*Given (.*))$")
    public void given(String string, String step) {
      assertMode(Mode.IN_SCENARIO_BEFORE_WHEN);
      executeStepAndWriteString(string, step, GIVEN_ASSOCIATION);
    }

    @Command("^(\\s*When (.*))$")
    public void when(String string, String step) {
      assertMode(Mode.IN_SCENARIO_BEFORE_WHEN);
      executeStepAndWriteString(string, step, WHEN_ASSOCIATION);
      setMode(Mode.IN_SCENARIO_AFTER_WHEN);
    }

    @Command("^(\\s*Then (.*))$")
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

    @Command(Expressive.EVERYTHING_ELSE_REGEX)
    public void everythingElse(String line) {
      assertMode(Mode.IN_FEATURE);
      resultPublisher.writeln(line);
    }

    private void executeStepAndWriteString(String stepLine, String step, AnnotationMethodRegexAssociation annotationAssociation) {
      try {
        expressive.execute(step, annotationAssociation, TRANSFORM_ASSOCIATION, stepsScope);
        resultPublisher.stepPassed(stepLine);
      } catch (Exception e) {
        stepFailedCountForScenario++;
        resultPublisher.stepFailed(stepLine, e);
      } catch (AssertionError e) {
        stepFailedCountForScenario++;
        resultPublisher.stepFailed(stepLine, e);
      }
    }

    private void assertMode(Mode expectedMode) {
      assertEquals(mode, expectedMode);
    }

    private void finished() {
      setMode(Mode.IN_FEATURE);
      setMode(Mode.NONE);
      resultPublisher.finished();
    }
  }

  @Target({ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  private static @interface Command {
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
