package app;

import config.Configuracion;
import excepciones.MochilaSaturadaException;
import logging.OperacionLoggerFactory;
import modelo.ActivoIncautado;
import modelo.AgenteFederal;
import modelo.BunkerObjetivo;
import modelo.CentroComando;
import servicio.GeneradorBunkers;
import servicio.ResultadoSimulacion;
import servicio.SimuladorOperacion;
import util.RandomProvider;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.logging.Logger;

/**
 * Harness manual de verificaciones tecnicas sin frameworks externos.
 */
public final class VerificadorTecnico {
    private static int pruebasEjecutadas = 0;
    private static int pruebasFallidas = 0;

    private VerificadorTecnico() {
    }

    public static void main(String[] args) throws Exception {
        Logger logger = null;
        try {
            logger = OperacionLoggerFactory.crearLogger("registro_federal.log");

            ejecutarPrueba("misma seed produce mismo resultado", VerificadorTecnico::verificarMismaSeedMismoResultado);
            ejecutarPrueba("seed distinta produce resultado distinto", VerificadorTecnico::verificarSeedDistintaResultadoDistinto);
            ejecutarPrueba("push pop de mochila", VerificadorTecnico::verificarPushPopMochila);
            ejecutarPrueba("saturacion de mochila", VerificadorTecnico::verificarSaturacionMochila);
            ejecutarPrueba("acceso a vecinos por hashmap", VerificadorTecnico::verificarVecinosPorHashMap);
            ejecutarPrueba("cierre exacto de auditoria", VerificadorTecnico::verificarCierreExactoAuditoria);
            ejecutarPrueba("ias acotado entre 0 y 100", VerificadorTecnico::verificarIASAcotado);
            ejecutarPrueba("disturbio civil como maximo una vez", VerificadorTecnico::verificarDisturbioSoloUnaVez);

            imprimirResumen();
            if (pruebasFallidas > 0) {
                throw new IllegalStateException("Se detectaron fallas en las verificaciones tecnicas.");
            }
        } finally {
            OperacionLoggerFactory.sincronizarLogger(logger);
        }
    }

    private static void verificarMismaSeedMismoResultado() throws Exception {
        ResultadoSimulacion resultadoA = ejecutarSimulacion(nuevaConfiguracion(12345L, 3, 5, 0.10d, 0.05d));
        ResultadoSimulacion resultadoB = ejecutarSimulacion(nuevaConfiguracion(12345L, 3, 5, 0.10d, 0.05d));
        assertEquals(resultadoA.firmaDeterministica(), resultadoB.firmaDeterministica(), "Las firmas no coinciden.");
    }

    private static void verificarSeedDistintaResultadoDistinto() throws Exception {
        ResultadoSimulacion resultadoA = ejecutarSimulacion(nuevaConfiguracion(12345L, 3, 5, 0.10d, 0.05d));
        ResultadoSimulacion resultadoB = ejecutarSimulacion(nuevaConfiguracion(54321L, 3, 5, 0.10d, 0.05d));
        assertTrue(!resultadoA.firmaDeterministica().equals(resultadoB.firmaDeterministica()),
                "Dos seeds distintas produjeron la misma firma deterministica.");
    }

    private static void verificarPushPopMochila() throws Exception {
        AgenteFederal agente = new AgenteFederal(2);
        ActivoIncautado primero = new ActivoIncautado(1, "DIVISA", new BigDecimal("1000"), 100);
        ActivoIncautado segundo = new ActivoIncautado(2, "ORO", new BigDecimal("2000"), 90);

        agente.push(primero);
        agente.push(segundo);

        assertEquals(2, agente.size(), "El tamano de la mochila deberia ser 2.");
        assertEquals(segundo, agente.peek(), "El tope deberia ser el segundo activo.");
        assertEquals(segundo, agente.pop(), "El primer pop deberia devolver el segundo activo.");
        assertEquals(primero, agente.pop(), "El segundo pop deberia devolver el primer activo.");
        assertTrue(agente.isEmpty(), "La mochila deberia quedar vacia.");
    }

    private static void verificarSaturacionMochila() throws Exception {
        AgenteFederal agente = new AgenteFederal(1);
        agente.push(new ActivoIncautado(1, "DIVISA", new BigDecimal("1000"), 100));
        try {
            agente.push(new ActivoIncautado(2, "ORO", new BigDecimal("2000"), 100));
            throw new IllegalStateException("Se esperaba MochilaSaturadaException.");
        } catch (MochilaSaturadaException e) {
            assertTrue(e.getMessage().contains("saturada"), "El mensaje de saturacion no es descriptivo.");
        }
    }

