package modelo;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Representa un activo o divisa disponible en un bunker.
 */
public class ActivoIncautado {
    private final int id;
    private final String tipo;
    private final BigDecimal valorNominal;
    private final int integridad;

    public ActivoIncautado(int id, String tipo, BigDecimal valorNominal, int integridad) {
        if (id <= 0) {
            throw new IllegalArgumentException("El ID del activo debe ser positivo.");
        }
        if (tipo == null || tipo.trim().isEmpty()) {
            throw new IllegalArgumentException("El tipo del activo es obligatorio.");
        }
        if (valorNominal == null || valorNominal.signum() < 0) {
            throw new IllegalArgumentException("El valor nominal no puede ser negativo.");
        }
        if (integridad < 0 || integridad > 100) {
            throw new IllegalArgumentException("La integridad debe estar entre 0 y 100.");
        }
        this.id = id;
        this.tipo = tipo.trim();
        this.valorNominal = valorNominal;
        this.integridad = integridad;
    }

    public int getId() {
        return id;
    }

    public String getTipo() {
        return tipo;
    }

    public BigDecimal getValorNominal() {
        return valorNominal;
    }

    public int getIntegridad() {
        return integridad;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ActivoIncautado)) {
            return false;
        }
        ActivoIncautado that = (ActivoIncautado) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ActivoIncautado{"
                + "id=" + id
                + ", tipo='" + tipo + '\''
                + ", valorNominal=" + valorNominal
                + ", integridad=" + integridad
                + '}';
    }
}
