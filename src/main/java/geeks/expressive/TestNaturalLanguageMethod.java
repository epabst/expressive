package geeks.expressive;

import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import java.lang.reflect.Method;
import java.util.regex.Pattern;
import java.util.Arrays;


/**
 * A test for {@link NaturalLanguageMethod}.
 *
 * @author pabstec
 */
///CLOVER:OFF
public class TestNaturalLanguageMethod {
  @Test
  public void equalsShouldWork() throws NoSuchMethodException {
    Method method1 = Object.class.getMethod("hashCode");
    Method method2 = Object.class.getMethod("toString");
    Pattern pattern1 = Pattern.compile(".*");
    Pattern pattern1b = Pattern.compile(".*");
    Pattern pattern2 = Pattern.compile("\\w*");
    NaturalLanguageMethod naturalLanguageMethod = new NaturalLanguageMethod(method1, pattern1, Arrays.<ArgumentConverter>asList());
    assertEquality(true, naturalLanguageMethod, new NaturalLanguageMethod(method1, pattern1, Arrays.<ArgumentConverter>asList()));
    assertEquality(true, naturalLanguageMethod, new NaturalLanguageMethod(method1, pattern1b, Arrays.<ArgumentConverter>asList()));
    assertEquality(true, naturalLanguageMethod, new NaturalLanguageMethod(method1, pattern1, Arrays.<ArgumentConverter>asList(ArgumentConverter.IDENTITY)));
    assertEquality(false, naturalLanguageMethod, new NaturalLanguageMethod(method1, pattern2, Arrays.<ArgumentConverter>asList()));
    assertEquality(false, naturalLanguageMethod, new NaturalLanguageMethod(method2, pattern1, Arrays.<ArgumentConverter>asList()));
  }

  private <T> void assertEquality(boolean shouldEqual, T object1, T object2) {
    if (shouldEqual) {
      assertEquals(object1, object2);
      assertEquals(object2, object1);
      assertEquals(object1.hashCode(), object2.hashCode());
    }
    else {
      assertFalse(object1.equals(object2));
      assertFalse(object2.equals(object1));
    }
  }
}
