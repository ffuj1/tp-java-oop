package modelo;

import util.Region;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Representa un bunker objetivo de la red operativa.
 */
public class BunkerObjetivo {
    private final int id;
    private final ArrayList<ActivoIncautado> almacen;
    private double nivelResistencia;
    private final Region region;

    public BunkerObjetivo(int id, List<ActivoIncautado> activos, double nivelResistencia, Region region) {
        if (activos == null) {
            throw new IllegalArgumentException("El almacen del bunker no puede ser nulo.");
        }
        if (region == null) {
            throw new IllegalArgumentException("La region del bunker es obligatoria.");
        }
        if (id <= 0) {
            throw new IllegalArgumentException("El ID del bunker debe ser positivo.");
        }
        validarNivelResistencia(nivelResistencia);
        this.id = id;
        this.almacen = new ArrayList<>(activos);
        this.nivelResistencia = nivelResistencia;
        this.region = region;
    }

    public int getId() {
        return id;
    }

    public List<ActivoIncautado> getAlmacen() {
        return Collections.unmodifiableList(almacen);
    }

    public double getNivelResistencia() {
        return nivelResistencia;
    }

    public void setNivelResistencia(double nivelResistencia) {
        validarNivelResistencia(nivelResistencia);
        this.nivelResistencia = nivelResistencia;
    }

    public Region getRegion() {
        return region;
    }

    public int cantidadActivos() {
        return almacen.size();
    }

    public boolean tieneActivos() {
        return !almacen.isEmpty();
    }

    public ActivoIncautado extraerActivo(int indice) {
        if (indice < 0 || indice >= almacen.size()) {
            throw new IllegalArgumentException("El indice de activo esta fuera de rango para el bunker " + id + ".");
        }
        return almacen.remove(indice);
    }

    public void insertarActivo(int indice, ActivoIncautado activo) {
        if (activo == null) {
            throw new IllegalArgumentException("No se puede insertar un activo nulo en el bunker " + id + ".");
        }
        if (indice < 0 || indice > almacen.size()) {
            throw new IllegalArgumentException("El indice de insercion esta fuera de rango para el bunker " + id + ".");
        }
        almacen.add(indice, activo);
    }

    public BigDecimal calcularValorTotalAlmacen() {
        BigDecimal total = BigDecimal.ZERO;
        for (ActivoIncautado activo : almacen) {
            total = total.add(activo.getValorNominal());
        }
        return total;
    }

    private void validarNivelResistencia(double nivelResistencia) {
        if (Double.isNaN(nivelResistencia) || nivelResistencia < 0.0d || nivelResistencia > 1.0d) {
            throw new IllegalArgumentException("El nivel de resistencia debe estar entre 0.0 y 1.0.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BunkerObjetivo)) {
            return false;
        }
        BunkerObjetivo that = (BunkerObjetivo) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "BunkerObjetivo{"
                + "id=" + id
                + ", cantidadActivos=" + almacen.size()
                + ", nivelResistencia=" + nivelResistencia
                + ", region=" + region
                + '}';
    }
}
