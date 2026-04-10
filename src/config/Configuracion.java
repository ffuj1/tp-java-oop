package config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * Carga y expone la configuración obligatoria del proyecto.
 */
public class Configuracion {
    private static final String CLAVE_SEED = "seed";
    private static final String CLAVE_PROBABILIDAD_SINIESTRO = "probabilidadSiniestro";
    private static final String CLAVE_PROBABILIDAD_VEEDURIA = "probabilidadVeeduria";
    private static final String CLAVE_CAPACIDAD_MOCHILA = "capacidadMochila";
    private static final String CLAVE_CANTIDAD_BUNKERS = "cantidadBunkers";
    private static final String CLAVE_ARCHIVO_LOG = "archivoLog";
    private static final String ARCHIVO_LOG_POR_DEFECTO = "registro_federal.log";

    private final long seed;
    private final double probabilidadSiniestro;
    private final double probabilidadVeeduria;
    private final int capacidadMochila;
    private final int cantidadBunkers;
    private final String archivoLog;

    public Configuracion(long seed, double probabilidadSiniestro, double probabilidadVeeduria,
                         int capacidadMochila, int cantidadBunkers, String archivoLog) {
        validarSeed(seed);
        validarProbabilidad(probabilidadSiniestro, CLAVE_PROBABILIDAD_SINIESTRO);
        validarProbabilidad(probabilidadVeeduria, CLAVE_PROBABILIDAD_VEEDURIA);
        validarEnteroPositivo(capacidadMochila, CLAVE_CAPACIDAD_MOCHILA);
        validarEnteroPositivo(cantidadBunkers, CLAVE_CANTIDAD_BUNKERS);
        if (archivoLog == null || archivoLog.trim().isEmpty()) {
            throw new IllegalArgumentException("El archivo de log opcional no puede ser vacio.");
        }
        this.seed = seed;
        this.probabilidadSiniestro = probabilidadSiniestro;
        this.probabilidadVeeduria = probabilidadVeeduria;
        this.capacidadMochila = capacidadMochila;
        this.cantidadBunkers = cantidadBunkers;
        this.archivoLog = archivoLog.trim();
    }

    public static Configuracion cargarDesdeArchivo(String rutaArchivo) throws ConfiguracionException {
        Properties properties = new Properties();
        try (FileInputStream inputStream = new FileInputStream(rutaArchivo)) {
            properties.load(inputStream);
        } catch (FileNotFoundException e) {
            throw new ConfiguracionException("No existe el archivo de configuracion obligatorio: " + rutaArchivo, e);
        } catch (IOException e) {
            throw new ConfiguracionException("No se pudo leer el archivo de configuración: " + rutaArchivo, e);
        }

        long seed = parseLongObligatorio(properties, CLAVE_SEED);
        double probabilidadSiniestro = parseDoubleObligatorio(properties, CLAVE_PROBABILIDAD_SINIESTRO);
        double probabilidadVeeduria = parseDoubleObligatorio(properties, CLAVE_PROBABILIDAD_VEEDURIA);
        int capacidadMochila = parseIntObligatorio(properties, CLAVE_CAPACIDAD_MOCHILA);
        int cantidadBunkers = parseIntObligatorio(properties, CLAVE_CANTIDAD_BUNKERS);
        String archivoLog = parseStringOpcional(properties, CLAVE_ARCHIVO_LOG, ARCHIVO_LOG_POR_DEFECTO);

        try {
            return new Configuracion(
                    seed,
                    probabilidadSiniestro,
                    probabilidadVeeduria,
                    capacidadMochila,
                    cantidadBunkers,
                    archivoLog
            );
        } catch (IllegalArgumentException e) {
            throw new ConfiguracionException("Configuracion invalida en " + rutaArchivo + ": " + e.getMessage(), e);
        }
    }

    public long getSeed() {
        return seed;
    }

    public double getProbabilidadSiniestro() {
        return probabilidadSiniestro;
    }

    public double getProbabilidadVeeduria() {
        return probabilidadVeeduria;
    }

    public int getCapacidadMochila() {
        return capacidadMochila;
    }

    public int getCantidadBunkers() {
        return cantidadBunkers;
    }

    public String getArchivoLog() {
        return archivoLog;
    }

    private static String getObligatoria(Properties properties, String clave) throws ConfiguracionException {
        String valor = properties.getProperty(clave);
        if (valor == null || valor.trim().isEmpty()) {
            throw new ConfiguracionException("Falta la clave obligatoria: " + clave);
        }
        return valor.trim();
    }

    private static int parseIntObligatorio(Properties properties, String clave) throws ConfiguracionException {
        try {
            return Integer.parseInt(getObligatoria(properties, clave));
        } catch (NumberFormatException e) {
            throw new ConfiguracionException("La clave " + clave + " debe ser un entero válido.", e);
        }
    }

    private static long parseLongObligatorio(Properties properties, String clave) throws ConfiguracionException {
        try {
            return Long.parseLong(getObligatoria(properties, clave));
        } catch (NumberFormatException e) {
            throw new ConfiguracionException("La clave " + clave + " debe ser un entero largo válido.", e);
        }
    }

    private static double parseDoubleObligatorio(Properties properties, String clave) throws ConfiguracionException {
        try {
            return Double.parseDouble(getObligatoria(properties, clave));
        } catch (NumberFormatException e) {
            throw new ConfiguracionException("La clave " + clave + " debe ser un decimal válido.", e);
        }
    }

    private static String parseStringOpcional(Properties properties, String clave, String valorPorDefecto) {
        String valor = properties.getProperty(clave);
        if (valor == null || valor.trim().isEmpty()) {
            return valorPorDefecto;
        }
        return valor.trim();
    }

    private static void validarSeed(long seed) {
        if (seed < 0L) {
            throw new IllegalArgumentException("La seed debe ser un entero no negativo.");
        }
    }

    private static void validarProbabilidad(double probabilidad, String clave) {
        if (Double.isNaN(probabilidad) || probabilidad < 0.0d || probabilidad > 1.0d) {
            throw new IllegalArgumentException("La clave " + clave + " debe estar en el rango [0.0, 1.0].");
        }
    }

    private static void validarEnteroPositivo(int valor, String clave) {
        if (valor <= 0) {
            throw new IllegalArgumentException("La clave " + clave + " debe ser un entero positivo.");
        }
    }
}
