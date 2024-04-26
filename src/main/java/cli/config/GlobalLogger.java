package cli.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class GlobalLogger {
    public static final Logger log;

    static {
        InputStream stream = GlobalLogger.class.getClassLoader().getResourceAsStream("logging.properties");
        try {
            LogManager.getLogManager().readConfiguration(stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log = Logger.getLogger(GlobalLogger.class.getName());
    }

    public static void turnOffLogging() {
        log.setLevel(java.util.logging.Level.OFF);
    }
}
