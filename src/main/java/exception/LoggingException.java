package exception;

public class LoggingException extends RuntimeException {
    public LoggingException(Exception e){
        super(e);
    }
}
