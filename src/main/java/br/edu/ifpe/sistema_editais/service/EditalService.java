package br.edu.ifpe.sistema_editais.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.edu.ifpe.sistema_editais.dto.EditalDto;
import br.edu.ifpe.sistema_editais.entity.Edital;
import br.edu.ifpe.sistema_editais.repository.EditalRepository;

@Service
public class EditalService {

    private final EditalRepository editalRepository;

    public EditalService(EditalRepository editalRepository) {
        this.editalRepository = editalRepository;
    }

    public void criar(EditalDto dto) {
        if (dto.titulo() == null ||
            dto.numero() == null ||
            dto.ano() == null ||
            dto.dataInicioSubmissao() == null ||
            dto.dataFimSubmissao() == null ||
            dto.dataInicioAvaliacao() == null ||
            dto.dataFimAvaliacao() == null
        ) {
            throw new IllegalArgumentException("Campos obrigatórios não preenchidos");
        }

        // 2. Validar período de submissão
        if (!dto.dataFimSubmissao().isAfter(dto.dataInicioSubmissao())) {
            throw new IllegalArgumentException("Data fim de submissão deve ser após a data início de submissão");
        }

        // 3. Validar período de avaliação
        if (!dto.dataFimAvaliacao().isAfter(dto.dataInicioAvaliacao())) {
            throw new IllegalArgumentException("Data fim de avaliação deve ser após a data início de avaliação");
        }

        // 4. Validar ordem dos períodos
        if (!dto.dataInicioAvaliacao().isAfter(dto.dataFimSubmissao())) {
            throw new IllegalArgumentException("O período de avaliação deve começar após o fim da submissão");
        }

        // 5. Criar e salvar o edital
        Edital edital = new Edital();
        edital.setTitulo(dto.titulo());
        edital.setNumero(dto.numero());
        edital.setAno(dto.ano());
        edital.setDataInicioSubmissao(dto.dataInicioSubmissao());
        edital.setDataFimSubmissao(dto.dataFimSubmissao());
        edital.setDataInicioAvaliacao(dto.dataInicioAvaliacao());
        edital.setDataFimAvaliacao(dto.dataFimAvaliacao());

        editalRepository.save(edital);
    }

    public void editar(Long id, EditalDto dto) {
        // 1. Buscar edital existente
        Edital edital = editalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Edital não encontrado com id: " + id));

        // 2. Validar período de submissão
        if (!dto.dataFimSubmissao().isAfter(dto.dataInicioSubmissao())) {
            throw new IllegalArgumentException("Data fim de submissão deve ser após a data início de submissão");
        }

        // 3. Validar período de avaliação
        if (!dto.dataFimAvaliacao().isAfter(dto.dataInicioAvaliacao())) {
            throw new IllegalArgumentException("Data fim de avaliação deve ser após a data início de avaliação");
        }

        // 4. Validar ordem dos períodos
        if (!dto.dataInicioAvaliacao().isAfter(dto.dataFimSubmissao())) {
            throw new IllegalArgumentException("O período de avaliação deve começar após o fim da submissão");
        }

        // 5. Atualizar e salvar
        edital.setTitulo(dto.titulo());
        edital.setNumero(dto.numero());
        edital.setAno(dto.ano());
        edital.setDataInicioSubmissao(dto.dataInicioSubmissao());
        edital.setDataFimSubmissao(dto.dataFimSubmissao());
        edital.setDataInicioAvaliacao(dto.dataInicioAvaliacao());
        edital.setDataFimAvaliacao(dto.dataFimAvaliacao());

        editalRepository.save(edital);
    }

    public List<Edital> listar() {
        return editalRepository.findAll();
    }
}
