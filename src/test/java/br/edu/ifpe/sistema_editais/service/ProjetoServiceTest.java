package br.edu.ifpe.sistema_editais.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.edu.ifpe.sistema_editais.dto.ProjetoDto;
import br.edu.ifpe.sistema_editais.entity.Projeto;
import br.edu.ifpe.sistema_editais.repository.ProjetoRepository;

@ExtendWith(MockitoExtension.class)
public class ProjetoServiceTest {
    
    @Mock
    private ProjetoRepository projetoRepository;

    @InjectMocks
    private ProjetoService projetoService;

    @Test
    @DisplayName("CT-17: Submissão de projeto de extensão com sucesso")
    void deveSubmeterProjetoComSucesso() {
        ProjetoDto dto = new ProjetoDto(
            null,
            "Inclusão Digital para Terceira Idade",
            "Projeto voltado para o ensino de ferramentas digitais básicas para idosos.",
            List.of("Inclusão digital", "Idosos", "Tecnologia"),
            "Pessoas acima de 60 anos",
            "Educação",
            "Recife",
            "Educação de Qualidade (ODS 4), Redução das Desigualdades (ODS 10)",
            true
        );

        projetoService.criarProjeto(dto);

        ArgumentCaptor<Projeto> captor = ArgumentCaptor.forClass(Projeto.class);
        verify(projetoRepository).save(captor.capture());

        Projeto projetoSalvo = captor.getValue();
        assertEquals(null, projetoSalvo.getId());
        assertEquals("Inclusão Digital para Terceira Idade", projetoSalvo.getTitulo());
        assertEquals("Projeto voltado para o ensino de ferramentas digitais básicas para idosos.", projetoSalvo.getResumo());
        assertEquals("Pessoas acima de 60 anos", projetoSalvo.getPublicoAlvo());
        assertEquals("Educação", projetoSalvo.getAreaTematica());
        assertEquals("Recife", projetoSalvo.getCampus());
        assertEquals("Educação de Qualidade (ODS 4), Redução das Desigualdades (ODS 10)", projetoSalvo.getOds());
        assertEquals(true, projetoSalvo.getTermoDeCompromissoAceito());
        assertEquals(3, projetoSalvo.getPalavrasChave().size());
        assertEquals("Submetido", projetoSalvo.getEstado());
    }

    @Test
    @DisplayName("CT-18 - Tentativa de submissão de projeto sem o aceite do Termo de Compromisso")
    void deveFalharAoCadastrarProjetoSemAceiteDosTermos() {
        ProjetoDto dto = new ProjetoDto(
            null,
            "Horta Comunitária Sustentável",
            "Criação de hortas comunitárias.",
            List.of("Sustentabilidade", "Meio ambiente", "Horta"),
            "Comunidade local",
            "Meio ambiente",
            "Recife",
            "Fome Zero e Agricultura Sustentável (ODS 2)",
            false
        );

        RuntimeException exception = assertThrows(
            RuntimeException.class, 
            () -> projetoService.criarProjeto(dto)
        );

        assertEquals("O termo de compromisso precisa ser aceito.", exception.getMessage());
        verify(projetoRepository, never()).save(any(Projeto.class));
    }

    @Test
    @DisplayName("CT-19 - Visualização de projeto já submetido (Bloqueio de edição)")
    void naoDeveDeixarEditarProjetoForaDeEstadoEmCorrecao() {
        // Projeto "Submetido"
        Projeto projeto = new Projeto(
            1L,
            "Horta Comunitária Sustentável",
            "Criação de hortas comunitárias.",
            List.of("Sustentabilidade", "Meio ambiente", "Horta"),
            "Comunidade local",
            "Meio ambiente",
            "Recife",
            "Fome Zero e Agricultura Sustentável (ODS 2)",
            false,
            "Submetido"
        );

        when(projetoRepository.getReferenceById(projeto.getId())).thenReturn(projeto);

        ProjetoDto projetoEditado = new ProjetoDto(
            1L,
            "Horta Comunitária", // Campo editado
            "Criação de hortas comunitárias.",
            List.of("Sustentabilidade", "Meio ambiente", "Horta"),
            "Comunidade local",
            "Meio ambiente",
            "Recife",
            "Fome Zero e Agricultura Sustentável (ODS 2)",
            false
        );

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> projetoService.editarProjeto(projetoEditado)
        );

