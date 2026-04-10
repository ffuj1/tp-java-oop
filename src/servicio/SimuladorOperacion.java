package servicio;

import config.Configuracion;
import excepciones.AlertaRadialException;
import excepciones.DisturbioCivilException;
import excepciones.MochilaSaturadaException;
import excepciones.ResistenciaSuperiorException;
import excepciones.VeeduriaExternaException;
import modelo.ActivoIncautado;
import modelo.AgenteFederal;
import modelo.BunkerObjetivo;
import modelo.CentroComando;
import util.Region;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

/**
 * Orquesta la simulación. En esta fase sólo inicializa la estructura base.
 */
public class SimuladorOperacion {
    private static final double IAS_INICIAL = 50.0d;
    private static final double IAS_MINIMO = 0.0d;
    private static final double IAS_MAXIMO = 100.0d;
    private static final double PENALIZACION_POR_BAJA = 2.0d;
    private static final double BONIFICACION_OPERATIVO_LIMPIO = 0.1d;

    private final Configuracion configuracion;
    private final Logger logger;
    private final GeneradorBunkers generadorBunkers;
    private final Random random;
    private final CuadroSituacionFinalFormatter cuadroSituacionFinalFormatter;
    private double indiceAceptacionSocial;
    private boolean disturbioCivilDisparado;
    private int bajasCivilesTotales;
    private int bunkersProcesados;
    private int bunkersExitosos;
    private int disturbiosDisparados;

    public SimuladorOperacion(Configuracion configuracion, Logger logger, GeneradorBunkers generadorBunkers, Random random) {
        this.configuracion = configuracion;
        this.logger = logger;
        this.generadorBunkers = generadorBunkers;
        this.random = random;
        this.cuadroSituacionFinalFormatter = new CuadroSituacionFinalFormatter();
        this.indiceAceptacionSocial = IAS_INICIAL;
        this.disturbioCivilDisparado = false;
        this.bajasCivilesTotales = 0;
        this.bunkersProcesados = 0;
        this.bunkersExitosos = 0;
        this.disturbiosDisparados = 0;
    }

    public String ejecutar() {
        return ejecutarConResultado().getReporteFinal();
    }

    public ResultadoSimulacion ejecutarConResultado() {
        long inicio = System.nanoTime();
        reiniciarEstadoSocial();

        CentroComando centroComando = generadorBunkers.generarCentroComandoInicial();
        AgenteFederal agenteFederal = new AgenteFederal(configuracion.getCapacidadMochila());

        for (int bunkerId = 1; bunkerId <= configuracion.getCantidadBunkers(); bunkerId++) {
            procesarBunkerPorId(bunkerId, centroComando, agenteFederal);
        }

        centroComando.cerrarAuditoriaFinal();

        long duracion = System.nanoTime() - inicio;

        String reporteFinal = construirReporteFinal(centroComando, duracion);
        return new ResultadoSimulacion(
                configuracion.getSeed(),
                bunkersProcesados,
                bunkersExitosos,
                bajasCivilesTotales,
                disturbiosDisparados,
                indiceAceptacionSocial,
                disturbioCivilDisparado,
                centroComando.getValorTotalInicial(),
                centroComando.getTotalIncautado(),
                centroComando.getTotalSiniestrado(),
                centroComando.getTotalQuemadoAlerta(),
                centroComando.getTotalRemanente(),
                centroComando.calcularDiferenciaContable(),
                duracion,
                reporteFinal
        );
    }

    public void procesarBunkerPorId(int bunkerId, CentroComando centroComando, AgenteFederal agenteFederal) {
        BunkerObjetivo bunker = centroComando.obtenerBunker(bunkerId);
        if (bunker == null) {
            throw new IllegalArgumentException("No existe el bunker con ID " + bunkerId + ".");
        }

        boolean procesadoConExito = false;
        bunkersProcesados++;

        try {
            intentarIntervencionConVeeduria(bunker, centroComando, agenteFederal);
            procesadoConExito = true;
        } catch (AlertaRadialException e) {
            manejarAlertaRadial(bunkerId, centroComando, e);
            procesadoConExito = true;
        } catch (DisturbioCivilException e) {
            procesadoConExito = bunkerFueNeutralizado(bunker);
            manejarDisturbioCivil(bunkerId, centroComando, e);
        } catch (ResistenciaSuperiorException e) {
            procesadoConExito = manejarResistenciaSuperior(bunker, centroComando, agenteFederal, e);
        } catch (MochilaSaturadaException e) {
            manejarMochilaSaturada(bunker, centroComando, e);
            procesadoConExito = true;
        } finally {
            if (procesadoConExito) {
                bunkersExitosos++;
            }
            logger.info(
                    "Bunker procesado ID=" + bunkerId
                            + " | exito=" + procesadoConExito
                            + " | activosRestantes=" + bunker.cantidadActivos()
                            + " | ias=" + indiceAceptacionSocial
            );
        }
    }

