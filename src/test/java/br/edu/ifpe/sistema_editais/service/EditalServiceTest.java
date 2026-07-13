package br.edu.ifpe.sistema_editais.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.edu.ifpe.sistema_editais.dto.EditalDto;
import br.edu.ifpe.sistema_editais.entity.Edital;
import br.edu.ifpe.sistema_editais.repository.EditalRepository;

@ExtendWith(MockitoExtension.class)
class EditalServiceTest {

    @Mock
    private EditalRepository editalRepository;

    @InjectMocks
    private EditalService editalService;

    private EditalDto dtoValido() {
        return new EditalDto(
            "Edital de Bolsas 2026",
            "001/2026",
            2026,
            LocalDate.of(2026, 3, 1),
            LocalDate.of(2026, 3, 31),
            LocalDate.of(2026, 4, 5),
            LocalDate.of(2026, 4, 30)
        );
    }

    // ---------- CAMINHO FELIZ: CRIAR ----------
    @Test
    void deveCriarEditalComSucesso() {
        var dto = dtoValido();

        editalService.criar(dto);

        var editalCaptor = ArgumentCaptor.forClass(Edital.class);
        verify(editalRepository).save(editalCaptor.capture());

        Edital editalSalvo = editalCaptor.getValue();
        assertThat(editalSalvo.getTitulo()).isEqualTo("Edital de Bolsas 2026");
        assertThat(editalSalvo.getNumero()).isEqualTo("001/2026");
        assertThat(editalSalvo.getAno()).isEqualTo(2026);
        assertThat(editalSalvo.getDataInicioSubmissao()).isEqualTo(LocalDate.of(2026, 3, 1));
        assertThat(editalSalvo.getDataFimAvaliacao()).isEqualTo(LocalDate.of(2026, 4, 30));
    }

    // ---------- CAMINHOS TRISTES: CAMPOS OBRIGATÓRIOS ----------
    static List<EditalDto> dtosComCampoNulo() {
        var base = LocalDate.of(2026, 3, 1);
        return List.of(
            new EditalDto(null, "001/2026", 2026, base, base.plusDays(30), base.plusDays(35), base.plusDays(60)),
            new EditalDto("Título", null, 2026, base, base.plusDays(30), base.plusDays(35), base.plusDays(60)),
            new EditalDto("Título", "001/2026", null, base, base.plusDays(30), base.plusDays(35), base.plusDays(60)),
            new EditalDto("Título", "001/2026", 2026, null, base.plusDays(30), base.plusDays(35), base.plusDays(60)),
            new EditalDto("Título", "001/2026", 2026, base, null, base.plusDays(35), base.plusDays(60)),
            new EditalDto("Título", "001/2026", 2026, base, base.plusDays(30), null, base.plusDays(60)),
            new EditalDto("Título", "001/2026", 2026, base, base.plusDays(30), base.plusDays(35), null)
        );
    }
    @ParameterizedTest
    @MethodSource("dtosComCampoNulo")
    void deveLancarExcecaoQuandoCampoObrigatorioForNulo(EditalDto dto) {
        assertThatThrownBy(() -> editalService.criar(dto))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Campos obrigatórios não preenchidos");

        verify(editalRepository, never()).save(any());
    }

    // ---------- CAMINHO TRISTE: PERÍODO DE SUBMISSÃO INVÁLIDO ----------
    @Test
    void deveLancarExcecaoQuandoDataFimSubmissaoNaoForDepoisDaDataInicio() {
        var dto = new EditalDto(
            "Edital", "001/2026", 2026,
            LocalDate.of(2026, 3, 10),
            LocalDate.of(2026, 3, 10),
            LocalDate.of(2026, 4, 5),
            LocalDate.of(2026, 4, 30)
        );
        assertThatThrownBy(() -> editalService.criar(dto))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Data fim de submissão deve ser após a data início de submissão");
        verify(editalRepository, never()).save(any());
    }

