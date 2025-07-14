package Cataphract.API;

import java.io.PrintWriter;
import java.io.StringWriter;

import Cataphract.API.Wraith.FileWrite;

/**
 * Handles exceptions by formatting stack traces, logging errors, and managing program termination.
 */
public class ExceptionHandler {
    private final StackTraceFormatter stackTraceFormatter;
    private final ErrorLogger errorLogger;
    private final UserInteractionHandler userInteractionHandler;
    private final ExitHandler exitHandler;

    /**
     * Constructs an ExceptionHandler instance with no arguments, using static dependencies from Config
     * and a locally instantiated FileWrite for logging.
     */
    public ExceptionHandler() {
        this.stackTraceFormatter = new StackTraceFormatter();
        this.errorLogger = new FileErrorLogger();
        this.userInteractionHandler = new ConsoleUserInteractionHandler();
        this.exitHandler = new DefaultExitHandler();
    }

    /**
     * Handles an exception by formatting its stack trace, logging it, collecting user input, and exiting.
     *
     * @param e The exception to handle.
     */
    public void handleException(Exception e) {
        String stackTrace = stackTraceFormatter.formatStackTrace(e);
        Config.io.println("[ FATAL ERROR ] AN EXCEPTION OCCURRED DURING THE EXECUTION OF THE PROGRAM.");
        Config.io.println("\n[ --- TECHNICAL DETAILS --- ]\n");
        Config.io.println("Class: " + e.getClass().getName());
        Config.io.println("Trace Details: " + e.getStackTrace());
        Config.io.println(stackTrace);
        Config.io.println("[ END OF TECHNICAL DETAILS ]\n");

        Config.io.println("This information will be written into a log file which can be used to debug the cause of the failure.");
        Config.io.println("Any additional information can be useful to find the root cause of the issue efficiently.");

        String userComment = userInteractionHandler.collectUserComment();
        errorLogger.logError(e, stackTrace, userComment);
        exitHandler.handleExit(userInteractionHandler.promptForRestart());
    }
}

/**
 * Formats exception stack traces.
 */
class StackTraceFormatter {
    /**
     * Formats an exception's stack trace into a string.
     *
     * @param e The exception to format.
     * @return The formatted stack trace.
     */
    public String formatStackTrace(Exception e) {
        try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
            e.printStackTrace(pw);
            return """
                \n***************************************
                !         PROGRAM STACK TRACE         !
                ***************************************

                """ + sw.toString() + """

                ***************************************
                !           STACK TRACE END           !
                ***************************************
                """;
        } catch (Exception ex) {
            return "Error formatting stack trace: " + ex.getMessage();
        }
    }
}

/**
 * Interface for logging errors.
 */
interface ErrorLogger {
    void logError(Exception e, String stackTrace, String userComment);
}

/**
 * Logs errors to a file using a locally instantiated FileWrite.
 */
class FileErrorLogger implements ErrorLogger {
    private static final String LOG_FILE_NAME = Config.LOG_FILE_NAME;
    private static final FileWrite fileWrite;

    static {
        // Instantiate FileWrite with dependencies for logging only (no Login or PolicyCheck)
        fileWrite = new FileWrite(
            null
        );
    }

    @Override
    public void logError(Exception e, String stackTrace, String userComment) {
        try {
            StringBuilder logContent = new StringBuilder()
                .append("\n[--- TECHNICAL DETAILS ---]\n")
                .append(e.getClass().getName()).append("\n")
                .append(e.getStackTrace().toString()).append("\n")
                .append(stackTrace).append("\n")
                .append("User Comment> ").append(userComment).append("\n\n");
            fileWrite.log(logContent.toString(), LOG_FILE_NAME);
        } catch (Exception ex) {
            Config.io.println("Error logging exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}

/**
 * Interface for user interaction.
 */
interface UserInteractionHandler {
    String collectUserComment();
    boolean promptForRestart();
}

/**
 * Handles user interaction via console.
 */
class ConsoleUserInteractionHandler implements UserInteractionHandler {
    @Override
    public String collectUserComment() {
        return Config.console != null ? Config.console.readLine("User Comment> ") : "";
    }

    @Override
    public boolean promptForRestart() {
        if (Config.console == null) {
            return false;
        }
        String response = Config.console.readLine("Do you want to restart the program? [ Y | N ]> ");
        return response != null && response.trim().equalsIgnoreCase("y");
    }
}

/**
 * Interface for handling program exit.
 */
interface ExitHandler {
    void handleExit(boolean restart);
}

/**
 * Default exit handler with configurable exit codes.
 */
class DefaultExitHandler implements ExitHandler {
    private static final int EXIT_CODE_NORMAL = 211;
    private static final int EXIT_CODE_RESTART = 5;

    @Override
    public void handleExit(boolean restart) {
        System.exit(restart ? EXIT_CODE_RESTART : EXIT_CODE_NORMAL);
    }
}