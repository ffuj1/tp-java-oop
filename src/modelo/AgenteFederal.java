package modelo;

import excepciones.MochilaSaturadaException;

/**
 * Gestiona la recolección de activos con una pila estática LIFO.
 */
public class AgenteFederal {
    private final ActivoIncautado[] mochila;
    private int tope;

    public AgenteFederal(int capacidadMochila) {
        if (capacidadMochila <= 0) {
            throw new IllegalArgumentException("La capacidad de la mochila debe ser positiva.");
        }
        this.mochila = new ActivoIncautado[capacidadMochila];
        this.tope = -1;
    }

    public void push(ActivoIncautado activo) throws MochilaSaturadaException {
        if (activo == null) {
            throw new IllegalArgumentException("No se puede apilar un activo nulo en la mochila.");
        }
        if (isFull()) {
            throw new MochilaSaturadaException(
                    "No se puede incorporar el activo ID " + activo.getId()
                            + ": la mochila esta saturada (capacidad=" + capacidad() + ")."
            );
        }
        tope++;
        mochila[tope] = activo;
    }

    public ActivoIncautado pop() {
        if (isEmpty()) {
            return null;
        }
        ActivoIncautado activo = mochila[tope];
        mochila[tope] = null;
        tope--;
        return activo;
    }

    public ActivoIncautado peek() {
        if (isEmpty()) {
            return null;
        }
        return mochila[tope];
    }

    public boolean isEmpty() {
        return tope < 0;
    }

    public boolean isFull() {
        return tope == mochila.length - 1;
    }

    public int size() {
        return tope + 1;
    }

    public int capacidad() {
        return mochila.length;
    }

    public int getCapacidad() {
        return capacidad();
    }

    public boolean eliminarActivo(ActivoIncautado activo) {
        if (activo == null || isEmpty()) {
            return false;
        }

        int indiceEncontrado = -1;
        for (int i = 0; i <= tope; i++) {
            if (activo.equals(mochila[i])) {
                indiceEncontrado = i;
                break;
            }
        }

        if (indiceEncontrado < 0) {
            return false;
        }

        for (int i = indiceEncontrado; i < tope; i++) {
            mochila[i] = mochila[i + 1];
        }
        mochila[tope] = null;
        tope--;
        return true;
    }
}
