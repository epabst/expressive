package geeks.jcucumber.snl;

import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.injectors.ConstructorInjection;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertNotNull;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.*;

/**
 * A test for {@link StructuredNaturalLanguageExecuter}.
 *
 * @author pabstec
 */
///CLOVER:OFF
public class TestStructuredNaturalLanguageExecuter {
  private StructuredNaturalLanguageExecuter executer;

  @BeforeMethod
  private void setup() {
    executer = new StructuredNaturalLanguageExecuter(new DefaultPicoContainer(new ConstructorInjection()));
  }

  @Test
  public void testParseWithTransform() {
    assertNotNull("executer should have been set", executer);

    executer.execute("say \"hello\" 10 times", AnnotationMethodRegexIdentifier.getInstance(Command.class),
            AnnotationMethodRegexIdentifier.getInstance(Transform.class), Talker.class);
    Talker talker = executer.addAndGetComponent(Talker.class);
    assertEquals(talker.getResult(), "hello, hello, hello, hello, hello, hello, hello, hello, hello, hello");
  }

  @Test
  public void testParseWithTransformToSubclass() {
    assertNotNull("executer should have been set", executer);

    executer.execute("say size of [a, b, c]", AnnotationMethodRegexIdentifier.getInstance(Command.class),
            AnnotationMethodRegexIdentifier.getInstance(Transform.class), Talker.class);
    Talker talker = executer.addAndGetComponent(Talker.class);
    assertEquals(talker.getResult(), "3");
  }

  @Test
  public void testParseWithTransformToSubclass2() {
    assertNotNull("executer should have been set", executer);

    executer.execute("say size of {a, b, c, d}", AnnotationMethodRegexIdentifier.getInstance(Command.class),
            AnnotationMethodRegexIdentifier.getInstance(Transform.class), Talker.class);
    Talker talker = executer.addAndGetComponent(Talker.class);
    assertEquals(talker.getResult(), "4");
  }

  @Test
  public void testParseAvoidsWrongTransform() {
    assertNotNull("executer should have been set", executer);

    executer.execute("say \"hello\" 10 times (as string)", AnnotationMethodRegexIdentifier.getInstance(Command.class),
            AnnotationMethodRegexIdentifier.getInstance(Transform.class), Talker.class);
    Talker talker = executer.addAndGetComponent(Talker.class);
    assertEquals(talker.getResult(), "hello, hello, hello, hello, hello, hello, hello, hello, hello, hello");
  }

  @Test
  public void testParseWithTransform_AttemptingCircular() {
    assertNotNull("executer should have been set", executer);

    executer.execute("say \"circular1\" 10 times", AnnotationMethodRegexIdentifier.getInstance(Command.class),
            AnnotationMethodRegexIdentifier.getInstance(Transform.class), Talker.class);
    Talker talker = executer.addAndGetComponent(Talker.class);
    assertEquals(talker.getResult(), "circular2, circular2, circular2, circular2, circular2, circular2, circular2, circular2, circular2, circular2");
  }

  @Test
  public void testParseWithIllegalAnnotation() {
    assertNotNull("executer should have been set", executer);

    try {
      executer.execute("say \"hello\" 10 times", AnnotationMethodRegexIdentifier.getInstance(TagAnnotation.class),
              AnnotationMethodRegexIdentifier.getInstance(Transform.class), Talker.class);
      fail("expected exception");
    }
    catch (IllegalStateException e) {
      assertEquals(e.getCause().getClass(), NoSuchMethodException.class);
      assertMessageSubstring("value", e);
    }
  }

  private static void assertMessageSubstring(String expectedMessage, Throwable throwable) {
    assertTrue(throwable.getMessage().contains(expectedMessage),
            "Expected '" + expectedMessage + "' within '" + throwable.getMessage() + "'");
  }

  @Test
  public void testParseWithAlternateTransform() {
    assertNotNull("executer should have been set", executer);

    executer.execute("say \"hi\" one time", AnnotationMethodRegexIdentifier.getInstance(Command.class),
            AnnotationMethodRegexIdentifier.getInstance(Transform.class), Talker.class);
    Talker talker = executer.addAndGetComponent(Talker.class);
    assertEquals(talker.getResult(), "hi");
  }

  public static class Talker {
    private String result;
    private static final String QUOTED_STRING = "\".*\"";
    private static final String COLLECTION_STRING = ".*, .*";
    private static final String INTEGER = ".*?";

    @Command("^say (" + QUOTED_STRING + ") (" + INTEGER + ") times?$")
    public void saySomethingNTimes(String message, int count) {
      StringBuffer buffer = new StringBuffer(message);
      for (int i = 1; i < count; i++) {
        buffer.append(", ").append(message);
      }
      result = buffer.toString();
    }

    @Command("^say (" + QUOTED_STRING + ") (" + INTEGER + ") times? \\(as string\\)$")
    public void saySomethingNTimes(String message, String countString) {
      StringBuffer buffer = new StringBuffer(message);
      for (int i = 1; i < Integer.parseInt(countString); i++) {
        buffer.append(", ").append(message);
      }
      result = buffer.toString();
    }

    @Command("^say size of (" + COLLECTION_STRING + ")$")
    public void saySizeOf(Collection<?> collection) {
      result = String.valueOf(collection.size());
    }

    @Transform("^([0-9]+)$")
    public int integer(String number) {
      return java.lang.Integer.parseInt(number);
    }

    @Transform("^one$")
    public int one() {
      return 1;
    }

    @Transform("^\"(.*)\"$")
    String quotedString(String string) {
      return string;
    }

    @Transform("^\\[(.*, .*)\\]$")
    List<String> stringList(String itemsString) {
      return Arrays.asList(itemsString.split(", "));
    }

    @Transform("^\\{(.*, .*)\\}$")
    Set<String> stringSet(String itemsString) {
      return new LinkedHashSet<String>(stringList(itemsString));
    }

    @SuppressWarnings({"UnusedDeclaration"})
    @Transform("^(circular1)$")
    public String circular1(String string) {
      return "circular2";
    }

    @SuppressWarnings({"UnusedDeclaration"})
    @Transform("^(circular2)$")
    public String circular2(String string) {
      return "circular1";
    }

    @TagAnnotation()
    public void methodWithNoRegularExpression() {

    }

    public String getResult() {
      return result;
    }
  }

  @Target({ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  public static @interface Command {
    java.lang.String value();
  }

  @Target({ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  public static @interface TagAnnotation {
  }

  /**
 * An annotation for methods that indicates it can transform String arguments to the correct parameter type.
   *
   * @author pabstec
   */
  @Target({ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  public static @interface Transform {
    /**
     * The regular expression to match against for the target method to be used.
     * @return the regular expression string
     */
    public abstract String value();
  }
}

