package util;

import java.util.Random;

/**
 * Expone una única instancia compartida de Random para toda la ejecución.
 */
public final class RandomProvider {
    private static Random random;

    private RandomProvider() {
        // Utilidad estática.
    }

    public static synchronized void inicializar(long seed) {
        if (random == null) {
            random = new Random(seed);
        }
    }

    /**
     * Reinicia la fuente compartida para simular una nueva ejecucion dentro del
     * mismo proceso. Se usa en runners tecnicos y no en el flujo principal.
     */
    public static synchronized void reinicializar(long seed) {
        random = new Random(seed);
    }

    public static synchronized Random getRandom() {
        if (random == null) {
            throw new IllegalStateException("RandomProvider no fue inicializado.");
        }
        return random;
    }
}
