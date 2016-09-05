package balint.lenart;

import com.google.common.collect.Maps;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

public class Configuration {

    private static final File SETTINGS_FILE = new File("settings.properties");

    private static final Map<String, String> SETTINGS = Maps.newHashMap();

    public static void loadFromFile() throws IOException {
        if( !SETTINGS_FILE.exists() ) {
            createDefaultConfiguration();
        }

        Properties settings = new Properties();
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(SETTINGS_FILE);
            settings.load(inputStream);

            settings.forEach((key, value) -> SETTINGS.put((String)key, (String)value));
        } catch (IOException e) {
            e.printStackTrace();
            createDefaultConfiguration();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String get(String key) {
        return SETTINGS.get(key);
    }

    private static void createDefaultConfiguration() throws IOException {
        URL defaultSettingsFileUrl = Configuration.class.getClassLoader().getResource("default-settings.properties");
        System.out.println(defaultSettingsFileUrl);
        FileUtils.copyURLToFile(defaultSettingsFileUrl, SETTINGS_FILE);
    }
}
