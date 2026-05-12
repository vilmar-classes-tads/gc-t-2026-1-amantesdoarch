package br.edu.ifpe.sistema_editais.dto;

public record UserRegistrationDto(
    // Obrigatórios
    String nome,
    String cpf,
    String email,
    String campus,
    String area_formacao,
    String titulacao,
    String senha,

    // Opcionais  
    String nome_social,
    String sexo,
    String link_lattes,
    String telefone
) {}