    private void intentarIntervencionConVeeduria(BunkerObjetivo bunker, CentroComando centroComando,
                                                 AgenteFederal agenteFederal)
            throws ResistenciaSuperiorException, MochilaSaturadaException,
            DisturbioCivilException, AlertaRadialException {
        try {
            evaluarVeeduriaExterna(bunker);
            ejecutarIntervencionExitosa(bunker, centroComando, agenteFederal, false);
        } catch (VeeduriaExternaException e) {
            logger.warning(
                    "Veeduria externa detectada en bunker ID=" + bunker.getId()
                            + ". Se registra demora y se fuerza intervencion sin bajas civiles. "
                            + e.getMessage()
            );
            ejecutarIntervencionExitosa(bunker, centroComando, agenteFederal, true);
        }
    }

    private void validarIntervencion(BunkerObjetivo bunker) throws ResistenciaSuperiorException {
        double eficacia = random.nextDouble();

        if (eficacia <= bunker.getNivelResistencia()) {
            throw new ResistenciaSuperiorException(
                    "La eficacia " + eficacia + " no supero la resistencia del bunker ID=" + bunker.getId() + "."
            );
        }
    }

    private void ejecutarIntervencionExitosa(BunkerObjetivo bunker, CentroComando centroComando,
                                             AgenteFederal agenteFederal, boolean sinBajasCiviles)
            throws ResistenciaSuperiorException, MochilaSaturadaException,
            DisturbioCivilException, AlertaRadialException {
        validarIntervencion(bunker);
        List<ActivoIncautado> activosRecolectados = recolectarActivosDelBunker(bunker, centroComando, agenteFederal);
        aplicarSiniestroEvidenciaSiCorresponde(
                bunker,
                centroComando,
                agenteFederal,
                activosRecolectados,
                sinBajasCiviles
        );
        registrarImpactoSocialDeIntervencion(bunker, sinBajasCiviles);
        evaluarAlertaRadial(bunker);
    }

    private List<ActivoIncautado> recolectarActivosDelBunker(BunkerObjetivo bunker, CentroComando centroComando,
                                                             AgenteFederal agenteFederal)
            throws MochilaSaturadaException {
        List<ActivoIncautado> activosRecolectados = new ArrayList<>();
        while (bunker.tieneActivos()) {
            ActivoIncautado activo = bunker.extraerActivo(0);
            try {
                agenteFederal.push(activo);
                centroComando.acumularIncautado(activo.getValorNominal());
                activosRecolectados.add(activo);
            } catch (MochilaSaturadaException e) {
                reinsertarActivoAlFrente(bunker, activo);
                throw e;
            }
        }
        return activosRecolectados;
    }

    private boolean manejarResistenciaSuperior(BunkerObjetivo bunker, CentroComando centroComando,
                                               AgenteFederal agenteFederal, ResistenciaSuperiorException exception) {
        logger.warning("Intervencion rechazada por resistencia superior en bunker ID=" + bunker.getId() + ". "
                + exception.getMessage());

        BunkerObjetivo bunkerApoyo = centroComando.obtenerMapaBunkers().get(bunker.getId() + 1);
        if (bunkerApoyo == null) {
            BigDecimal remanente = bunker.calcularValorTotalAlmacen();
            centroComando.acumularRemanente(remanente);
            logger.warning(
                    "No existe bunker de apoyo para ID=" + bunker.getId()
                            + ". Se registra repliegue y remanente por " + remanente + "."
            );
            return false;
        }

        try {
            intentarIntervencionConVeeduria(bunker, centroComando, agenteFederal);
            return true;
        } catch (AlertaRadialException e) {
            manejarAlertaRadial(bunker.getId(), centroComando, e);
            return true;
        } catch (DisturbioCivilException e) {
            boolean bunkerNeutralizado = bunkerFueNeutralizado(bunker);
            manejarDisturbioCivil(bunker.getId(), centroComando, e);
            return bunkerNeutralizado;
        } catch (ResistenciaSuperiorException e) {
            BigDecimal remanente = bunker.calcularValorTotalAlmacen();
            centroComando.acumularRemanente(remanente);
            logger.warning(
                    "El apoyo no alcanzo para neutralizar el bunker ID=" + bunker.getId()
                            + ". Se registra repliegue final y remanente por " + remanente + "."
            );
            return false;
        } catch (MochilaSaturadaException e) {
            manejarMochilaSaturada(bunker, centroComando, e);
            return true;
        }
    }

