package com.fintrack.entity;

/** Estado de una consulta simulada. ERROR y NO_DISPONIBLE nunca equivalen a "sin hallazgos". */
public enum SourceStatus {
    DISPONIBLE, SIN_HALLAZGOS, HALLAZGO, NO_REGISTRA, ERROR, NO_DISPONIBLE;

    public boolean isFailure() {
        return this == ERROR || this == NO_DISPONIBLE;
    }
}