        assertEquals("O projeto só pode ser editado se estiver em estado 'correção'", exception.getMessage());
        verify(projetoRepository, never()).save(any(Projeto.class));
    }

    @Test
    @DisplayName("CT-20 - Edição de projeto com status de correção")
    void deveTerSucessoAoEditarProjetoEmCorrecao() {
        // Projeto "Submetido"
        Projeto projeto = new Projeto(
            1L,
            "Horta Comunitária Sustentável",
            "Criação de hortas comunitárias.",
            List.of("Sustentabilidade", "Meio ambiente", "Horta"),
            "Comunidade local",
            "Meio ambiente",
            "Recife",
            "Fome Zero e Agricultura Sustentável (ODS 2)",
            false,
            "correção"
        );

        when(projetoRepository.getReferenceById(projeto.getId())).thenReturn(projeto);

        ProjetoDto projetoEditado = new ProjetoDto(
            1L,
            "Inclusão Digital para Terceira Idade - Módulo 2", // Campo editado
            "(Novo texto contendo as correções solicitadas)", // Campo editado 2
            List.of("Sustentabilidade", "Meio ambiente", "Horta"),
            "Comunidade local",
            "Meio ambiente",
            "Recife",
            "Fome Zero e Agricultura Sustentável (ODS 2)",
            false
        );

        projetoService.editarProjeto(projetoEditado);

        ArgumentCaptor<Projeto> captor = ArgumentCaptor.forClass(Projeto.class);
        verify(projetoRepository, times(1)).save(captor.capture());

        Projeto projetoSalvo = captor.getValue();
        assertEquals(projetoEditado.titulo(), projetoSalvo.getTitulo());
        assertEquals(projetoEditado.resumo(), projetoSalvo.getResumo());
        assertEquals(projetoEditado.campus(), projetoSalvo.getCampus());
        assertEquals(projetoEditado.areaTematica(), projetoSalvo.getAreaTematica());
        assertEquals(projetoEditado.ods(), projetoSalvo.getOds());
        assertEquals(projetoEditado.termoDeCompromissoAceito(), projetoSalvo.getTermoDeCompromissoAceito());
        assertEquals(projetoEditado.publicoAlvo(), projetoSalvo.getPublicoAlvo());
        assertEquals(projetoEditado.palavrasChave().size(), projetoSalvo.getPalavrasChave().size());
        assertEquals(projetoEditado.palavrasChave().get(0), projetoSalvo.getPalavrasChave().get(0));
    }

    @Test
    @DisplayName("CT-21 - Submissão sem preencher o título")
    void rejeitaProjetoSemTitutlo() {
        ProjetoDto dto = new ProjetoDto(
            null,
            null, // Sem titulo
            "Projeto voltado para o ensino de ferramentas digitais básicas para idosos.",
            List.of(" Inclusão digital", "Idosos", "Tecnologia"),
            "Pessoas acima de 60 anos",
            "Educação",
            "Recife",
            "Educação de Qualidade (ODS 4), Redução das Desigualdades (ODS 10)",
            true
        );

        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> projetoService.criarProjeto(dto)
        );

        assertEquals("O titulo não pode estar vázio.", exception.getMessage());
        verify(projetoRepository, never()).save(any(Projeto.class));
    }

    @Test
    @DisplayName("CT-22 - Tentativa de submissão sem selecionar ODS")
    void rejeitaProjetoSemODS() {
        ProjetoDto dto = new ProjetoDto(
            null,
            "Inclusão Digital para Terceira Idade - Módulo 2",
            "Projeto voltado para o ensino de ferramentas digitais básicas para idosos.",
            List.of(" Inclusão digital", "Idosos", "Tecnologia"),
            "Pessoas acima de 60 anos",
            "Educação",
            "Recife",
            null, // ODS null
            true
        );

        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> projetoService.criarProjeto(dto)
        );

        assertEquals("É preciso selecionar uma ODS.", exception.getMessage());
        verify(projetoRepository, never()).save(any(Projeto.class));
    }

}
