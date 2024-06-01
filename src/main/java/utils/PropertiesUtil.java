package utils;

import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Properties;

@Slf4j
public class PropertiesUtil {
    public static Properties loadFromFile(Path path) {
        return loadFromFile(path.toString());
    }

    public static Properties loadFromFile(String path) {
        Properties props = new Properties();
        try {
            final InputStreamReader fr = new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8);
            props.load(fr);
            fr.close();
        } catch (IOException ex) {
            log.info("failed to load properties from file", ex);
        }
        return props;
    }
}
