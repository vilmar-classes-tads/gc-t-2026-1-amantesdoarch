package br.edu.ifpe.sistema_editais.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    @Test
    @DisplayName("CT-12 - Cadastro de edital com sucesso utilizando datas válidas")
    void cadastroComSucessoComDatasValidas() {
        EditalDto dto = new EditalDto(
                "Edital Teste",
                "001",
                2026,
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 10),
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 15));

        editalService.criar(dto);

        ArgumentCaptor<Edital> captor = ArgumentCaptor.forClass(Edital.class);
        verify(editalRepository).save(captor.capture());

        Edital editalSalvo = captor.getValue();
        assertEquals("Edital Teste", editalSalvo.getTitulo());
        assertEquals("001", editalSalvo.getNumero());
        assertEquals(2026, editalSalvo.getAno());
        assertEquals(LocalDate.of(2026, 7, 1), editalSalvo.getDataInicioSubmissao());
        assertEquals(LocalDate.of(2026, 7, 10), editalSalvo.getDataFimSubmissao());
        assertEquals(LocalDate.of(2026, 8, 1), editalSalvo.getDataInicioAvaliacao());
        assertEquals(LocalDate.of(2026, 8, 15), editalSalvo.getDataFimAvaliacao());
    }

    @Test
    @DisplayName("CT-13 - Tentativa de cadastro de edital com Data de Início da Submissão maior que a Data de Fim")
    void naoDeveCadastrarComDataInicioSubmissaoMaiorQueFim() {
        EditalDto dto = new EditalDto(
                "Edital de Inovação",
                "002",
                2026,
                LocalDate.of(2026, 8, 20),
                LocalDate.of(2026, 8, 10),
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 15));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> editalService.criar(dto));

        assertEquals(
                "Data fim de submissão deve ser após a data início de submissão",
                exception.getMessage());

        verify(editalRepository, never()).save(any());
    }

    @Test
    @DisplayName("CT-14 - Tentativa de cadastro de edital com Data de Início da Avaliação maior que a Data de Fim")
    void naoDeveCadastrarComDataInicioAvaliacaoMaiorQueFim() {
        EditalDto dto = new EditalDto(
                "Edital de Apoio a Eventos",
                "003",
                2026,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 15),
                LocalDate.of(2026, 9, 30),
                LocalDate.of(2026, 9, 1)
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> editalService.criar(dto));

        assertEquals(
                "Data fim de avaliação deve ser após a data início de avaliação",
                exception.getMessage());

        verify(editalRepository, never()).save(any());
    }

    @Test
    @DisplayName("CT-15 - Visualização da listagem de editais na área administrativa")
    void deveListarEditaisCadastrados() {
        Edital edital1 = new Edital(
                1L,
                "Edital de Inovação",
                "001",
                2026,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 15),
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 15));

        Edital edital2 = new Edital(
                2L,
                "Edital de Apoio a Eventos",
                "002",
                2026,
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 20),
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 10));

        when(editalRepository.findAll()).thenReturn(List.of(edital1, edital2));

        List<Edital> resultado = editalService.listar();

        assertEquals(2, resultado.size());
        assertEquals("Edital de Inovação", resultado.get(0).getTitulo());
        assertEquals("Edital de Apoio a Eventos", resultado.get(1).getTitulo());

        verify(editalRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("CT-16 - Listagem quando não existem editais cadastrados")
    void deveRetornarListaVaziaQuandoNaoHaEditaisCadastrados() {
        when(editalRepository.findAll()).thenReturn(List.of());

        List<Edital> resultado = editalService.listar();

        assertTrue(resultado.isEmpty());
        verify(editalRepository, times(1)).findAll();
    }

}