package geeks.expressive;

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
import java.text.DateFormat;

/**
 * A test for {@link Expressive}.
 *
 * @author pabstec
 */
///CLOVER:OFF
public class TestExpressive {
  private Expressive executer;

  @BeforeMethod
  private void setup() {
    executer = new Expressive(new DefaultObjectFactory());
  }

  @Test
  public void testIgnored() {
    assertNotNull("executer should have been set", executer);

    List<String> plannedIgnoredLines = Arrays.asList("blah blah blah", "fee fi fo fum");
    for (String plannedIgnoredLine : plannedIgnoredLines) {
      executer.execute(plannedIgnoredLine, new AnnotationMethodRegexAssociation(Command.class),
              new AnnotationMethodRegexAssociation(TransformForTesting.class), Scopes.asScope(Talker.class));
    }
    Talker talker = executer.addAndGetComponent(Talker.class);
    assertEquals(talker.getIgnoredLines(), plannedIgnoredLines);
  }

  @Test
  public void testParseWithTransform() {
    assertNotNull("executer should have been set", executer);

    executer.execute("say \"hello\" 10 times", new AnnotationMethodRegexAssociation(Command.class),
            new AnnotationMethodRegexAssociation(TransformForTesting.class), Scopes.asScope(Talker.class));
    Talker talker = executer.addAndGetComponent(Talker.class);
    assertEquals(talker.getResult(), "hello, hello, hello, hello, hello, hello, hello, hello, hello, hello");
  }

  @Test
  public void testParseWithTransformToSubclass() {
    assertNotNull("executer should have been set", executer);

    executer.execute("say size of [a, b, c]", new AnnotationMethodRegexAssociation(Command.class),
            new AnnotationMethodRegexAssociation(TransformForTesting.class), Scopes.asScope(Talker.class));
    Talker talker = executer.addAndGetComponent(Talker.class);
    assertEquals(talker.getResult(), "3");
  }

  @Test
  public void testParseWithTransformToSubclass2() {
    assertNotNull("executer should have been set", executer);

    executer.execute("say size of {a, b, c, d}", new AnnotationMethodRegexAssociation(Command.class),
            new AnnotationMethodRegexAssociation(TransformForTesting.class), Scopes.asScope(Talker.class));
    Talker talker = executer.addAndGetComponent(Talker.class);
    assertEquals(talker.getResult(), "4");
  }

  @Test
  public void testParseWithTransformUsingSearchPath() {
    assertNotNull("executer should have been set", executer);

    executer.execute("say date 10/21/2008", new AnnotationMethodRegexAssociation(Command.class),
            new AnnotationMethodRegexAssociation(TransformForTesting.class), Scopes.asScope(TestExpressive.class.getPackage()));
    Talker talker = executer.addAndGetComponent(Talker.class);
    assertEquals(talker.getResult(), "October 21, 2008");
  }

  @Test
  public void testParseAvoidsWrongTransform() {
    assertNotNull("executer should have been set", executer);

    executer.execute("say \"hello\" 10 times (as string)", new AnnotationMethodRegexAssociation(Command.class),
            new AnnotationMethodRegexAssociation(TransformForTesting.class), Scopes.asScope(Talker.class));
    Talker talker = executer.addAndGetComponent(Talker.class);
    assertEquals(talker.getResult(), "hello, hello, hello, hello, hello, hello, hello, hello, hello, hello");
  }

  @Test
  public void testParseWithTransform_AttemptingCircular() {
    assertNotNull("executer should have been set", executer);

    executer.execute("say \"circular1\" 10 times", new AnnotationMethodRegexAssociation(Command.class),
            new AnnotationMethodRegexAssociation(TransformForTesting.class), Scopes.asScope(Talker.class));
    Talker talker = executer.addAndGetComponent(Talker.class);
    assertEquals(talker.getResult(), "circular2, circular2, circular2, circular2, circular2, circular2, circular2, circular2, circular2, circular2");
  }

  @Test
  public void testParseWithIllegalAnnotation() {
    assertNotNull("executer should have been set", executer);

    try {
      executer.execute("say \"hello\" 10 times", new AnnotationMethodRegexAssociation(TagAnnotation.class),
              new AnnotationMethodRegexAssociation(TransformForTesting.class), Scopes.asScope(Talker.class));
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

    executer.execute("say \"hi\" one time", new AnnotationMethodRegexAssociation(Command.class),
            new AnnotationMethodRegexAssociation(TransformForTesting.class), Scopes.asScope(Talker.class));
    Talker talker = executer.addAndGetComponent(Talker.class);
    assertEquals(talker.getResult(), "hi");
  }

  public static class Talker {
    private String result;
    private static final String QUOTED_STRING = "\".*\"";
    private static final String COLLECTION_STRING = ".*, .*";
    private static final String DATE_STRING = ".*/.*";
    private static final String INTEGER = ".*?";
    private final List<String> ignoredLines = new LinkedList<String>();

    @Command(Expressive.EVERYTHING_ELSE_REGEX)
    public void everythingElse(String string) {
      ignoredLines.add(string);
    }

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

    @Command("^say date (" + DATE_STRING + ")$")
    public void sayDate(Date date) {
      result = DateFormat.getDateInstance(DateFormat.LONG).format(date);
    }

    @TransformForTesting("^([0-9]+)$")
    public int integer(String number) {
      return java.lang.Integer.parseInt(number);
    }

    @TransformForTesting("^one$")
    public int one() {
      return 1;
    }

    @TransformForTesting("^\"(.*)\"$")
    public String quotedString(String string) {
      return string;
    }

    @TransformForTesting("^\\[(.*, .*)\\]$")
    public List<String> stringList(String itemsString) {
      return Arrays.asList(itemsString.split(", "));
    }

    @TransformForTesting("^\\{(.*, .*)\\}$")
    public Set<String> stringSet(String itemsString) {
      return new LinkedHashSet<String>(stringList(itemsString));
    }

    @SuppressWarnings({"UnusedDeclaration"})
    @TransformForTesting("^(circular1)$")
    public String circular1(String string) {
      return "circular2";
    }

    @SuppressWarnings({"UnusedDeclaration"})
    @TransformForTesting("^(circular2)$")
    public String circular2(String string) {
      return "circular1";
    }

    @TagAnnotation()
    public void methodWithNoRegularExpression() {

    }

    public String getResult() {
      return result;
    }

    public List<String> getIgnoredLines() {
      return ignoredLines;
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

}

