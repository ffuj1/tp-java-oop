package servicio;

import config.Configuracion;
import modelo.CentroComando;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Construye el Cuadro de Situacion final sin mezclar formato con logica operativa.
 */
public final class CuadroSituacionFinalFormatter {
    private static final BigDecimal CIEN = new BigDecimal("100");
    private static final BigDecimal UMBRAL_NEUTRALIZACION = new BigDecimal("80");
    private static final BigDecimal UMBRAL_IAS_EXITO = new BigDecimal("30");

    private final DecimalFormat formatoMonto;
    private final DecimalFormat formatoPorcentaje;

    public CuadroSituacionFinalFormatter() {
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols(Locale.US);
        this.formatoMonto = new DecimalFormat("#,##0.00", simbolos);
        this.formatoPorcentaje = new DecimalFormat("0.00", simbolos);
    }

    public String formatear(Configuracion configuracion,
                            CentroComando centroComando,
                            long tiempoTotalNanos,
                            int bunkersProcesados,
                            int bunkersExitosos,
                            int bajasCivilesTotales,
                            double indiceAceptacionSocial,
                            boolean disturbioCivilDisparado) {
        BigDecimal porcentajeNeutralizados = calcularPorcentajeNeutralizados(
                bunkersExitosos,
                configuracion.getCantidadBunkers()
        );
        BigDecimal iasFinal = BigDecimal.valueOf(indiceAceptacionSocial).setScale(2, RoundingMode.HALF_UP);
        BigDecimal diferenciaContable = centroComando.calcularDiferenciaContable();

        List<String> condicionesIncumplidas = determinarCondicionesIncumplidas(
                diferenciaContable,
                porcentajeNeutralizados,
                iasFinal
        );

        String estadoFinal = condicionesIncumplidas.isEmpty() ? "EXITO OPERATIVO" : "FRACASO OPERATIVO";
        String resultadoTecnico = bunkersExitosos + "/" + configuracion.getCantidadBunkers()
                + " bunkers neutralizados (" + formatoPorcentaje.format(porcentajeNeutralizados) + "%)";

        String salto = System.lineSeparator();
        StringBuilder reporte = new StringBuilder();
        reporte.append("=== CUADRO DE SITUACION FINAL ===").append(salto);
        reporte.append("Semilla: ").append(configuracion.getSeed()).append(salto);
        reporte.append("Tiempo total: ").append(tiempoTotalNanos).append(" ns").append(salto);
        reporte.append("Resultado tecnico: ").append(resultadoTecnico).append(salto);
        reporte.append("Objetivos capturados: ").append(bunkersExitosos)
                .append("/").append(configuracion.getCantidadBunkers())
                .append(" (").append(formatoPorcentaje.format(porcentajeNeutralizados)).append("%)")
                .append(salto);
        reporte.append("Bunkers procesados: ").append(bunkersProcesados).append(salto);
        reporte.append("Bajas: ").append(bajasCivilesTotales).append(salto);
        reporte.append("Indice de aceptacion: ").append(formatoPorcentaje.format(iasFinal)).append("%").append(salto);
        reporte.append("Valor inicial: ").append(formatearMonto(centroComando.getValorTotalInicial())).append(salto);
        reporte.append("Incautado: ").append(formatearMonto(centroComando.getTotalIncautado())).append(salto);
        reporte.append("Siniestrado: ").append(formatearMonto(centroComando.getTotalSiniestrado())).append(salto);
        reporte.append("Quemado por alerta: ").append(formatearMonto(centroComando.getTotalQuemadoAlerta())).append(salto);
        reporte.append("Remanente: ").append(formatearMonto(centroComando.getTotalRemanente())).append(salto);
        reporte.append("Diferencia contable: ").append(formatearMonto(diferenciaContable)).append(salto);
        reporte.append("Estado final: ").append(estadoFinal).append(salto);

        if (condicionesIncumplidas.isEmpty()) {
            reporte.append("Condiciones de exito: todas cumplidas").append(salto);
        } else {
            reporte.append("Condiciones incumplidas: ").append(String.join("; ", condicionesIncumplidas)).append(salto);
        }

        reporte.append("Disturbio civil: ").append(disturbioCivilDisparado ? "SI" : "NO");
        return reporte.toString();
    }

    private BigDecimal calcularPorcentajeNeutralizados(int bunkersExitosos, int cantidadBunkers) {
        if (cantidadBunkers <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(bunkersExitosos)
                .multiply(CIEN)
                .divide(BigDecimal.valueOf(cantidadBunkers), 2, RoundingMode.HALF_UP);
    }

    private List<String> determinarCondicionesIncumplidas(BigDecimal diferenciaContable,
                                                          BigDecimal porcentajeNeutralizados,
                                                          BigDecimal iasFinal) {
        List<String> condiciones = new ArrayList<>();
        if (diferenciaContable.compareTo(BigDecimal.ZERO) != 0) {
            condiciones.add("diferencia contable distinta de 0");
        }
        if (porcentajeNeutralizados.compareTo(UMBRAL_NEUTRALIZACION) <= 0) {
            condiciones.add("porcentaje de bunkers neutralizados no supera 80%");
        }
        if (iasFinal.compareTo(UMBRAL_IAS_EXITO) <= 0) {
            condiciones.add("IAS final no supera 30%");
        }
        return condiciones;
    }

    private String formatearMonto(BigDecimal monto) {
        BigDecimal normalizado = monto.setScale(2, RoundingMode.HALF_UP);
        return formatoMonto.format(normalizado);
    }
}
