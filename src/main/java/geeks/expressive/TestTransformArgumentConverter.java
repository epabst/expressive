package geeks.expressive;

import org.testng.annotations.Test;
import static org.easymock.classextension.EasyMock.createMock;

/**
 * A test for {@link geeks.expressive.TransformArgumentConverter}.
 *
 * @author pabstec
 */
///CLOVER:OFF
public class TestTransformArgumentConverter {

  @Test
  public void noncircularRegexAssociationShouldImplementEquals() {
    NaturalLanguageMethod method1 = createMock(NaturalLanguageMethod.class);
    NaturalLanguageMethod method2 = createMock(NaturalLanguageMethod.class);
    MethodRegexAssociation delegate1 = createMock(MethodRegexAssociation.class);
    MethodRegexAssociation delegate2 = createMock(MethodRegexAssociation.class);
    TransformArgumentConverter.NoncircularRegexAssociation association = new TransformArgumentConverter.NoncircularRegexAssociation(method1, delegate1);
    TestNaturalLanguageMethod.assertEquality(true, association, new TransformArgumentConverter.NoncircularRegexAssociation(method1, delegate1));
    TestNaturalLanguageMethod.assertEquality(false, association, new TransformArgumentConverter.NoncircularRegexAssociation(method1, delegate2));
    TestNaturalLanguageMethod.assertEquality(false, association, new TransformArgumentConverter.NoncircularRegexAssociation(method2, delegate1));
    TestNaturalLanguageMethod.assertEquality(false, association, "a string");
    TestNaturalLanguageMethod.assertEquality(false, association, null);
  }
}
