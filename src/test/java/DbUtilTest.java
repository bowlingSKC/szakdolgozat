import balint.lenart.utils.DbUtil;
import org.junit.Assert;
import org.junit.Test;

public class DbUtilTest {

    @Test
    public void testQuotedStringWithNull() {
        Assert.assertEquals(null, DbUtil.getQuotedString(null));
    }

    @Test
    public void testQuotedStringWithEmpty() {
        Assert.assertEquals("''", DbUtil.getQuotedString(""));
    }

    @Test
    public void testQuotedStringWithNonEmpty() {
        final String testString = "this is a test string";
        Assert.assertEquals("'"+ testString +"'", DbUtil.getQuotedString(testString));
    }

}
