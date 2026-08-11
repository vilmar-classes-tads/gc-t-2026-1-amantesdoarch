package br.edu.ifpe.sistema_editais.service;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import br.edu.ifpe.sistema_editais.dto.ProjetoDto;
import br.edu.ifpe.sistema_editais.entity.Anexo;
import br.edu.ifpe.sistema_editais.entity.Projeto;
import br.edu.ifpe.sistema_editais.repository.AnexoRepository;
import br.edu.ifpe.sistema_editais.repository.ProjetoRepository;

@Service
public class ProjetoService {
    
    private final ProjetoRepository projetoRepository;
    private final AnexoRepository anexoRepository;

    public ProjetoService(ProjetoRepository projetoRepository, AnexoRepository anexoRepository) {
        this.projetoRepository = projetoRepository;
        this.anexoRepository = anexoRepository;
    }

    public void criarProjeto(ProjetoDto dto) {
        if (dto.termoDeCompromissoAceito() == false)
            throw new RuntimeException("O termo de compromisso precisa ser aceito.");

        if (dto.titulo() == null || dto.titulo().isBlank())
            throw new RuntimeException("O titulo não pode estar vázio.");

        if (dto.ods() == null || dto.ods().isBlank())
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

        if (!projeto.getEstado().equals("rascunho") && !projeto.getEstado().equals("correção")) {
            throw new IllegalStateException("O projeto só pode ser editado se estiver em estado 'correção'");
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

    // issue 4
    public List<Projeto> listarProjetos() {
        return projetoRepository.findAll();
    }

    public List<Projeto> listarProjetosComFiltros(String editalTitulo, String campus, String areaTematica, String estado) {
        return projetoRepository.findAllByCampusAndEditalTituloAndAreaTematicaAndEstado(
                campus, editalTitulo, areaTematica, estado);
    }

    public List<Projeto> listarProjetosComFiltrosParciais(String editalTitulo, String campus, String areaTematica, String estado) {
        if (editalTitulo == null && campus == null && areaTematica == null && estado == null) {
            return projetoRepository.findAll();
        }
        return listarProjetosComFiltros(editalTitulo, campus, areaTematica, estado);
    }

    public List<Projeto> listarProjetosPorStatus(String estado) {
        List<Projeto> projetos = projetoRepository.findAllByEstado(estado);
        return projetos == null ? Collections.emptyList() : projetos;
    }

    public List<Projeto> listarProjetosGestor(String campus) {
        List<Projeto> projetos = projetoRepository.findAllByCampus(campus);
        return projetos == null ? Collections.emptyList() : projetos;
    }

    public List<Projeto> listarProjetosGestorComStatus(String campus, List<String> status) {
        return projetoRepository.findByCampusEStatus(campus, status);
    }

    public Projeto getProjetoComVerificacao(long id, String campus) {
        Projeto projeto = projetoRepository.getReferenceById(id);

        if (!List.of("Em Correção", "Em Análise").contains(projeto.getEstado())
                || !projeto.getCampus().equals(campus)) {
            throw new SecurityException("Acesso não autorizado");
        }

        return projeto;
    }

    public boolean verificarPermissaoDownload(long projetoId, String perfil) {
        if (perfil == null) {
            return false;
        }
        return perfil.equals("Admin Geral") || perfil.equals("Gestor");
    }

    public Anexo downloadAnexo(long projetoId, long anexoId) {
        return anexoRepository.findByIdAndProjetoId(anexoId, projetoId)
                .orElseThrow(() -> new RuntimeException("Anexo não encontrado para o projeto " + projetoId));
    }
}
