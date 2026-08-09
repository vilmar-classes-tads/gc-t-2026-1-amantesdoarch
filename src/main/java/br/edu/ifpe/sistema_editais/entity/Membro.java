package br.edu.ifpe.sistema_editais.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class Membro {
    private String nome;
    private String cpf;
    private String funcao;
    private Integer cargaHoraria;
}