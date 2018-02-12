package serverless.function;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ConfigLoader {

    private static final Gson GSON = new Gson();

    private ConfigLoader() { }

    public static Config loadConfig(String name) {
        InputStream configFile = ConfigLoader.class.getClassLoader().getResourceAsStream(name);
        try {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(configFile))) {
                return GSON.fromJson(reader, Config.class);
            }
        } catch (IOException e) {
            throw new ServerlessFunctionException("Error reading config.", e);
        }
    }
}
