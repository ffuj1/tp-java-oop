package excepciones;

/**
 * Se dispara cuando la resistencia de un bunker supera la capacidad operativa
 * disponible para intervenirlo en el intento actual.
 *
 * <p>Efecto esperado del catch:
 * debe intentarse apoyo con el bunker de ID + 1; si ese apoyo no existe,
 * corresponde registrar repliegue en el log y continuar con el flujo definido
 * por la simulación.</p>
 */
public class ResistenciaSuperiorException extends OperacionException {
    private static final long serialVersionUID = 1L;

    public ResistenciaSuperiorException(String message) {
        super(message);
    }

    public ResistenciaSuperiorException(String message, Throwable cause) {
        super(message, cause);
    }
}
