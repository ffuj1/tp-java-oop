package servicio;

import config.Configuracion;
import modelo.ActivoIncautado;
import modelo.BunkerObjetivo;
import modelo.CentroComando;
import util.Region;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Genera la red inicial de bunkers usando la fuente de aleatoriedad compartida.
 */
public class GeneradorBunkers {
    private static final int MIN_ACTIVOS_POR_BUNKER = 1;
    private static final int MAX_ACTIVOS_POR_BUNKER = 10;
    private static final long MIN_VALOR_ACTIVO = 1_000L;
    private static final long MAX_VALOR_ACTIVO = 10_000_000L;
    private static final int INTEGRIDAD_INICIAL = 100;

    private final Configuracion configuracion;
    private final Random random;
    private int proximoIdActivo;

    public GeneradorBunkers(Configuracion configuracion, Random random) {
        if (configuracion == null) {
            throw new IllegalArgumentException("La configuracion no puede ser nula.");
        }
        if (random == null) {
            throw new IllegalArgumentException("La instancia compartida de Random no puede ser nula.");
        }
        this.configuracion = configuracion;
        this.random = random;
        this.proximoIdActivo = 1;
    }

    public CentroComando generarCentroComandoInicial() {
        CentroComando centroComando = new CentroComando(configuracion.getCantidadBunkers());
        BigDecimal valorTotalInicial = BigDecimal.ZERO;

        for (int bunkerId = 1; bunkerId <= configuracion.getCantidadBunkers(); bunkerId++) {
            BunkerObjetivo bunker = crearBunkerBase(bunkerId);
            centroComando.registrarBunker(bunker);
            valorTotalInicial = valorTotalInicial.add(bunker.calcularValorTotalAlmacen());
        }

        centroComando.establecerValorTotalInicial(valorTotalInicial);
        return centroComando;
    }

    private BunkerObjetivo crearBunkerBase(int id) {
        int cantidadActivos = random.nextInt(MAX_ACTIVOS_POR_BUNKER) + MIN_ACTIVOS_POR_BUNKER;
        List<ActivoIncautado> activos = new ArrayList<>(cantidadActivos);

        for (int i = 0; i < cantidadActivos; i++) {
            activos.add(crearActivo());
        }

        double resistencia = (random.nextInt(9) + 1) / 10.0d;
        Region region = Region.desdeIndice(id - 1);
        return new BunkerObjetivo(id, activos, resistencia, region);
    }

    private ActivoIncautado crearActivo() {
        long valor = MIN_VALOR_ACTIVO + random.nextInt((int) (MAX_VALOR_ACTIVO - MIN_VALOR_ACTIVO + 1L));
        int idActivo = proximoIdActivo;
        proximoIdActivo++;
        return new ActivoIncautado(idActivo, "DIVISA", BigDecimal.valueOf(valor), INTEGRIDAD_INICIAL);
    }
}
