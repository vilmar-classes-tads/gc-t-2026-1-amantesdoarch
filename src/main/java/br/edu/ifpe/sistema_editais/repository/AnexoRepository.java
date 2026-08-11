package br.edu.ifpe.sistema_editais.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.edu.ifpe.sistema_editais.entity.Anexo;

@Repository
public interface AnexoRepository extends JpaRepository<Anexo, Long> {

    Optional<Anexo> findByIdAndProjetoId(Long id, Long projetoId);
}