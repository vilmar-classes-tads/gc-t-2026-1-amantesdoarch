package br.edu.ifpe.sistema_editais.dto;

import java.util.List;

public record ProjetoDto(
    Long id,
    String titulo,
    String resumo,
    List<String> palavrasChave,
    String publicoAlvo,
    String areaTematica,
    String campus,
    String ods,
    Boolean termoDeCompromissoAceito
) {}
