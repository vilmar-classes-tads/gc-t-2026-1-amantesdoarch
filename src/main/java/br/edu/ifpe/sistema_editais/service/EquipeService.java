package br.edu.ifpe.sistema_editais.service;

import org.springframework.stereotype.Service;

import br.edu.ifpe.sistema_editais.dto.MembroDto;
import br.edu.ifpe.sistema_editais.dto.PlanoTrabalhoDto;
import br.edu.ifpe.sistema_editais.entity.Membro;
import br.edu.ifpe.sistema_editais.entity.PlanoTrabalho;
import br.edu.ifpe.sistema_editais.repository.EquipeRepository;

@Service
public class EquipeService {

    private final EquipeRepository equipeRepository;

    public EquipeService(EquipeRepository equipeRepository) {
        this.equipeRepository = equipeRepository;
    }

    public void adicionarMembro(Long projetoId, MembroDto dto) {
        // 1. Validar campos obrigatórios
        if (dto.nome() == null) {
            throw new IllegalArgumentException("O campo nome é obrigatório");
        }
        if (dto.cpf() == null) {
            throw new IllegalArgumentException("O campo CPF é obrigatório");
        }

        // 2. Criar e salvar membro
        Membro membro = new Membro();
        membro.setNome(dto.nome());
        membro.setCpf(dto.cpf());
        membro.setFuncao(dto.funcao());
        membro.setCargaHoraria(dto.cargaHoraria());
        membro.setProjetoId(projetoId);

        equipeRepository.saveMembro(membro);
    }

    public void adicionarMembroEPlano(Long projetoId, MembroDto dto) {
        // 1. Verificar limite de planos
        long total = equipeRepository.contarPlanosDaEquipe(projetoId);
        if (total >= 4) {
            throw new RuntimeException("Limite máximo de 4 planos de trabalho atingido");
        }

        // 2. Adicionar membro
        adicionarMembro(projetoId, dto);
    }

    public void removerMembro(Long idMembro) {
        Membro membro = equipeRepository.getReferenceById(idMembro);
        equipeRepository.delete(membro);
    }

    public void adicionarPlanoTrabalho(Long idMembro, PlanoTrabalhoDto dto) {
        // 1. Verificar limite de planos
        Membro membro = equipeRepository.buscarMembroPorId(idMembro);
        long total = equipeRepository.contarPlanosDaEquipe(membro.getProjetoId());
        if (total >= 4) {
            throw new RuntimeException("Limite máximo de 4 planos de trabalho atingido");
        }

        // 2. Criar e salvar plano
        PlanoTrabalho plano = new PlanoTrabalho();
        plano.setDescricao(dto.descricao());
        plano.setTipo(dto.tipo());
        plano.setArquivoAnexo(dto.descricao());
        plano.setMembro(membro);

        equipeRepository.savePlano(plano);
    }
}