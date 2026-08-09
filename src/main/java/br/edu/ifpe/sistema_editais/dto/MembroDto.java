package br.edu.ifpe.sistema_editais.dto;

public record MembroDto(
    String nome,
    String cpf,
    String funcao,
    Integer cargaHoraria
) {}