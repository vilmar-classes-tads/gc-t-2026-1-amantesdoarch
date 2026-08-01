package br.edu.ifpe.sistema_editais.service;

import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

import br.edu.ifpe.sistema_editais.dto.ProjetoDto;
import br.edu.ifpe.sistema_editais.entity.Projeto;
import br.edu.ifpe.sistema_editais.repository.ProjetoRepository;

@Service
public class ProjetoService {
    
    private final ProjetoRepository projetoRepository;

    public ProjetoService(ProjetoRepository projetoRepository) {
        this.projetoRepository = projetoRepository;
    }

    public void criarProjeto(ProjetoDto dto) {
        if (dto.termoDeCompromissoAceito() == false)
            throw new RuntimeException("O termo de compromisso precisa ser aceito.");

        if (dto.titulo().isBlank())
            throw new RuntimeException("O titulo não pode estar vázio.");

        if (dto.ods().isBlank())
            throw new RuntimeException("É preciso selecionar uma ODS.");

        Projeto p = new Projeto();
        p.setTitulo(dto.titulo());
        p.setResumo(dto.resumo());
        p.setPalavrasChave(dto.palavrasChave());
        p.setCampus(dto.campus());
        p.setAreaTematica(dto.areaTematica());
        p.setOds(dto.ods());
        p.setPublicoAlvo(dto.publicoAlvo());
        p.setTermoDeCompromissoAceito(dto.termoDeCompromissoAceito());
        p.setEstado("Submetido");
        projetoRepository.save(p);
    }

    public void editarProjeto(ProjetoDto dto) {
        Projeto projeto = projetoRepository.getReferenceById(dto.id());

        if (!projeto.getEstado().equals("rascunho") && !projeto.getEstado().equals("em correção")) {
            throw new IllegalStateException("O projeto só pode ser editado se estiver em estado 'rascunho' ou 'em correção'");
        }

        projeto.setOds(dto.ods());
        projeto.setPublicoAlvo(dto.publicoAlvo());
        projeto.setPalavrasChave(dto.palavrasChave());
        projeto.setTermoDeCompromissoAceito(dto.termoDeCompromissoAceito());
        projeto.setTitulo(dto.titulo());
        projeto.setResumo(dto.resumo());
        projeto.setAreaTematica(dto.areaTematica());
        projeto.setCampus(dto.campus());
        projetoRepository.save(projeto);
    }

}
