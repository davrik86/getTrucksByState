package Utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class dataReader {
    private static Properties configFile;

    static {
        String path="src/test/resources.property";
        try {
            FileInputStream fileInputStream= new FileInputStream(path);
            configFile= new Properties();
            configFile.load(fileInputStream);
            fileInputStream.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getPropertyValue(String key){
        return configFile.getProperty(key);
    }

}
