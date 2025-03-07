package exceptions;
/**
 * Custom exception class to handle cases where input has invalid arguments.
 */
public class InvalidArgumentException extends Exception {

    /**
     * Public constructor for throwing InvalidArgumentException
     * @param message string of text
     */
    public InvalidArgumentException(String message, String cmd) {
        super("Wrong argument '" + message + "' provided, macho! Please use " + cmd
                + " for any tasks, macho!\n");
    }
}
