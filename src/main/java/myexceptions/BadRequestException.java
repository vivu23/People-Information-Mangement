package myexceptions;

public class BadRequestException extends RuntimeException {
    public BadRequestException(Exception e) { super(e); }
    public BadRequestException(String msg) { super(msg); }
}
