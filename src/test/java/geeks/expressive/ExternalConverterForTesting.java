package geeks.expressive;

import java.util.Date;
import java.text.ParseException;
import java.text.DateFormat;

/**
 * A converter outside of the class that uses it.
*
* @author pabstec
*/
public class ExternalConverterForTesting {
  @TransformForTesting("^([0-9][0-9]/[0-9][0-9]/[0-9][0-9][0-9][0-9])$")
  public Date toDate(String string) throws ParseException {
    return DateFormat.getDateInstance(DateFormat.SHORT).parse(string);
  }
}
