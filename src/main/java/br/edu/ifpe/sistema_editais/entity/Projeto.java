package br.edu.ifpe.sistema_editais.entity;

import java.util.List;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Projeto {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String titulo;
    private List<String> palavrasChave;
    private String publicoAlvo;
    private String areaTematica;
    private String campus;
    private String ods;
    private Boolean termoDeCompromissoAceito;
    private String estado; // valores possíveis: "Aprovado", "Rascunho", "Em correção", "Rejeitado"

}
