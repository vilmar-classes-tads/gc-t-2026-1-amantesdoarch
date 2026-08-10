package br.edu.ifpe.sistema_editais.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

// Importações simuladas (você precisará criar estas classes/records)
import br.edu.ifpe.sistema_editais.dto.MembroDto;
import br.edu.ifpe.sistema_editais.dto.PlanoTrabalhoDto;
import br.edu.ifpe.sistema_editais.entity.Membro;
import br.edu.ifpe.sistema_editais.entity.PlanoTrabalho;
import br.edu.ifpe.sistema_editais.repository.EquipeRepository;
import br.edu.ifpe.sistema_editais.service.EquipeService;

@ExtendWith(MockitoExtension.class)
class EquipeServiceTest {

    @Mock
    private EquipeRepository equipeRepository;

    @InjectMocks
    private EquipeService equipeService;

    @Test
    @DisplayName("CT-23 - Tentativa de adição de plano de trabalho/membro excedendo o limite permitido")
    void naoDeveAdicionarQuintoPlanoDeTrabalho() {
        // Arrange
        MembroDto dto = new MembroDto("Ana Clara Souza", "111.222.333-44", "Bolsista", 20);
        Long idProjeto = 1L;
        
        // Simula que o banco de dados já acusa a existência de 4 planos para esta equipe
        when(equipeRepository.contarPlanosDaEquipe(idProjeto)).thenReturn(4L);

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> equipeService.adicionarMembroEPlano(idProjeto, dto)
        );

        assertEquals("Limite máximo de 4 planos de trabalho atingido", exception.getMessage());
        verify(equipeRepository, never()).save(any(Membro.class));
    }

    @Test
    @DisplayName("CT-24 / CT-25 - Adição de membro da equipe com sucesso")
    void deveAdicionarMembroComSucesso() {
        // Arrange
        MembroDto dto = new MembroDto("Carlos Eduardo", "999.888.777-66", "Pesquisador", 40);
        Long idProjeto = 1L;

        // Act
        equipeService.adicionarMembro(idProjeto, dto);

        // Assert
        ArgumentCaptor<Membro> captor = ArgumentCaptor.forClass(Membro.class);
        verify(equipeRepository).saveMembro(captor.capture());

        Membro membroSalvo = captor.getValue();
        assertEquals("Carlos Eduardo", membroSalvo.getNome());
        assertEquals("999.888.777-66", membroSalvo.getCpf());
        assertEquals("Pesquisador", membroSalvo.getFuncao());
        assertEquals(40, membroSalvo.getCargaHoraria());
    }

    @Test
    @DisplayName("CT-26 - Remoção de membro da equipe com sucesso")
    void deveRemoverMembroComSucesso() {
        // Arrange
        Long idMembroAlvo = 1L;
        Membro membroExistente = new Membro();
        membroExistente.setId(idMembroAlvo);
        membroExistente.setNome("Carlos Eduardo");

        when(equipeRepository.getReferenceById(idMembroAlvo)).thenReturn(membroExistente);
        doNothing().when(equipeRepository).delete(membroExistente);

        // Act
        equipeService.removerMembro(idMembroAlvo);

        // Assert
        verify(equipeRepository, times(1)).getReferenceById(idMembroAlvo);
        verify(equipeRepository, times(1)).delete(membroExistente);
    }

    @Test
    @DisplayName("CT-27 - Adição de plano de trabalho para bolsista/voluntário com sucesso")
    void deveAdicionarPlanoTrabalhoParaBolsista() {
        // Arrange
        Long idMembro = 1L;
        PlanoTrabalhoDto dto = new PlanoTrabalhoDto(
                "Desenvolvimento de módulo de testes automatizados", 
                "Bolsista"
        );
        
        Membro membroAssociado = new Membro();
        membroAssociado.setId(idMembro);
        membroAssociado.setNome("Ana Clara Souza");
        membroAssociado.setProjetoId(1L); 

        // Simula que há espaço (retorna menos de 4 planos)
        when(equipeRepository.contarPlanosDaEquipe(anyLong())).thenReturn(3L);
        when(equipeRepository.buscarMembroPorId(idMembro)).thenReturn(membroAssociado);

        // Act
        equipeService.adicionarPlanoTrabalho(idMembro, dto);

        // Assert
        ArgumentCaptor<PlanoTrabalho> captor = ArgumentCaptor.forClass(PlanoTrabalho.class);
        verify(equipeRepository).savePlano(captor.capture());

        PlanoTrabalho planoSalvo = captor.getValue();
        assertEquals("Desenvolvimento de módulo de testes automatizados", planoSalvo.getDescricao());
        assertEquals("Ana Clara Souza", planoSalvo.getMembro().getNome());
    }

    @Test
    @DisplayName("CT-28 - Tentativa de adicionar membro sem preencher o nome")
    void rejeitaAdicaoDeMembroSemNome() {
        // Arrange
        MembroDto dto = new MembroDto(null, "111.222.333-44", "Bolsista", 20);
        Long idProjeto = 1L;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> equipeService.adicionarMembro(idProjeto, dto)
        );

        assertTrue(exception.getMessage().toLowerCase().contains("nome"));
        verify(equipeRepository, never()).saveMembro(any());
    }

    @Test
    @DisplayName("CT-29 - Tentativa de adicionar membro sem preencher o CPF")
    void rejeitaAdicaoDeMembroSemCpf() {
        // Arrange
        MembroDto dto = new MembroDto("Carlos Eduardo", null, "Pesquisador", 40);
        Long idProjeto = 1L;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> equipeService.adicionarMembro(idProjeto, dto)
        );

        assertTrue(exception.getMessage().contains("CPF"));
        verify(equipeRepository, never()).saveMembro(any());
    }

    @Test
    @DisplayName("CT-30 - Adicionar plano de trabalho para voluntário")
    void deveAdicionarPlanoTrabalhoParaVoluntario() {
        // Arrange
        Long idMembro = 1L;
        // Simulando envio de anexo usando um nome de arquivo ou bytes no DTO
        PlanoTrabalhoDto dto = new PlanoTrabalhoDto("Plano_Voluntario.pdf", "Voluntário");
        
        Membro membroAssociado = new Membro();
        membroAssociado.setId(idMembro);
        membroAssociado.setProjetoId(1L);

        when(equipeRepository.contarPlanosDaEquipe(anyLong())).thenReturn(1L);
        when(equipeRepository.buscarMembroPorId(idMembro)).thenReturn(membroAssociado);

        equipeService.adicionarPlanoTrabalho(idMembro, dto);

        ArgumentCaptor<PlanoTrabalho> captor = ArgumentCaptor.forClass(PlanoTrabalho.class);
        verify(equipeRepository).savePlano(captor.capture());

        PlanoTrabalho planoSalvo = captor.getValue();
        assertEquals("Plano_Voluntario.pdf", planoSalvo.getArquivoAnexo());
        assertEquals("Voluntário", planoSalvo.getTipo());
    }
}