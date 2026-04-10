package app;

import config.Configuracion;
import config.ConfiguracionException;
import logging.OperacionLoggerFactory;
import servicio.GeneradorBunkers;
import servicio.SimuladorOperacion;
import util.RandomProvider;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Punto de entrada de la aplicación.
 */
public final class SoberaniaFederal {

    private SoberaniaFederal() {
        // Evita instanciación accidental.
    }

    public static void main(String[] args) {
        Logger logger = null;
        try {
            Configuracion configuracion = Configuracion.cargarDesdeArchivo("soberania.properties");
            logger = OperacionLoggerFactory.crearLogger(configuracion.getArchivoLog());
            RandomProvider.inicializar(configuracion.getSeed());

            GeneradorBunkers generadorBunkers = new GeneradorBunkers(configuracion, RandomProvider.getRandom());
            SimuladorOperacion simuladorOperacion = new SimuladorOperacion(
                    configuracion,
                    logger,
                    generadorBunkers,
                    RandomProvider.getRandom()
            );

            String reporteFinal = simuladorOperacion.ejecutar();
            System.out.println(reporteFinal);
        } catch (ConfiguracionException | IOException e) {
            throw new IllegalStateException("No fue posible iniciar la operación: " + e.getMessage(), e);
        } finally {
            OperacionLoggerFactory.sincronizarLogger(logger);
        }
    }
}
