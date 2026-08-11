package br.edu.ifpe.sistema_editais.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.edu.ifpe.sistema_editais.entity.Projeto;

@Repository
public interface ProjetoRepository extends JpaRepository<Projeto, Long> {

    List<Projeto> findAllByEstado(String estado);

    List<Projeto> findAllByCampus(String campus);

    List<Projeto> findAllByCampusAndEditalTituloAndAreaTematicaAndEstado(
            String campus, String editalTitulo, String areaTematica, String estado);

    @Query("SELECT p FROM Projeto p WHERE p.campus = :campus AND p.estado IN :status")
    List<Projeto> findByCampusEStatus(@Param("campus") String campus, @Param("status") List<String> status);
}