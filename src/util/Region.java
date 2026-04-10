package util;

/**
 * Regiones válidas para clasificar bunkers.
 */
public enum Region {
    GBA,
    ROSARIO,
    NOA,
    NEA,
    PATAGONIA,
    ISLAS_ATLANTICO_SUR;

    public static Region desdeIndice(int indice) {
        Region[] regiones = values();
        return regiones[Math.floorMod(indice, regiones.length)];
    }
}
