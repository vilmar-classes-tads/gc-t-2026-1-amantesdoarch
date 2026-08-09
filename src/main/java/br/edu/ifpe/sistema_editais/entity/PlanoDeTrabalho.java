package br.edu.ifpe.sistema_editais.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class PlanoDeTrabalho {
    private String nome;
    private String tipo; // "bolsista" ou "voluntário"
}