package modelo;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Centraliza los bunkers y toda la auditoría monetaria global.
 */
public class CentroComando {
    private final HashMap<Integer, BunkerObjetivo> bunkers;
    private BigDecimal valorTotalInicial;
    private BigDecimal totalIncautado;
    private BigDecimal totalSiniestrado;
    private BigDecimal totalQuemadoAlerta;
    private BigDecimal totalRemanente;

    public CentroComando() {
        this(16);
    }

    public CentroComando(int capacidadInicialBunkers) {
        if (capacidadInicialBunkers <= 0) {
            throw new IllegalArgumentException("La capacidad inicial de bunkers debe ser positiva.");
        }
        int capacidadMapa = (int) Math.ceil(capacidadInicialBunkers / 0.75d) + 1;
        this.bunkers = new HashMap<>(capacidadMapa);
        this.valorTotalInicial = BigDecimal.ZERO;
        this.totalIncautado = BigDecimal.ZERO;
        this.totalSiniestrado = BigDecimal.ZERO;
        this.totalQuemadoAlerta = BigDecimal.ZERO;
        this.totalRemanente = BigDecimal.ZERO;
    }

    public void registrarBunker(BunkerObjetivo bunker) {
        if (bunker == null) {
            throw new IllegalArgumentException("El bunker a registrar no puede ser nulo.");
        }
        int siguienteIdEsperado = bunkers.size() + 1;
        if (bunker.getId() != siguienteIdEsperado) {
            throw new IllegalArgumentException(
                    "Los bunkers deben registrarse en orden correlativo. Esperado: "
                            + siguienteIdEsperado + ", recibido: " + bunker.getId()
            );
        }
        bunkers.put(bunker.getId(), bunker);
    }

    public BunkerObjetivo obtenerBunker(int id) {
        return bunkers.get(id);
    }

    public Collection<BunkerObjetivo> obtenerBunkers() {
        return Collections.unmodifiableCollection(bunkers.values());
    }

    public Map<Integer, BunkerObjetivo> obtenerMapaBunkers() {
        return Collections.unmodifiableMap(bunkers);
    }

    public void establecerValorTotalInicial(BigDecimal valorTotalInicial) {
        this.valorTotalInicial = validarMontoNoNegativo(valorTotalInicial, "valorTotalInicial");
    }

    public void acumularIncautado(BigDecimal monto) {
        totalIncautado = totalIncautado.add(validarMontoNoNegativo(monto, "totalIncautado"));
    }

    public void descontarIncautado(BigDecimal monto) {
        BigDecimal montoValidado = validarMontoNoNegativo(monto, "totalIncautado");
        if (totalIncautado.compareTo(montoValidado) < 0) {
            throw new IllegalArgumentException("No se puede descontar mas incautado del acumulado actual.");
        }
        totalIncautado = totalIncautado.subtract(montoValidado);
    }

    public void acumularSiniestrado(BigDecimal monto) {
        totalSiniestrado = totalSiniestrado.add(validarMontoNoNegativo(monto, "totalSiniestrado"));
    }

    public void acumularQuemadoAlerta(BigDecimal monto) {
        totalQuemadoAlerta = totalQuemadoAlerta.add(validarMontoNoNegativo(monto, "totalQuemadoAlerta"));
    }

    public void acumularRemanente(BigDecimal monto) {
        totalRemanente = totalRemanente.add(validarMontoNoNegativo(monto, "totalRemanente"));
    }

    public void descontarRemanente(BigDecimal monto) {
        BigDecimal montoValidado = validarMontoNoNegativo(monto, "totalRemanente");
        if (totalRemanente.compareTo(montoValidado) < 0) {
            throw new IllegalArgumentException("No se puede descontar mas remanente del acumulado actual.");
        }
        totalRemanente = totalRemanente.subtract(montoValidado);
    }

    public void reiniciarAuditoria() {
        valorTotalInicial = BigDecimal.ZERO;
        totalIncautado = BigDecimal.ZERO;
        totalSiniestrado = BigDecimal.ZERO;
        totalQuemadoAlerta = BigDecimal.ZERO;
        totalRemanente = BigDecimal.ZERO;
    }

    public BigDecimal calcularValorTotalInicialDesdeBunkers() {
        BigDecimal total = BigDecimal.ZERO;
        for (BunkerObjetivo bunker : bunkers.values()) {
            for (ActivoIncautado activo : bunker.getAlmacen()) {
                total = total.add(activo.getValorNominal());
            }
        }
        return total;
    }

    public BigDecimal calcularTotalRemanenteDesdeBunkers() {
        BigDecimal total = BigDecimal.ZERO;
        for (BunkerObjetivo bunker : bunkers.values()) {
            total = total.add(bunker.calcularValorTotalAlmacen());
        }
        return total;
    }

    public void cerrarAuditoriaFinal() {
        totalRemanente = calcularTotalRemanenteDesdeBunkers();
    }

    public BigDecimal calcularDiferenciaContable() {
        return valorTotalInicial
                .subtract(totalIncautado)
                .subtract(totalSiniestrado)
                .subtract(totalQuemadoAlerta)
                .subtract(totalRemanente);
    }

    public BigDecimal getValorTotalInicial() {
        return valorTotalInicial;
    }

    public BigDecimal getTotalIncautado() {
        return totalIncautado;
    }

    public BigDecimal getTotalSiniestrado() {
        return totalSiniestrado;
    }

    public BigDecimal getTotalQuemadoAlerta() {
        return totalQuemadoAlerta;
    }

    public BigDecimal getTotalRemanente() {
        return totalRemanente;
    }

    private BigDecimal validarMontoNoNegativo(BigDecimal monto, String nombreCampo) {
        if (monto == null) {
            throw new IllegalArgumentException("El monto para " + nombreCampo + " no puede ser nulo.");
        }
        if (monto.signum() < 0) {
            throw new IllegalArgumentException("El monto para " + nombreCampo + " no puede ser negativo.");
        }
        return monto;
    }
}
