package excepciones;

/**
 * Se dispara cuando el IAS cae por debajo del umbral que habilita un disturbio
 * civil durante la ejecución.
 *
 * <p>Efecto esperado del catch:
 * puede ocurrir como máximo una vez por ejecución y debe aumentar en 40% la
 * resistencia de todos los bunkers restantes, sin que ninguna supere 1.0.</p>
 */
public class DisturbioCivilException extends OperacionException {
    private static final long serialVersionUID = 1L;

    public DisturbioCivilException(String message) {
        super(message);
    }

    public DisturbioCivilException(String message, Throwable cause) {
        super(message, cause);
    }
}
