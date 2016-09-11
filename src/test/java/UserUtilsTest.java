import balint.lenart.model.User;
import balint.lenart.utils.Tuple;
import balint.lenart.utils.UserUtils;
import org.junit.Assert;
import org.junit.Test;

public class UserUtilsTest {

    private static final String ONE_NAME_STRING = "Test";
    private static final String TWO_NAME_STRING = "John Test";
    private static final String THREE_NAME_STRING = "John A. Test";

    @Test
    public void testWithZeroName() {
        User user = new User(null, false, null, null, "", null, null);
        Tuple<String, String> nameByUser = UserUtils.getNameByUser(user);
        Assert.assertNotNull(nameByUser.getFirst());
        Assert.assertNull(nameByUser.getSecond());
        Assert.assertEquals(user.getFullName(), nameByUser.getFirst());
    }

    @Test
    public void testWithNullName() {
        User user = new User(null, false, null, null, null, null, null);
        Tuple<String, String> nameByUser = UserUtils.getNameByUser(user);
        Assert.assertNull(nameByUser.getFirst());
        Assert.assertNull(nameByUser.getSecond());
        Assert.assertEquals(user.getFullName(), nameByUser.getFirst());
    }

    @Test
    public void testWithOneName() {
        User user = new User(null, false, null, null, ONE_NAME_STRING, null, null);
        Tuple<String, String> nameByUser = UserUtils.getNameByUser(user);
        Assert.assertNotNull(nameByUser.getFirst());
        Assert.assertNull(nameByUser.getSecond());
        Assert.assertEquals(user.getFullName(), nameByUser.getFirst());
    }

    @Test
    public void testWithTwoName() {
        User user = new User(null, false, null, null, TWO_NAME_STRING, null, null);
        Tuple<String, String> nameByUser = UserUtils.getNameByUser(user);
        Assert.assertNotNull(nameByUser.getFirst());
        Assert.assertNotNull(nameByUser.getSecond());
        Assert.assertEquals("John", nameByUser.getFirst());
        Assert.assertEquals("Test", nameByUser.getSecond());
    }

    @Test
    public void testWithThreeName() {
        User user = new User(null, false, null, null, THREE_NAME_STRING, null, null);
        Tuple<String, String> nameByUser = UserUtils.getNameByUser(user);
        Assert.assertNotNull(nameByUser.getFirst());
        Assert.assertNotNull(nameByUser.getSecond());
        Assert.assertEquals("John", nameByUser.getFirst());
        Assert.assertEquals("A. Test", nameByUser.getSecond());
    }

}
