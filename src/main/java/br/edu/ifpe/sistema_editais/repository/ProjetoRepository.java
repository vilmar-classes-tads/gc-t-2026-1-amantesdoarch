package br.edu.ifpe.sistema_editais.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.ifpe.sistema_editais.entity.Projeto;

public interface ProjetoRepository extends JpaRepository<Projeto, Long> {
    
}
