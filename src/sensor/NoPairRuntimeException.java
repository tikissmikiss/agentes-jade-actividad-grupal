package sensor;

public class NoPairRuntimeException extends RuntimeException {

    public NoPairRuntimeException() {
        super("No se ha emparejado con un actuador");
    }

    public NoPairRuntimeException(String message) {
        super(message);
    }

}
