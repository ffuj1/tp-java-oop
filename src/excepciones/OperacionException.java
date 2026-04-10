package excepciones;

/**
 * Excepción base para todos los eventos excepcionales de negocio de la operación.
 *
 * <p>Contrato de uso:
 * debe emplearse como superclase común para los casos previstos por la consigna,
 * evitando representar estos escenarios con excepciones genéricas de Java.</p>
 *
 * <p>Efecto esperado del catch:
 * cualquier captura de una subclase debe producir un cambio real y trazable en el
 * estado del sistema, en la auditoría o en el flujo de la simulación.</p>
 */
public class OperacionException extends Exception {
    private static final long serialVersionUID = 1L;

    public OperacionException(String message) {
        super(message);
    }

    public OperacionException(String message, Throwable cause) {
        super(message, cause);
    }
}
