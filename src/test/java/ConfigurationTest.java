import balint.lenart.Configuration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class ConfigurationTest {

    @Before
    public void init() {
        try {
            Configuration.loadFromFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void getNewKeyValuePairTest() {
        String value = Configuration.get("application.title");
        Assert.assertNotNull(value);
    }

    @Test(expected = Configuration.MissingKeyException.class)
    public void getNonExistsKeyValuePair() {
        Configuration.get("non.exists.key");
    }

    @Test(expected = Configuration.MissingKeyException.class)
    public void setNonExistsKeyValuePair() {
        Configuration.set("non.exists.key", "some value");
    }

    @Test
    public void setExistsKeyValuePair() {
        Configuration.set("application.title", "Test title");
    }

    @Test(expected = Configuration.AlreadyExistsKeyException.class)
    public void addExistsKeyValuePair() {
        Configuration.add("application.title", "Test title");
    }

    @Test
    public void addNonExistsKeyValuePair() {
        Configuration.add("test.key", "Test value");
    }

}