package com.fintrack.dto;

import java.util.List;

/** Separa siempre los DATOS DEL SISTEMA de la INTERPRETACIÓN DE IA. */
public record AiResponse(String question, List<String> systemData, String interpretation, String engine,
                         String notice) {
}
