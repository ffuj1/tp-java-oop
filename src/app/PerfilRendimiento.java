package app;

import config.Configuracion;
import logging.OperacionLoggerFactory;
import servicio.GeneradorBunkers;
import servicio.ResultadoSimulacion;
import servicio.SimuladorOperacion;
import util.RandomProvider;
import java.util.logging.Logger;

/**
 * Runner simple para medir rendimiento sin depender del archivo de configuracion principal.
 */
public final class PerfilRendimiento {

    private PerfilRendimiento() {
    }

    public static void main(String[] args) throws Exception {
        Logger logger = null;
        long seed = args.length > 0 ? Long.parseLong(args[0]) : 12345L;
        int cantidadBunkers = args.length > 1 ? Integer.parseInt(args[1]) : 50000;
        int capacidadMochila = args.length > 2 ? Integer.parseInt(args[2]) : 50000;
        double probabilidadSiniestro = args.length > 3 ? Double.parseDouble(args[3]) : 0.10d;
        double probabilidadVeeduria = args.length > 4 ? Double.parseDouble(args[4]) : 0.05d;

        Configuracion configuracion = new Configuracion(
                seed,
                probabilidadSiniestro,
                probabilidadVeeduria,
                capacidadMochila,
                cantidadBunkers,
                "registro_federal.log"
        );

        try {
            logger = OperacionLoggerFactory.crearLogger(configuracion.getArchivoLog());
            RandomProvider.reinicializar(configuracion.getSeed());

            SimuladorOperacion simulador = new SimuladorOperacion(
                    configuracion,
                    logger,
                    new GeneradorBunkers(configuracion, RandomProvider.getRandom()),
                    RandomProvider.getRandom()
            );

            ResultadoSimulacion resultado = simulador.ejecutarConResultado();

            System.out.println("Seed: " + resultado.getSeed());
            System.out.println("Bunkers procesados: " + resultado.getBunkersProcesados());
            System.out.println("Bunkers exitosos: " + resultado.getBunkersExitosos());
            System.out.println("Tiempo total (ns): " + resultado.getTiempoTotalNanos());
            System.out.println("Diferencia contable: " + resultado.getDiferenciaContable());
        } finally {
            OperacionLoggerFactory.sincronizarLogger(logger);
        }
    }
}
