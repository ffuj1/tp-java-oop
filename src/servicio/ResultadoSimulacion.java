package servicio;

import java.math.BigDecimal;

/**
 * Snapshot inmutable del estado final de una simulacion.
 */
public class ResultadoSimulacion {
    private final long seed;
    private final int bunkersProcesados;
    private final int bunkersExitosos;
    private final int bajasCivilesTotales;
    private final int disturbiosDisparados;
    private final double indiceAceptacionSocial;
    private final boolean disturbioCivilDisparado;
    private final BigDecimal valorTotalInicial;
    private final BigDecimal totalIncautado;
    private final BigDecimal totalSiniestrado;
    private final BigDecimal totalQuemadoAlerta;
    private final BigDecimal totalRemanente;
    private final BigDecimal diferenciaContable;
    private final long tiempoTotalNanos;
    private final String reporteFinal;

    public ResultadoSimulacion(long seed, int bunkersProcesados, int bunkersExitosos, int bajasCivilesTotales,
                               int disturbiosDisparados, double indiceAceptacionSocial,
                               boolean disturbioCivilDisparado, BigDecimal valorTotalInicial,
                               BigDecimal totalIncautado, BigDecimal totalSiniestrado,
                               BigDecimal totalQuemadoAlerta, BigDecimal totalRemanente,
                               BigDecimal diferenciaContable, long tiempoTotalNanos, String reporteFinal) {
        this.seed = seed;
        this.bunkersProcesados = bunkersProcesados;
        this.bunkersExitosos = bunkersExitosos;
        this.bajasCivilesTotales = bajasCivilesTotales;
        this.disturbiosDisparados = disturbiosDisparados;
        this.indiceAceptacionSocial = indiceAceptacionSocial;
        this.disturbioCivilDisparado = disturbioCivilDisparado;
        this.valorTotalInicial = valorTotalInicial;
        this.totalIncautado = totalIncautado;
        this.totalSiniestrado = totalSiniestrado;
        this.totalQuemadoAlerta = totalQuemadoAlerta;
        this.totalRemanente = totalRemanente;
        this.diferenciaContable = diferenciaContable;
        this.tiempoTotalNanos = tiempoTotalNanos;
        this.reporteFinal = reporteFinal;
    }

    public long getSeed() {
        return seed;
    }

    public int getBunkersProcesados() {
        return bunkersProcesados;
    }

    public int getBunkersExitosos() {
        return bunkersExitosos;
    }

    public int getBajasCivilesTotales() {
        return bajasCivilesTotales;
    }

    public int getDisturbiosDisparados() {
        return disturbiosDisparados;
    }

    public double getIndiceAceptacionSocial() {
        return indiceAceptacionSocial;
    }

    public boolean isDisturbioCivilDisparado() {
        return disturbioCivilDisparado;
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

    public BigDecimal getDiferenciaContable() {
        return diferenciaContable;
    }

    public long getTiempoTotalNanos() {
        return tiempoTotalNanos;
    }

    public String getReporteFinal() {
        return reporteFinal;
    }

    public String firmaDeterministica() {
        return seed + "|"
                + bunkersProcesados + "|"
                + bunkersExitosos + "|"
                + bajasCivilesTotales + "|"
                + disturbiosDisparados + "|"
                + indiceAceptacionSocial + "|"
                + disturbioCivilDisparado + "|"
                + valorTotalInicial + "|"
                + totalIncautado + "|"
                + totalSiniestrado + "|"
                + totalQuemadoAlerta + "|"
                + totalRemanente + "|"
                + diferenciaContable;
    }
}
