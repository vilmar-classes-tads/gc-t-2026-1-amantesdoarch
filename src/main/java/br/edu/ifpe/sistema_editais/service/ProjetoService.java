package br.edu.ifpe.sistema_editais.service;

import org.springframework.stereotype.Service;

import br.edu.ifpe.sistema_editais.dto.ProjetoDto;
import br.edu.ifpe.sistema_editais.entity.Membro;
import br.edu.ifpe.sistema_editais.entity.PlanoDeTrabalho;
import br.edu.ifpe.sistema_editais.entity.Projeto;
import br.edu.ifpe.sistema_editais.repository.ProjetoRepository;

@Service
public class ProjetoService {

    private final ProjetoRepository projetoRepository;

    public ProjetoService(ProjetoRepository projetoRepository) {
        this.projetoRepository = projetoRepository;
    }

    public void criarProjeto(ProjetoDto dto) {
        Projeto p = new Projeto();
        p.setTitulo(dto.titulo());
        p.setPalavrasChave(dto.palavrasChave());
        p.setCampus(dto.campus());
        p.setAreaTematica(dto.areaTematica());
        p.setOds(dto.ods());
        p.setPublicoAlvo(dto.publicoAlvo());
        p.setTermoDeCompromissoAceito(dto.termoDeCompromissoAceito());
        p.setEstado("Rascunho");
        projetoRepository.save(p);
    }

    public void editarProjeto(ProjetoDto dto) {
        Projeto projeto = projetoRepository.getReferenceById(dto.id());

        if (!projeto.getEstado().equals("Rascunho") && !projeto.getEstado().equals("Em correção")) {
            throw new IllegalStateException("O projeto só pode ser editado se estiver em estado 'Rascunho' ou 'Em correção'");
        }

        projeto.setOds(dto.ods());
        projeto.setPublicoAlvo(dto.publicoAlvo());
        projeto.setPalavrasChave(dto.palavrasChave());
        projeto.setTermoDeCompromissoAceito(dto.termoDeCompromissoAceito());
        projeto.setTitulo(dto.titulo());
        projeto.setAreaTematica(dto.areaTematica());
        projeto.setCampus(dto.campus());
        projetoRepository.save(projeto);
    }

    public void adicionarMembro(Long projetoId, Membro membro) {
        Projeto projeto = projetoRepository.findById(projetoId)
                .orElseThrow(() -> new RuntimeException("Projeto não encontrado"));

        projeto.getMembros().add(membro);
        projetoRepository.save(projeto);
    }

    public void removerMembro(Long projetoId, int index) {
        Projeto projeto = projetoRepository.findById(projetoId)
                .orElseThrow(() -> new RuntimeException("Projeto não encontrado"));

        projeto.getMembros().remove(index);
        projetoRepository.save(projeto);
    }

    public void adicionarPlano(Long projetoId, PlanoDeTrabalho plano) {
        Projeto projeto = projetoRepository.findById(projetoId)
                .orElseThrow(() -> new RuntimeException("Projeto não encontrado"));

        if (projeto.getPlanosDeTrabalho().size() >= 4) {
            throw new IllegalArgumentException("O projeto já atingiu o limite máximo de 4 planos de trabalho");
        }

        projeto.getPlanosDeTrabalho().add(plano);
        projetoRepository.save(projeto);
    }

    public void removerPlano(Long projetoId, int index) {
        Projeto projeto = projetoRepository.findById(projetoId)
                .orElseThrow(() -> new RuntimeException("Projeto não encontrado"));

        projeto.getPlanosDeTrabalho().remove(index);
        projetoRepository.save(projeto);
    }
}