    // ---------- CAMINHO TRISTE: PERÍODO DE AVALIAÇÃO INVÁLIDO ----------
    @Test
    void deveLancarExcecaoQuandoDataFimAvaliacaoNaoForDepoisDaDataInicio() {
        var dto = new EditalDto(
            "Edital", "001/2026", 2026,
            LocalDate.of(2026, 3, 1),
            LocalDate.of(2026, 3, 31),
            LocalDate.of(2026, 4, 10),
            LocalDate.of(2026, 4, 10)
        );
        assertThatThrownBy(() -> editalService.criar(dto))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Data fim de avaliação deve ser após a data início de avaliação");

        verify(editalRepository, never()).save(any());
    }

    // ---------- CAMINHO TRISTE: ORDEM DOS PERÍODOS INVÁLIDA ----------
    @Test
    void deveLancarExcecaoQuandoAvaliacaoComecarAntesDoFimDaSubmissao() {
        var dto = new EditalDto(
            "Edital", "001/2026", 2026,
            LocalDate.of(2026, 3, 1),
            LocalDate.of(2026, 3, 31),
            LocalDate.of(2026, 3, 20),
            LocalDate.of(2026, 4, 30)
        );

        assertThatThrownBy(() -> editalService.criar(dto))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("O período de avaliação deve começar após o fim da submissão");

        verify(editalRepository, never()).save(any());
    }

    // ---------- CAMINHO FELIZ: EDITAR ----------
    @Test
    void deveEditarEditalComSucesso() {
        var editalExistente = new Edital();
        editalExistente.setTitulo("Título antigo");

        when(editalRepository.findById(1L)).thenReturn(Optional.of(editalExistente));

        var dto = dtoValido();
        editalService.editar(1L, dto);

        var editalCaptor = ArgumentCaptor.forClass(Edital.class);
        verify(editalRepository).save(editalCaptor.capture());

        Edital editalAtualizado = editalCaptor.getValue();
        assertThat(editalAtualizado.getTitulo()).isEqualTo("Edital de Bolsas 2026");
        assertThat(editalAtualizado.getNumero()).isEqualTo("001/2026");
    }

    // ---------- CAMINHO TRISTE: EDITAR EDITAL INEXISTENTE ----------
    @Test
    void deveLancarExcecaoAoEditarEditalInexistente() {
        when(editalRepository.findById(99L)).thenReturn(Optional.empty());

        var dto = dtoValido();

        assertThatThrownBy(() -> editalService.editar(99L, dto))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Edital não encontrado com id: 99");

        verify(editalRepository, never()).save(any());
    }

    // ---------- CAMINHO TRISTE: EDITAR COM PERÍODO DE SUBMISSÃO INVÁLIDO ----------
    @Test
    void deveLancarExcecaoAoEditarComDataFimSubmissaoInvalida() {
        var editalExistente = new Edital();
        when(editalRepository.findById(1L)).thenReturn(Optional.of(editalExistente));

        var dto = new EditalDto(
                "Edital", "001/2026", 2026,
            LocalDate.of(2026, 3, 10),
            LocalDate.of(2026, 3, 10),
            LocalDate.of(2026, 4, 5),
            LocalDate.of(2026, 4, 30)
        );

        assertThatThrownBy(() -> editalService.editar(1L, dto))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Data fim de submissão deve ser após a data início de submissão");

        verify(editalRepository, never()).save(any());
    }

    // ---------- LISTAR ----------
    @Test
    void deveListarTodosOsEditais() {
        var edital1 = new Edital();
        edital1.setTitulo("Edital 1");
        var edital2 = new Edital();
        edital2.setTitulo("Edital 2");

        when(editalRepository.findAll()).thenReturn(List.of(edital1, edital2));

        List<Edital> resultado = editalService.listar();

        assertThat(resultado).hasSize(2);
        assertThat(resultado).extracting(Edital::getTitulo).containsExactly("Edital 1", "Edital 2");
    }
    @Test
    void deveRetornarListaVaziaQuandoNaoHouverEditais() {
        when(editalRepository.findAll()).thenReturn(List.of());

        List<Edital> resultado = editalService.listar();

        assertThat(resultado).isEmpty();
    }
}