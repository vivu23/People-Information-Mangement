package myexceptions;

public class UnknownException extends RuntimeException{
    public UnknownException(Exception e) {
        super(e);
    }

    public UnknownException(String msg) {
        super(msg);
    }
}