    private static void verificarVecinosPorHashMap() throws Exception {
        Configuracion configuracion = nuevaConfiguracion(12345L, 5, 5, 0.10d, 0.05d);
        RandomProvider.reinicializar(configuracion.getSeed());
        CentroComando centroComando = new GeneradorBunkers(configuracion, RandomProvider.getRandom())
                .generarCentroComandoInicial();

        BunkerObjetivo bunkerTres = centroComando.obtenerMapaBunkers().get(3);
        assertTrue(bunkerTres != null, "Debe existir el bunker 3.");
        assertTrue(centroComando.obtenerMapaBunkers().containsKey(2), "Debe existir el vecino ID-1.");
        assertTrue(centroComando.obtenerMapaBunkers().containsKey(4), "Debe existir el vecino ID+1.");
    }

    private static void verificarCierreExactoAuditoria() throws Exception {
        ResultadoSimulacion resultado = ejecutarSimulacion(nuevaConfiguracion(12345L, 3, 5, 0.10d, 0.05d));
        assertEquals(BigDecimal.ZERO, resultado.getDiferenciaContable(), "La diferencia contable debe ser cero.");
    }

    private static void verificarIASAcotado() throws Exception {
        ResultadoSimulacion resultado = ejecutarSimulacion(nuevaConfiguracion(99999L, 40, 40, 0.10d, 0.00d));
        assertTrue(resultado.getIndiceAceptacionSocial() >= 0.0d, "El IAS no puede ser negativo.");
        assertTrue(resultado.getIndiceAceptacionSocial() <= 100.0d, "El IAS no puede superar 100.");
    }

    private static void verificarDisturbioSoloUnaVez() throws Exception {
        ResultadoSimulacion resultado = ejecutarSimulacion(nuevaConfiguracion(77777L, 60, 60, 0.10d, 0.00d));
        assertTrue(resultado.getDisturbiosDisparados() <= 1, "El disturbio civil se disparo mas de una vez.");
    }

    private static ResultadoSimulacion ejecutarSimulacion(Configuracion configuracion) throws IOException {
        Logger logger = OperacionLoggerFactory.crearLogger(configuracion.getArchivoLog());
        RandomProvider.reinicializar(configuracion.getSeed());
        SimuladorOperacion simulador = new SimuladorOperacion(
                configuracion,
                logger,
                new GeneradorBunkers(configuracion, RandomProvider.getRandom()),
                RandomProvider.getRandom()
        );
        ResultadoSimulacion resultado = simulador.ejecutarConResultado();
        OperacionLoggerFactory.sincronizarLogger(logger);
        return resultado;
    }

    private static Configuracion nuevaConfiguracion(long seed, int cantidadBunkers, int capacidadMochila,
                                                    double probabilidadSiniestro, double probabilidadVeeduria) {
        return new Configuracion(
                seed,
                probabilidadSiniestro,
                probabilidadVeeduria,
                capacidadMochila,
                cantidadBunkers,
                "registro_federal.log"
        );
    }

    private static void ejecutarPrueba(String nombre, Verificacion verificacion) throws Exception {
        pruebasEjecutadas++;
        try {
            verificacion.ejecutar();
            System.out.println("[OK] " + nombre);
        } catch (Exception e) {
            pruebasFallidas++;
            System.out.println("[FALLA] " + nombre + " -> " + e.getMessage());
        }
    }

    private static void imprimirResumen() {
        System.out.println();
        System.out.println("Pruebas ejecutadas: " + pruebasEjecutadas);
        System.out.println("Pruebas fallidas: " + pruebasFallidas);
        System.out.println("Estado: " + (pruebasFallidas == 0 ? "OK" : "CON FALLAS"));
    }

    private static void assertEquals(Object esperado, Object actual, String mensaje) {
        if (esperado == null ? actual != null : !esperado.equals(actual)) {
            throw new IllegalStateException(mensaje + " Esperado=" + esperado + ", actual=" + actual);
        }
    }

    private static void assertTrue(boolean condicion, String mensaje) {
        if (!condicion) {
            throw new IllegalStateException(mensaje);
        }
    }

    @FunctionalInterface
    private interface Verificacion {
        void ejecutar() throws Exception;
    }
}
