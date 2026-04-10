package logging;

import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.MemoryHandler;
import java.util.logging.SimpleFormatter;

/**
 * Centraliza la creación del logger de la operación.
 */
public final class OperacionLoggerFactory {
    private static final String NOMBRE_LOGGER = "OperacionSoberaniaFederal";
    private static final Level NIVEL_LOGGER = Level.INFO;
    private static final int BUFFER_LOGS = 262144;
    private static String rutaConfigurada;

    private OperacionLoggerFactory() {
        // Utilidad estática.
    }

    public static synchronized Logger crearLogger(String rutaArchivo) throws IOException {
        if (rutaArchivo == null || rutaArchivo.trim().isEmpty()) {
            throw new IllegalArgumentException("La ruta del archivo de log es obligatoria.");
        }

        String rutaNormalizada = rutaArchivo.trim();
        Logger logger = Logger.getLogger(NOMBRE_LOGGER);
        logger.setUseParentHandlers(false);
        logger.setLevel(NIVEL_LOGGER);

        if (!rutaNormalizada.equals(rutaConfigurada)) {
            limpiarHandlers(logger);

            FileHandler fileHandler = new FileHandler(rutaNormalizada, false);
            fileHandler.setFormatter(new SimpleFormatter());
            fileHandler.setLevel(Level.ALL);

            MemoryHandler memoryHandler = new MemoryHandler(fileHandler, BUFFER_LOGS, Level.SEVERE);
            memoryHandler.setLevel(Level.ALL);
            logger.addHandler(memoryHandler);

            rutaConfigurada = rutaNormalizada;
        }

        return logger;
    }

    public static synchronized void sincronizarLogger(Logger logger) {
        if (logger == null) {
            return;
        }
        for (Handler handler : logger.getHandlers()) {
            if (handler instanceof MemoryHandler memoryHandler) {
                memoryHandler.push();
            }
            handler.flush();
        }
    }

    private static void limpiarHandlers(Logger logger) {
        for (Handler handler : logger.getHandlers()) {
            handler.flush();
            handler.close();
            logger.removeHandler(handler);
        }
    }
}
