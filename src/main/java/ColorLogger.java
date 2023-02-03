import java.util.logging.Logger;

public class ColorLogger {
    private static final Logger LOGGER = Logger.getLogger(ColorLogger.class.getName());

    public void logDebug(String logging) {
        LOGGER.fine("\u001B[34m" + logging + "\u001B[0m");
    }
    public void logInfo(String logging) {
        LOGGER.info("\u001B[32m" + logging + "\u001B[0m");
    }

    public void logError(String logging) {
        LOGGER.severe("\u001B[31m" + logging + "\u001B[0m");
    }
}
