package br.edu.ifpe.sistema_editais.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.edu.ifpe.sistema_editais.entity.Edital;

@Repository
public interface EditalRepository extends JpaRepository<Edital, Long> {

}