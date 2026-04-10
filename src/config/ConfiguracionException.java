package config;

/**
 * Indica errores de carga o validación de configuración.
 */
public class ConfiguracionException extends Exception {
    private static final long serialVersionUID = 1L;

    public ConfiguracionException(String message) {
        super(message);
    }

    public ConfiguracionException(String message, Throwable cause) {
        super(message, cause);
    }
}