    private void manejarMochilaSaturada(BunkerObjetivo bunker, CentroComando centroComando,
                                        MochilaSaturadaException exception) {
        BigDecimal remanente = bunker.calcularValorTotalAlmacen();
        centroComando.acumularRemanente(remanente);
        logger.warning(
                "Mochila saturada durante bunker ID=" + bunker.getId()
                        + ". Se corta la recoleccion. Valor remanente registrado: " + remanente
                        + ". Detalle: " + exception.getMessage()
        );
    }

    private void reinsertarActivoAlFrente(BunkerObjetivo bunker, ActivoIncautado activo) {
        bunker.insertarActivo(0, activo);
    }

    private void evaluarVeeduriaExterna(BunkerObjetivo bunker) throws VeeduriaExternaException {
        double probabilidadBase = configuracion.getProbabilidadVeeduria();
        double probabilidadAjustada = probabilidadBase;

        if (esRegionSensible(bunker.getRegion())) {
            probabilidadAjustada = Math.min(1.0d, probabilidadBase + 0.15d);
        }

        double disparo = random.nextDouble();

        if (disparo < probabilidadAjustada) {
            throw new VeeduriaExternaException(
                    "Se activo control externo sobre el bunker ID=" + bunker.getId() + "."
            );
        }
    }

    private boolean esRegionSensible(Region region) {
        return region == Region.GBA || region == Region.ROSARIO;
    }

    private void aplicarSiniestroEvidenciaSiCorresponde(BunkerObjetivo bunker, CentroComando centroComando,
                                                        AgenteFederal agenteFederal,
                                                        List<ActivoIncautado> activosRecolectados,
                                                        boolean sinBajasCiviles) {
        if (activosRecolectados.isEmpty()) {
            return;
        }

        double disparo = random.nextDouble();
        if (disparo >= configuracion.getProbabilidadSiniestro()) {
            return;
        }

        int cantidadSiniestrada = random.nextInt(activosRecolectados.size()) + 1;
        List<ActivoIncautado> candidatos = new ArrayList<>(activosRecolectados);
        BigDecimal valorSiniestrado = BigDecimal.ZERO;

        for (int i = 0; i < cantidadSiniestrada; i++) {
            int indice = random.nextInt(candidatos.size());
            ActivoIncautado activoSiniestrado = candidatos.remove(indice);
            agenteFederal.eliminarActivo(activoSiniestrado);
            valorSiniestrado = valorSiniestrado.add(activoSiniestrado.getValorNominal());
        }

        centroComando.descontarIncautado(valorSiniestrado);
        centroComando.acumularSiniestrado(valorSiniestrado);
        logger.warning(
                "Siniestro de evidencia en bunker ID=" + bunker.getId()
                        + " | activosAfectados=" + cantidadSiniestrada
                        + " | valorSiniestrado=" + valorSiniestrado
                        + " | modoSinBajasCiviles=" + sinBajasCiviles
        );
    }

    /**
     * En esta fase una baja civil se modela como una afectación colateral durante una
     * intervención exitosa sin veeduría externa. Puede haber como máximo una baja por
     * bunker procesado y su probabilidad depende del nivel de resistencia del bunker.
     */
    private void registrarImpactoSocialDeIntervencion(BunkerObjetivo bunker, boolean sinBajasCiviles)
            throws DisturbioCivilException {
        int bajasEnIntervencion = calcularBajasCiviles(bunker, sinBajasCiviles);
        if (bajasEnIntervencion > 0) {
            bajasCivilesTotales += bajasEnIntervencion;
            ajustarIndiceAceptacionSocial(-(bajasEnIntervencion * PENALIZACION_POR_BAJA));
            logger.warning(
                    "Se registraron bajas civiles en bunker ID=" + bunker.getId()
                            + " | bajas=" + bajasEnIntervencion
                            + " | IAS=" + indiceAceptacionSocial
            );
        } else {
            ajustarIndiceAceptacionSocial(BONIFICACION_OPERATIVO_LIMPIO);
        }

        verificarDisturbioCivil(bunker.getId());
    }

    private int calcularBajasCiviles(BunkerObjetivo bunker, boolean sinBajasCiviles) {
        if (sinBajasCiviles) {
            return 0;
        }

        double umbralBaja = Math.min(1.0d, bunker.getNivelResistencia());
        double disparo = random.nextDouble();

        return disparo < umbralBaja ? 1 : 0;
    }

    private void ajustarIndiceAceptacionSocial(double delta) {
        indiceAceptacionSocial += delta;
        if (indiceAceptacionSocial < IAS_MINIMO) {
            indiceAceptacionSocial = IAS_MINIMO;
        }
        if (indiceAceptacionSocial > IAS_MAXIMO) {
            indiceAceptacionSocial = IAS_MAXIMO;
        }
    }

