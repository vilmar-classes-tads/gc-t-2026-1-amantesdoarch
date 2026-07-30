package br.edu.ifpe.sistema_editais.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.edu.ifpe.sistema_editais.dto.EditalDto;
import br.edu.ifpe.sistema_editais.repository.EditalRepository;

@ExtendWith(MockitoExtension.class)
class EditalServiceTest {

    @Mock
    private EditalRepository editalRepository;

    @InjectMocks
    private EditalService editalService;

    @Test
    @DisplayName("CT-01 - Deve lançar exceção quando a data de fim da submissão não for após a data de início")
    void deveFalharQuandoFimSubmissaoNaoEhDepoisDoInicio() {
        EditalDto dto = new EditalDto(
                "Edital Teste",
                "001",
                2026,
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 15)
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> editalService.criar(dto)
        );

        assertEquals(
                "Data fim de submissão deve ser após a data início de submissão",
                exception.getMessage()
        );

        verify(editalRepository, never()).save(any());
    }
}