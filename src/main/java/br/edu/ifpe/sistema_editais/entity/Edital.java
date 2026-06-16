package br.edu.ifpe.sistema_editais.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "editais")
public class Edital {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false)
    private String numero;

    @Column(nullable = false)
    private Integer ano;

    @Column(nullable = false)
    private LocalDate dataInicioSubmissao;

    @Column(nullable = false)
    private LocalDate dataFimSubmissao;

    @Column(nullable = false)
    private LocalDate dataInicioAvaliacao;

    @Column(nullable = false)
    private LocalDate dataFimAvaliacao;
}