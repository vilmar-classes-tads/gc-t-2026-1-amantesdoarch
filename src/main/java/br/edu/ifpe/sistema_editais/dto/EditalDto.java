package br.edu.ifpe.sistema_editais.dto;

import java.time.LocalDate;

public record EditalDto(
    String titulo,
    String numero,
    Integer ano,
    LocalDate dataInicioSubmissao,
    LocalDate dataFimSubmissao,
    LocalDate dataInicioAvaliacao,
    LocalDate dataFimAvaliacao
) {}