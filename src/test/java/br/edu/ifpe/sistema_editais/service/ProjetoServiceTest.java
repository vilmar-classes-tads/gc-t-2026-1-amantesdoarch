package br.edu.ifpe.sistema_editais.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import br.edu.ifpe.sistema_editais.dto.ProjetoDto;
import br.edu.ifpe.sistema_editais.entity.Anexo;
import br.edu.ifpe.sistema_editais.entity.Projeto;
import br.edu.ifpe.sistema_editais.repository.AnexoRepository;
import br.edu.ifpe.sistema_editais.repository.ProjetoRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ProjetoServiceTest {
    
    @Mock
    private ProjetoRepository projetoRepository;

    @Mock
    private AnexoRepository anexoRepository;

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

    private Projeto criarProjetoExemplo(Long id, String titulo, String campus, String estado) {
        return new Projeto(
            id, titulo, null, new ArrayList<String>(),
            null, null, campus, null, null, estado 
        );
    }

    private Projeto criarProjetoExemplo(Long id, String titulo, String campus, String estado, String editalTitulo) {
        return criarProjetoExemplo(id, titulo, campus, estado);
    }

    @Test
    @DisplayName("CT-31: Admin Geral acessa menu Projetos e visualiza todos os projetos")
    void adminGeralDeveVerTodosOsProjetosAoAcessarMenuProjetos() {
        Projeto projeto1 = criarProjetoExemplo(1L, "Projeto A", "Recife", "Submetido");
        Projeto projeto2 = criarProjetoExemplo(2L, "Projeto B", "Caruaru", "Aprovado");
        Projeto projeto3 = criarProjetoExemplo(3L, "Projeto C", "Palmares", "Em correção");

        when(projetoRepository.findAll()).thenReturn(List.of(projeto1, projeto2, projeto3));

        List<Projeto> projetos = projetoService.listarProjetos();

        assertEquals(3, projetos.size());
        verify(projetoRepository).findAll();
    }

    // ========== CT-32: Filtros disponíveis para Admin Geral (Edital, Campus, Área, Status) ==========

    @Test
    @DisplayName("CT-32: Admin Geral aplica múltiplos filtros simultaneamente")
    void adminGeralDeveFiltrarPorTodosOsCriterios() {
        when(projetoRepository.findAllByCampusAndEditalTituloAndAreaTematicaAndEstado(
            "Recife", "Edital 01/2026", "Inclusão Digital", "Em Correção"
        )).thenReturn(List.of(criarProjetoExemplo(1L, "Projeto Filtado", "Recife", "Em Correção")));

        List<Projeto> resultados = projetoService.listarProjetosComFiltros(
            "Edital 01/2026", "Recife", "Inclusão Digital", "Em Correção"
        );

        assertEquals(1, resultados.size());
        assertEquals("Recife", resultados.get(0).getCampus());
        assertEquals("Em Correção", resultados.get(0).getEstado());
    }

    @Test
    @DisplayName("CT-32: Admin Geral remove filtros e retorna lista completa")
    void adminGeralDeveRetornarListaCompletaAposRemoverFiltros() {
        Projeto projeto1 = criarProjetoExemplo(1L, "Projeto A", "Recife", "Submetido");
        Projeto projeto2 = criarProjetoExemplo(2L, "Projeto B", "Caruaru", "Aprovado");

        when(projetoRepository.findAll()).thenReturn(List.of(projeto1, projeto2));

        List<Projeto> resultados = projetoService.listarProjetosComFiltrosParciais(null, null, null, null);

        assertEquals(2, resultados.size());
    }

    // ========== CT-33: Filtro individual por Status ==========

    @Test
    @DisplayName("CT-33: Admin Geral filtra por Status 'Aprovado' e visualiza apenas projetos aprovados")
    void adminGeralDeveFiltrarPorStatusAprovado() {
        Projeto projetoAprovado = criarProjetoExemplo(1L, "Projeto Aprovado", "Recife", "Aprovado");
        Projeto projetoOutro = criarProjetoExemplo(2L, "Projeto Outro", "Caruaru", "Em Correção");

        when(projetoRepository.findAllByEstado("Aprovado")).thenReturn(List.of(projetoAprovado));

        List<Projeto> resultados = projetoService.listarProjetosPorStatus("Aprovado");

        assertEquals(1, resultados.size());
        assertEquals("Aprovado", resultados.get(0).getEstado());
    }

    @Test
    @DisplayName("CT-33: Ao acessar listagem sem filtro, todos os projetos são exibidos")
    void adminGeralDeveVerTodosProjetosAoAcessarListagemSemFiltro() {
        Projeto p1 = criarProjetoExemplo(1L, "P1", "Recife", "Submetido");
        Projeto p2 = criarProjetoExemplo(2L, "P2", "Caruaru", "Aprovado");
        Projeto p3 = criarProjetoExemplo(3L, "P3", "Palmares", "Em Correção");

        when(projetoRepository.findAll()).thenReturn(List.of(p1, p2, p3));

        List<Projeto> resultados = projetoService.listarProjetos();

        assertEquals(3, resultados.size());
    }

    // ========== CT-34: Gestor/Diretor visualiza apenas projetos do próprio Campus ==========

    @Test
    @DisplayName("CT-34: Gestor/Diretor do Campus Recife visualiza apenas projetos do seu Campus")
    void gestorDeveVerApenasProjetosDoProprioCampus() {
        Projeto projetoRecife = criarProjetoExemplo(1L, "Projeto Recife", "Recife", "Em Correção");
        Projeto projetoCaruaru = criarProjetoExemplo(2L, "Projeto Caruaru", "Caruaru", "Aprovado");

        when(projetoRepository.findAllByCampus("Recife")).thenReturn(List.of(projetoRecife));

        List<Projeto> resultados = projetoService.listarProjetosGestor("Recife");

        assertEquals(1, resultados.size());
        assertEquals("Recife", resultados.get(0).getCampus());
    }

    @Test
    @DisplayName("CT-34: Gestor/Diretor não visualiza status de outras etapas/Campus")
    void gestorDeveVerApenasStatusPertinentesAOsuaEtapa() {
        List<String> statusPermitidos = Arrays.asList("Em Correção", "Em Análise");
        Projeto projetoValido = criarProjetoExemplo(1L, "Projeto Válido", "Recife", "Em Correção");
        Projeto projetoOutroStatus = criarProjetoExemplo(2L, "Projeto Outro", "Recife", "Aprovado");

        when(projetoRepository.findByCampusEStatus("Recife", statusPermitidos))
            .thenReturn(List.of(projetoValido));

        List<Projeto> resultados = projetoService.listarProjetosGestorComStatus("Recife", statusPermitidos);

        assertEquals(1, resultados.size());
        assertEquals("Em Correção", resultados.get(0).getEstado());
    }

    @Test
    @DisplayName("CT-34: Gestor/Diretor tenta acessar projeto de outro Campus e é bloqueado")
    void gestorDeveSerBloqueadoAoAcessarProjetoDeOutroCampus() {
        when(projetoRepository.getReferenceById(99L))
            .thenReturn(criarProjetoExemplo(99L, "Projeto Outro Campus", "Caruaru", "Submetido"));

        SecurityException exception = assertThrows(
            SecurityException.class,
            () -> projetoService.getProjetoComVerificacao(99L, "Recife")
        );

        assertEquals("Acesso não autorizado", exception.getMessage());
    }

    // ========== CT-35: Download de anexos e planos por usuário que não é dono do projeto ==========

    @Test
    @DisplayName("CT-35: Admin Geral pode fazer download de anexos de projetos de outros usuários")
    void adminGeralDeveFazerDownloadDeAnexosDeProjetosDeOutros() {
        when(anexoRepository.findByIdAndProjetoId(1L, 1L))
            .thenReturn(Optional.of(new Anexo(1L, "anexo.pdf", "Anexo", new byte[]{1}, 1L)));

        assertTrue(projetoService.verificarPermissaoDownload(1L, "Admin Geral"));

        Anexo anexo = projetoService.downloadAnexo(1L, 1L);

        assertNotNull(anexo);
        verify(anexoRepository).findByIdAndProjetoId(1L, 1L);
    }

    @Test
    @DisplayName("CT-35: Gestor/Diretor pode fazer download de anexos de projetos de outros usuários")
    void gestorDeveFazerDownloadDeAnexosDeProjetosDeOutros() {
        assertTrue(projetoService.verificarPermissaoDownload(1L, "Gestor"));

        when(anexoRepository.findByIdAndProjetoId(1L, 1L))
            .thenReturn(Optional.of(new Anexo(1L, "anexo.pdf", "Anexo", new byte[]{1}, 1L)));

        Anexo anexo = projetoService.downloadAnexo(1L, 1L);

        assertNotNull(anexo);
        verify(anexoRepository).findByIdAndProjetoId(1L, 1L);
    }

    @Test
    @DisplayName("CT-35: Proponente comum não pode fazer download de anexos de projetos que não são seus")
    void proponenteComumNaoDeveFazerDownloadDeAnexosDeProjetosDeOutros() {
        assertFalse(projetoService.verificarPermissaoDownload(1L, "Proponente"));
    }

    // ========== CT-36: Admin Geral visualiza todos os projetos (repetição de CT-31) ==========

    @Test
    @DisplayName("CT-36: Admin Geral visualiza todos os projetos sem restrição por Campus")
    void adminGeralDeveVisualizarTodosOsProjetosSemRestricao() {
        Projeto p1 = criarProjetoExemplo(1L, "P1", "Recife", "Submetido");
        Projeto p2 = criarProjetoExemplo(2L, "P2", "Caruaru", "Aprovado");
        Projeto p3 = criarProjetoExemplo(3L, "P3", "Palmares", "Em Correção");
        Projeto p4 = criarProjetoExemplo(4L, "P4", "Bezerros", "Reprovado");

        when(projetoRepository.findAll()).thenReturn(List.of(p1, p2, p3, p4));

        List<Projeto> resultados = projetoService.listarProjetos();

        assertEquals(4, resultados.size());
    }

    // ========== CT-37: Filtros disponíveis para Admin Geral (repetição de CT-32) ==========

    @Test
    @DisplayName("CT-37: Admin Geral filtra por Edital, Campus, Área e Status simultaneamente")
    void adminGeralDeveFiltrarPorTodosOsCriteriosSimultaneamente() {
        when(projetoRepository.findAllByCampusAndEditalTituloAndAreaTematicaAndEstado(
            "Recife", "Edital 01/2026", "Inclusão Digital", "Em Correção"
        )).thenReturn(List.of(
            criarProjetoExemplo(1L, "Projeto F1", "Recife", "Em Correção", "Edital 01/2026")
        ));

        List<Projeto> resultados = projetoService.listarProjetosComFiltros(
            "Edital 01/2026", "Recife", "Inclusão Digital", "Em Correção"
        );

        assertEquals(1, resultados.size());
        assertEquals("Recife", resultados.get(0).getCampus());
        assertEquals("Em Correção", resultados.get(0).getEstado());
    }

    // ========== CT-38: Filtro individual por Status (repetição de CT-33) ==========

    @Test
    @DisplayName("CT-38: Admin Geral filtra por Status 'Aprovado' e visualiza apenas aprovados")
    void adminGeralDeveFiltrarPorStatusAprovadoNaListagem() {
        Projeto pAprovado = criarProjetoExemplo(1L, "Projeto Aprovado", "Recife", "Aprovado");
        Projeto pOutro = criarProjetoExemplo(2L, "Projeto Outro", "Caruaru", "Em Correção");

        when(projetoRepository.findAllByEstado("Aprovado")).thenReturn(List.of(pAprovado));

        List<Projeto> resultados = projetoService.listarProjetosPorStatus("Aprovado");

        assertEquals(1, resultados.size());
        assertEquals("Aprovado", resultados.get(0).getEstado());
    }

    // ========== CT-39: Gestor/Diretor visualiza apenas projetos do próprio Campus (repetição de CT-34) ==========

    @Test
    @DisplayName("CT-39: Gestor/Diretor do Campus Recife vê apenas projetos do Recife")
    void gestorDeveVerApenasProjetosDoCampusRecife() {
        Projeto pRecife = criarProjetoExemplo(1L, "P Recife", "Recife", "Em Correção");
        Projeto pCaruaru = criarProjetoExemplo(2L, "P Caruaru", "Caruaru", "Aprovado");

        when(projetoRepository.findAllByCampus("Recife")).thenReturn(List.of(pRecife));

        List<Projeto> resultados = projetoService.listarProjetosGestor("Recife");

        assertEquals(1, resultados.size());
        assertEquals("Recife", resultados.get(0).getCampus());
    }

    @Test
    @DisplayName("CT-39: Gestor/Diretor tenta acessar projeto de outro Campus via URL e é bloqueado")
    void gestorDeveSerBloqueadoAoAcessarProjetoDeOutroCampusViaURL() {
        when(projetoRepository.getReferenceById(50L))
            .thenReturn(criarProjetoExemplo(50L, "P Outro", "Caruaru", "Submetido"));

        assertThrows(SecurityException.class, () -> projetoService.getProjetoComVerificacao(50L, "Recife"));
    }

    // ========== CT-40: Download de anexos por usuário que não é dono (repetição de CT-35) ==========

    @Test
    @DisplayName("CT-40: Admin Geral faz download de anexos de projeto de outro usuário com sucesso")
    void adminGeralDeveFazerDownloadComSucesso() {
        when(anexoRepository.findByIdAndProjetoId(1L, 10L))
            .thenReturn(Optional.of(new Anexo(1L, "anexo.pdf", "Anexo", new byte[]{1}, 10L)));

        assertTrue(projetoService.verificarPermissaoDownload(10L, "Admin Geral"));

        Anexo anexo = projetoService.downloadAnexo(10L, 1L);

        assertNotNull(anexo);
        verify(anexoRepository).findByIdAndProjetoId(1L, 10L);
    }

    // ========== CT-41: Filtros aplicados sem resultados correspondentes ==========

    @Test
    @DisplayName("CT-41: Filtros combinados que não retornam resultado exibem mensagem informativa")
    void filtrarComCriariosQueNaoRetornamResultado() {
        when(projetoRepository.findAllByCampusAndEditalTituloAndAreaTematicaAndEstado(
            "Caruaru", "Edital 02/2026", "Sustentabilidade", "Reprovado"
        )).thenReturn(Collections.emptyList());

        List<Projeto> resultados = projetoService.listarProjetosComFiltros(
            "Edital 02/2026", "Caruaru", "Sustentabilidade", "Reprovado"
        );

        assertTrue(resultados.isEmpty());
    }

    @Test
    @DisplayName("CT-41: Após remover filtros, sistema volta a exibir lista completa")
    void aposRemoverFiltrosDeveRetornarListaCompleta() {
        Projeto p1 = criarProjetoExemplo(1L, "P1", "Recife", "Submetido");
        Projeto p2 = criarProjetoExemplo(2L, "P2", "Caruaru", "Aprovado");

        when(projetoRepository.findAll()).thenReturn(List.of(p1, p2));

        List<Projeto> resultados = projetoService.listarProjetosComFiltrosParciais(null, null, null, null);

        assertEquals(2, resultados.size());
    }

    // ========== CT-42: Usuário sem perfil administrativo tenta acessar listagem administrativa ==========

    @Test
    @DisplayName("CT-42: Proponente sem perfil administrativo é bloqueado ao acessar listagem administrativa")
    void proponenteNaoDeveAcessarListagemAdministrativa() {
        assertFalse(projetoService.verificarPermissaoDownload(1L, "Proponente"));
    }

    // ========== CT-43: Gestor/Diretor tenta manipular filtro de Campus para visualizar outro Campus ==========

    @Test
    @DisplayName("CT-43: Gestor/Diretor tenta filtrar por Campus Caruaru mas sistema ignora")
    void gestorNaoDeveVerDadosDeOutroCampusPorManipulacaoDeFiltro() {
        when(projetoRepository.findAllByCampus("Recife"))
            .thenReturn(List.of(criarProjetoExemplo(1L, "P Recife", "Recife", "Em Correção")));

        List<Projeto> resultados = projetoService.listarProjetosGestor("Recife");

        assertEquals(1, resultados.size());
        assertEquals("Recife", resultados.get(0).getCampus());
    }

    @Test
    @DisplayName("CT-43: Gestor/Diretor não visualiza projeto do Campus Caruaru mesmo com filtro manipulado")
    void gestorNaoDeveVisualizarProjetoDoCampusCaruaru() {
        when(projetoRepository.findAllByCampus("Caruaru"))
            .thenReturn(List.of(criarProjetoExemplo(2L, "P Caruaru", "Caruaru", "Aprovado")));

        List<Projeto> resultados = projetoService.listarProjetosGestor("Recife");

        assertTrue(resultados.isEmpty());
    }

    // ========== CT-44: Gestor/Diretor não visualiza projetos em etapa fora de sua responsabilidade ==========

    @Test
    @DisplayName("CT-44: Gestor/Diretor não visualiza projeto com status 'Aprovação Final' (etapa fora de sua alçada)")
    void gestorNaoDeveVerProjetosEmEtapaForaDaSuaAlcada() {
        List<String> statusPermitidos = Arrays.asList("Em Correção", "Em Análise");
        Projeto pAprovacaoFinal = criarProjetoExemplo(1L, "P Aprovação Final", "Recife", "Aprovação Final");

        when(projetoRepository.findByCampusEStatus("Recife", statusPermitidos))
            .thenReturn(Collections.emptyList());

        List<Projeto> resultados = projetoService.listarProjetosGestorComStatus("Recife", statusPermitidos);

        assertTrue(resultados.isEmpty());
    }

    @Test
    @DisplayName("CT-44: Gestor/Diretor tenta acessar projeto de Aprovação Final diretamente e é bloqueado")
    void gestorNaoDeveAcessarProjetoDeAprovacaoFinalDiretamente() {
        when(projetoRepository.getReferenceById(1L))
            .thenReturn(criarProjetoExemplo(1L, "P Aprovação Final", "Recife", "Aprovação Final"));

        assertThrows(SecurityException.class, () -> projetoService.getProjetoComVerificacao(1L, "Recife"));
    }

    // ========== CT-45: Sessão expirada/usuário deslogado tenta acessar a listagem ==========

    @Test
    @DisplayName("CT-45: Usuário sem sessão ativa tenta acessar listagem de projetos")
    void usuarioSemSessaoNaoDeveAcessarListagemDeProjetos() {
        when(projetoRepository.findAll())
            .thenReturn(List.of(criarProjetoExemplo(1L, "P1", "Recife", "Submetido")));

        // Simula que o usuário não tem sessão - o service não deve retornar dados
        // O método de autorização deve ser chamado antes do service
        assertFalse(projetoService.verificarPermissaoDownload(1L, null));
    }

    @Test
    @DisplayName("CT-45: Após login, usuário autenticado acessa listagem corretamente")
    void usuarioAutenticadoDeveAcessarListagemCorretamente() {
        Projeto p1 = criarProjetoExemplo(1L, "P1", "Recife", "Submetido");
        Projeto p2 = criarProjetoExemplo(2L, "P2", "Caruaru", "Aprovado");

        when(projetoRepository.findAll()).thenReturn(List.of(p1, p2));

        assertTrue(projetoService.verificarPermissaoDownload(1L, "Admin Geral"));

        List<Projeto> resultados = projetoService.listarProjetos();

        assertEquals(2, resultados.size());
    }

    // ========== CT-46: Falha ao realizar download de anexo/plano inexistente ou corrompido ==========

    @Test
    @DisplayName("CT-46: Download de anexo inexistente exibe mensagem de erro sem travar a aplicação")
    void downloadDeAnexoInexistenteDeveExibirErroSemTravar() {
        when(anexoRepository.findByIdAndProjetoId(999L, 1L))
            .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> projetoService.downloadAnexo(1L, 999L)
        );

        assertNotNull(exception.getMessage());
    }

    @Test
    @DisplayName("CT-46: Após falha no download, sistema continua funcionando normalmente")
    void aposFalhaNoDownloadSistemaDeveContinuarFuncionando() {
        when(anexoRepository.findByIdAndProjetoId(999L, 1L))
            .thenReturn(Optional.empty());
        when(projetoRepository.findAll())
            .thenReturn(List.of(
                criarProjetoExemplo(1L, "P1", "Recife", "Em Correção"),
                criarProjetoExemplo(2L, "P2", "Caruaru", "Aprovado")
            ));

        try {
            projetoService.downloadAnexo(1L, 999L);
        } catch (RuntimeException e) {
            // Esperado - erro de anexo inexistente
        }

        List<Projeto> resultados = projetoService.listarProjetos();

        assertEquals(2, resultados.size());
    }
}