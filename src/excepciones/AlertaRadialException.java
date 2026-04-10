package excepciones;

/**
 * Se dispara cuando un bunker activa una alerta radial que afecta a los bunkers
 * vecinos inmediatos.
 *
 * <p>Efecto esperado del catch:
 * deben afectarse los bunkers ID - 1 e ID + 1 cuando existan, destruyendo parte
 * de sus activos y acumulando ese valor en {@code totalQuemadoAlerta}.</p>
 */
public class AlertaRadialException extends OperacionException {
    private static final long serialVersionUID = 1L;

    public AlertaRadialException(String message) {
        super(message);
    }

    public AlertaRadialException(String message, Throwable cause) {
        super(message, cause);
    }
}
