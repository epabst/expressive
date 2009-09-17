package geeks.jcucumber.snl;

import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.injectors.ConstructorInjection;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertNotNull;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A test for {@link StructuredNaturalLanguageExecuter}.
 *
 * @author pabstec
 */
///CLOVER:OFF
public class TestStructuredNaturalLanguageExecuter {
  private final DefaultPicoContainer container = new DefaultPicoContainer(new ConstructorInjection());
  private final StructuredNaturalLanguageExecuter executer = StructuredNaturalLanguageExecuter.addAndGetComponent(StructuredNaturalLanguageExecuter.class, container);

  @Test
  public void testParseWithoutToken() {
    assertNotNull("executer should have been set", executer);

    Talker talker = StructuredNaturalLanguageExecuter.addAndGetComponent(Talker.class, container);
    executer.execute("say \"hello\" 10 times without token", Command.class, talker, container);
    assertEquals("hello, hello, hello, hello, hello, hello, hello, hello, hello, hello", talker.getResult());
  }

  @Test
  public void testParseWithTokenAndRegex() {
    assertNotNull("executer should have been set", executer);

    Talker talker = StructuredNaturalLanguageExecuter.addAndGetComponent(Talker.class, container);
    executer.execute("say \"hello\" 10 times with token and regex", Command.class, talker, container);
    assertEquals("hello, hello, hello, hello, hello, hello, hello, hello, hello, hello", talker.getResult());
  }

  @Test
  public void testParseWithRegexAndToken() {
    assertNotNull("executer should have been set", executer);

    Talker talker = StructuredNaturalLanguageExecuter.addAndGetComponent(Talker.class, container);
    executer.execute("say \"hello\" 10 times with regex and token", Command.class, talker, container);
    assertEquals("hello, hello, hello, hello, hello, hello, hello, hello, hello, hello", talker.getResult());
  }

  @Test
  public void testParseWithIllegalAnnotation() {
    assertNotNull("executer should have been set", executer);

    Talker talker = StructuredNaturalLanguageExecuter.addAndGetComponent(Talker.class, container);
    try {
      executer.execute("say \"hello\" 10 times", TagAnnotation.class, talker, container);
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
  public void testParseWithToken() {
    assertNotNull("executer should have been set", executer);

    Talker talker = StructuredNaturalLanguageExecuter.addAndGetComponent(Talker.class, container);
    executer.execute("say \"hello\" 10 times", Command.class, talker, container);
    assertEquals("hello, hello, hello, hello, hello, hello, hello, hello, hello, hello", talker.getResult());
  }

  @Test
  public void testParseWithAlternateToken() {
    assertNotNull("executer should have been set", executer);

    Talker talker = StructuredNaturalLanguageExecuter.addAndGetComponent(Talker.class, container);
    executer.execute("say \"hi\" one time", Command.class, talker, container);
    assertEquals("hi", talker.getResult());
  }

  @UsesTokens({
          @UsesToken(token = "SomeInteger", regex = ".*?", annotation = SomeInteger.class),
          @UsesToken(token = "QuotedString", annotation = QuotedString.class, recognizer = LiteralStrings.class)
  })
  public static class Talker {
    private String result;

    @Command("^say \"([^\"]+)\" ([0-9]+) times? without token$")
    public void saySomethingNTimesWithoutToken(String message, String count) {
      saySomethingNTimes(message, Integer.parseInt(count));
    }

    @Command("^say {QuotedString} ([0-9]+) times? with token and regex$")
    public void saySomethingNTimesWithTokenAndRegex(String message, String count) {
      saySomethingNTimes(message, Integer.parseInt(count));
    }

    @Command("^say \"([^\"]+)\" {SomeInteger} times? with regex and token$")
    public void saySomethingNTimesWithOneToken(String message, int count) {
      saySomethingNTimes(message, count);
    }

    @Command("^say {QuotedString} {SomeInteger} times?$")
    public void saySomethingNTimes(String message, int count) {
      StringBuffer buffer = new StringBuffer(message);
      for (int i = 1; i < count; i++) {
        buffer.append(", ").append(message);
      }
      result = buffer.toString();
    }

    @SomeInteger("^([0-9]+)$")
    public int integer(String number) {
      return java.lang.Integer.parseInt(number);
    }

    @SomeInteger("^one$")
    public int one() {
      return 1;
    }

    //put this here just to make sure it isn't used
    @QuotedString("^\".*\"$")
    public String quotedString() {
      return "this should not be used";
    }

    @TagAnnotation()
    public void methodWithNoRegularExpression() {

    }

    public String getResult() {
      return result;
    }
  }

  public static class LiteralStrings {
    @QuotedString("^\"(.*)\"$")
    String quotedString(String string) {
      return string;
    }
  }

  @Target({ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  public static @interface Command {
    java.lang.String value();
  }

  @Target({ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  public static @interface SomeInteger {
    String value();
  }

  @Target({ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  public static @interface QuotedString {
    String value();
  }

  @Target({ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  public static @interface TagAnnotation {
  }
}

