package excepciones;

/**
 * Se dispara cuando el agente intenta recolectar un activo y la mochila está
 * completamente ocupada.
 *
 * <p>Efecto esperado del catch:
 * debe cortarse la recolección del bunker actual y el valor de los activos que
 * queden sin levantar debe impactar en {@code totalRemanente}.</p>
 */
public class MochilaSaturadaException extends OperacionException {
    private static final long serialVersionUID = 1L;

    public MochilaSaturadaException(String message) {
        super(message);
    }

    public MochilaSaturadaException(String message, Throwable cause) {
        super(message, cause);
    }
}
