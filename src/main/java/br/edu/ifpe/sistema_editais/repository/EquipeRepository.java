package br.edu.ifpe.sistema_editais.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.edu.ifpe.sistema_editais.entity.Membro;
import br.edu.ifpe.sistema_editais.entity.PlanoTrabalho;

@Repository
public interface EquipeRepository extends JpaRepository<Membro, Long> {
    long contarPlanosDaEquipe(Long projetoId);
    void saveMembro(Membro membro);
    void savePlano(PlanoTrabalho plano);
    Membro buscarMembroPorId(Long id);
    void delete(Membro membro);
}