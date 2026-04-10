package excepciones;

/**
 * Se dispara cuando una veeduría externa interrumpe o condiciona un operativo.
 *
 * <p>Efecto esperado del catch:
 * la intervención debe continuar bajo la restricción de no producir bajas
 * civiles y debe registrarse una demora en nivel WARNING.</p>
 */
public class VeeduriaExternaException extends OperacionException {
    private static final long serialVersionUID = 1L;

    public VeeduriaExternaException(String message) {
        super(message);
    }

    public VeeduriaExternaException(String message, Throwable cause) {
        super(message, cause);
    }
}
