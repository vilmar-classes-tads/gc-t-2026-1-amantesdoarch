package br.edu.ifpe.sistema_editais.service;

import org.springframework.stereotype.Service;

import br.edu.ifpe.sistema_editais.entity.Projeto;
import br.edu.ifpe.sistema_editais.repository.ProjetoRepository;

@Service
public class ProjetoService {
    
    private final ProjetoRepository projetoRepository;

    public ProjetoService(ProjetoRepository projetoRepository) {
        this.projetoRepository = projetoRepository;
    }

    public void editarProjeto(Projeto projeto) {

        if (!projeto.getEstado().equals("rascunho") && !projeto.getEstado().equals("em correção")) {
            throw new IllegalStateException("O projeto só pode ser editado se estiver em estado 'rascunho' ou 'em correção'");
        }

        projetoRepository.save(projeto);
    }

}