    private void verificarDisturbioCivil(int bunkerActualId) throws DisturbioCivilException {
        if (!disturbioCivilDisparado && indiceAceptacionSocial < 15.0d) {
            disturbioCivilDisparado = true;
            throw new DisturbioCivilException(
                    "El IAS cayo por debajo de 15 luego del bunker ID=" + bunkerActualId
                            + ". IAS actual=" + indiceAceptacionSocial
            );
        }
    }

    private void manejarDisturbioCivil(int bunkerActualId, CentroComando centroComando,
                                       DisturbioCivilException exception) {
        disturbiosDisparados++;
        logger.warning("Disturbio civil disparado. " + exception.getMessage());
        int bunkersAfectados = 0;
        for (int bunkerId = bunkerActualId + 1; bunkerId <= configuracion.getCantidadBunkers(); bunkerId++) {
            BunkerObjetivo bunkerRestante = centroComando.obtenerBunker(bunkerId);
            if (bunkerRestante == null) {
                continue;
            }
            double resistenciaActual = bunkerRestante.getNivelResistencia();
            double resistenciaAumentada = Math.min(1.0d, resistenciaActual * 1.4d);
            bunkerRestante.setNivelResistencia(resistenciaAumentada);
            if (resistenciaAumentada != resistenciaActual) {
                bunkersAfectados++;
            }
        }
        logger.warning("Disturbio aplicado sobre bunkers restantes: " + bunkersAfectados + ".");
    }

    /**
     * Decision de diseño:
     * una alerta radial puede activarse al caer un bunker exitosamente.
     * La probabilidad usada es nivelResistencia / 2, con rango efectivo [0.05, 0.45],
     * para modelar que bunkers mas resistentes tienen mas chances de dejar defensas
     * reactivas al ser neutralizados.
     */
    private void evaluarAlertaRadial(BunkerObjetivo bunker) throws AlertaRadialException {
        double probabilidadAlerta = bunker.getNivelResistencia() / 2.0d;
        double disparo = random.nextDouble();

        if (disparo < probabilidadAlerta) {
            throw new AlertaRadialException(
                    "El bunker ID=" + bunker.getId() + " activo una alerta radial al ser neutralizado."
            );
        }
    }

    /**
     * Decision de diseño:
     * "parte de sus activos" se interpreta como la mitad del almacen actual redondeada
     * hacia abajo, con un minimo de 1 si el bunker vecino posee activos. La destruccion
     * se aplica sobre los primeros activos del almacen para mantener determinismo.
     */
    private void manejarAlertaRadial(int bunkerIdOrigen, CentroComando centroComando,
                                     AlertaRadialException exception) {
        logger.warning("Alerta radial disparada. " + exception.getMessage());
        BigDecimal totalQuemado = BigDecimal.ZERO;

        int[] vecinos = new int[]{bunkerIdOrigen - 1, bunkerIdOrigen + 1};
        for (int vecinoId : vecinos) {
            BunkerObjetivo vecino = centroComando.obtenerMapaBunkers().get(vecinoId);
            if (vecino == null || !vecino.tieneActivos()) {
                continue;
            }

            int cantidadADestruir = Math.max(1, vecino.cantidadActivos() / 2);
            BigDecimal quemadoVecino = BigDecimal.ZERO;

            for (int i = 0; i < cantidadADestruir && vecino.tieneActivos(); i++) {
                ActivoIncautado destruido = vecino.extraerActivo(0);
                quemadoVecino = quemadoVecino.add(destruido.getValorNominal());
            }

            if (vecinoId < bunkerIdOrigen && quemadoVecino.signum() > 0) {
                centroComando.descontarRemanente(quemadoVecino);
            }
            totalQuemado = totalQuemado.add(quemadoVecino);
            logger.warning(
                    "Danio por alerta radial sobre bunker vecino ID=" + vecinoId
                            + " | activosDestruidos=" + cantidadADestruir
                            + " | valorQuemado=" + quemadoVecino
            );
        }

        if (totalQuemado.signum() > 0) {
            centroComando.acumularQuemadoAlerta(totalQuemado);
        }
    }

    private boolean bunkerFueNeutralizado(BunkerObjetivo bunker) {
        return bunker != null && !bunker.tieneActivos();
    }

    private void reiniciarEstadoSocial() {
        indiceAceptacionSocial = IAS_INICIAL;
        disturbioCivilDisparado = false;
        bajasCivilesTotales = 0;
        bunkersProcesados = 0;
        bunkersExitosos = 0;
        disturbiosDisparados = 0;
    }

    private String construirReporteFinal(CentroComando centroComando, long duracionNanos) {
        return cuadroSituacionFinalFormatter.formatear(
                configuracion,
                centroComando,
                duracionNanos,
                bunkersProcesados,
                bunkersExitosos,
                bajasCivilesTotales,
                indiceAceptacionSocial,
                disturbioCivilDisparado
        );
    }
}
