package br.edu.ifpe.sistema_editais.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.edu.ifpe.sistema_editais.dto.ProjetoDto;
import br.edu.ifpe.sistema_editais.entity.Projeto;
import br.edu.ifpe.sistema_editais.repository.ProjetoRepository;

@ExtendWith(MockitoExtension.class)
class ProjetoServiceTest {

    @Mock
    private ProjetoRepository projetoRepository;

    @InjectMocks
    private ProjetoService projetoService;

    private ProjetoDto dtoValido() {
        return new ProjetoDto(
                1L,
                "Green Roof Atlas",
                List.of("geoprocessamento", "telhados verdes"),
                "Comunidade urbana",
                "Meio Ambiente",
                "Recife",
                "ODS 11",
                true
        );
    }

    // ---------- CAMINHO FELIZ: CRIAR ----------
    @Test
    void deveCriarProjetoComSucesso() {
        var dto = dtoValido();

        projetoService.criarProjeto(dto);

        var projetoCaptor = ArgumentCaptor.forClass(Projeto.class);
        verify(projetoRepository).save(projetoCaptor.capture());

        Projeto projetoSalvo = projetoCaptor.getValue();
        assertThat(projetoSalvo.getTitulo()).isEqualTo("Green Roof Atlas");
        assertThat(projetoSalvo.getCampus()).isEqualTo("Recife");
        assertThat(projetoSalvo.getAreaTematica()).isEqualTo("Meio Ambiente");
        assertThat(projetoSalvo.getPublicoAlvo()).isEqualTo("Comunidade urbana");
        assertThat(projetoSalvo.getOds()).isEqualTo("ODS 11");
        assertThat(projetoSalvo.getTermoDeCompromissoAceito()).isTrue();
        assertThat(projetoSalvo.getEstado()).isEqualTo("Rascunho");
    }

    @Test
    void deveCriarProjetoSempreComEstadoRascunho() {
        var dto = dtoValido();

        projetoService.criarProjeto(dto);

        var projetoCaptor = ArgumentCaptor.forClass(Projeto.class);
        verify(projetoRepository).save(projetoCaptor.capture());

        assertThat(projetoCaptor.getValue().getEstado()).isEqualTo("Rascunho");
    }

    // ---------- CAMINHO FELIZ: EDITAR ----------
    @ParameterizedTest
    @ValueSource(strings = {"rascunho", "em correção"})
    void devePermitirEdicaoQuandoEstadoForRascunhoOuEmCorrecao(String estadoAtual) {
        var projetoExistente = new Projeto();
        projetoExistente.setEstado(estadoAtual);

        when(projetoRepository.getReferenceById(1L)).thenReturn(projetoExistente);

        var dto = dtoValido();
        projetoService.editarProjeto(dto);

        var projetoCaptor = ArgumentCaptor.forClass(Projeto.class);
        verify(projetoRepository).save(projetoCaptor.capture());

        Projeto projetoAtualizado = projetoCaptor.getValue();
        assertThat(projetoAtualizado.getTitulo()).isEqualTo("Green Roof Atlas");
        assertThat(projetoAtualizado.getCampus()).isEqualTo("Recife");
        assertThat(projetoAtualizado.getOds()).isEqualTo("ODS 11");
    }

    // ---------- CAMINHO TRISTE: EDITAR PROJETO EM ESTADO NÃO EDITÁVEL ----------
    @ParameterizedTest
    @ValueSource(strings = {"aprovado", "em avaliação", "reprovado", "Rascunho"})
    void deveLancarExcecaoAoEditarProjetoEmEstadoNaoPermitido(String estadoAtual) {
        var projetoExistente = new Projeto();
        projetoExistente.setEstado(estadoAtual);

        when(projetoRepository.getReferenceById(1L)).thenReturn(projetoExistente);

        var dto = dtoValido();

        assertThatThrownBy(() -> projetoService.editarProjeto(dto))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("O projeto só pode ser editado se estiver em estado 'rascunho' ou 'em correção'");

        verify(projetoRepository, never()).save(any());
    }
}