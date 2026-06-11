package com.example.back.exception;

import java.time.LocalTime;

public class ConflictoHorarioException extends RuntimeException {

    private final Long conferenciaConflictoId;
    private final String conferenciaConflictoNombre;
    private final LocalTime horaInicioConflicto;
    private final LocalTime horaFinConflicto;

    public ConflictoHorarioException(String message) {
        super(message);
        this.conferenciaConflictoId = null;
        this.conferenciaConflictoNombre = null;
        this.horaInicioConflicto = null;
        this.horaFinConflicto = null;
    }

    public ConflictoHorarioException(String message, Long conferenciaId,
                                     String conferenciaNombre,
                                     LocalTime horaInicio,
                                     LocalTime horaFin) {
        super(message);
        this.conferenciaConflictoId = conferenciaId;
        this.conferenciaConflictoNombre = conferenciaNombre;
        this.horaInicioConflicto = horaInicio;
        this.horaFinConflicto = horaFin;
    }

    // Getters
    public Long getConferenciaConflictoId() { return conferenciaConflictoId; }
    public String getConferenciaConflictoNombre() { return conferenciaConflictoNombre; }
    public LocalTime getHoraInicioConflicto() { return horaInicioConflicto; }
    public LocalTime getHoraFinConflicto() { return horaFinConflicto; }
}