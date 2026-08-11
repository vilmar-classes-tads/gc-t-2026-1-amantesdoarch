package br.edu.ifpe.sistema_editais.entity;

import org.springframework.data.annotation.Id;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "planos_trabalho")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanoTrabalho {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String descricao;
    private String tipo; // "Bolsista" ou "Voluntário"
    private String arquivoAnexo;

    @ManyToOne
    private Membro membro;
}