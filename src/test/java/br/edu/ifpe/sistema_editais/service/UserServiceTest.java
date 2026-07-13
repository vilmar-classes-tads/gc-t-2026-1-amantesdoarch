package br.edu.ifpe.sistema_editais.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import br.edu.ifpe.sistema_editais.dto.UserRegistrationDto;
import br.edu.ifpe.sistema_editais.entity.Perfil;
import br.edu.ifpe.sistema_editais.entity.User;
import br.edu.ifpe.sistema_editais.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private UserRegistrationDto dtoValido() {
        return new UserRegistrationDto(
            "José da Silva",
            "111.222.3330-44",
            "jose@ifpe.edu.br",
            "Recife",
            "Análise e Desenvolvimento de Sistemas",
            "Graduação",
            "senhaForte123", 
            null, 
            null, 
            null, 
            null
        );
    }

    // ---------- CAMINHO FELIZ ----------
    @Test
    void deveCadastrarUsuarioComSucesso() {
        var dto = dtoValido();

        when(userRepository.existsByEmail(dto.email())).thenReturn(false);
        when(userRepository.existsByCpf(dto.cpf())).thenReturn(false);
        when(passwordEncoder.encode(dto.senha())).thenReturn("senhaCriptografada");

        userService.cadastrar(dto);

        var userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User userSalvo = userCaptor.getValue();
        assertThat(userSalvo.getNome()).isEqualTo("José da Silva");
        assertThat(userSalvo.getEmail()).isEqualTo("jose@ifpe.edu.br");
        assertThat(userSalvo.getSenha()).isEqualTo("senhaCriptografada");
        assertThat(userSalvo.getPerfis()).containsExactly(Perfil.ROLE_AVALIADOR, Perfil.ROLE_COORDENADOR);
    }
    @Test
    void deveCriptografarSenhaAntesDeSalvar() {
        var dto = dtoValido();
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByCpf(anyString())).thenReturn(false);
        when(passwordEncoder.encode(dto.senha())).thenReturn("hash123");

        userService.cadastrar(dto);

        verify(passwordEncoder, times(1)).encode(dto.senha());
    }

    // ---------- CAMINHOS TRISTES: CAMPOS OBRIGATÓRIOS ----------
    static UserRegistrationDto dtoComCampoNulo() {
        return new UserRegistrationDto(
            null, 
            "111.333.444-70", 
            "e@e.com", 
            "Recife", 
            "CC", 
            "Graduação", 
            "senha123", 
            null, 
            null, 
            null, 
            null
        );
    }
    @Test
    void deveLancarExcecaoQuandoCampoObrigatorioForNulo() {
        UserRegistrationDto dto = dtoComCampoNulo();
        assertThatThrownBy(() -> userService.cadastrar(dto))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Campos obrigatórios não preenchidos");

        verify(userRepository, never()).save(any());
    }

    // ---------- CAMINHO TRISTE: EMAIL DUPLICADO ----------
    @Test
    void deveLancarExcecaoQuandoEmailJaCadastrado() {
        var dto = dtoValido();
        when(userRepository.existsByEmail(dto.email())).thenReturn(true);

        assertThatThrownBy(() -> userService.cadastrar(dto))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Email institucional já cadastrado");

        verify(userRepository, never()).save(any());
        verify(userRepository, never()).existsByCpf(anyString()); // nem deve chegar a checar CPF
    }

    // ---------- CAMINHO TRISTE: CPF DUPLICADO ----------
    @Test
    void deveLancarExcecaoQuandoCpfJaCadastrado() {
        var dto = dtoValido();
        when(userRepository.existsByEmail(dto.email())).thenReturn(false);
        when(userRepository.existsByCpf(dto.cpf())).thenReturn(true);

        assertThatThrownBy(() -> userService.cadastrar(dto))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("CPF já cadastrado");

        verify(userRepository, never()).save(any());
    }

    // ---------- CAMINHO TRISTE: SENHA CURTA ----------
    @ParameterizedTest
    @MethodSource("senhasInvalidas")
    void deveLancarExcecaoQuandoSenhaForMuitoCurta(String senhaCurta) {
        var dto = new UserRegistrationDto(
            "José", 
            "111-333-444-00", 
            "jose@ifpe.edu.br",
            "Recife", 
            "CC", 
            "Graduação", 
            senhaCurta, 
            null, 
            null, 
            null, 
            null
        );
        when(userRepository.existsByEmail(dto.email())).thenReturn(false);
        when(userRepository.existsByCpf(dto.cpf())).thenReturn(false);

        assertThatThrownBy(() -> userService.cadastrar(dto))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Senha deve conter mais de 5 caracteres");

        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }
    static List<String> senhasInvalidas() {
        return List.of("", "1", "12345");
    }

    // ---------- LISTAR ----------
    @Test
    void deveListarTodosOsUsuarios() {
        var user1 = new User();
        user1.setNome("Gilberto");
        var user2 = new User();
        user2.setNome("Ana");

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        List<User> resultado = userService.listar();

        assertThat(resultado).hasSize(2);
        assertThat(resultado).extracting(User::getNome).containsExactly("Gilberto", "Ana");
    }
    @Test
    void deveRetornarListaVaziaQuandoNaoHouverUsuarios() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<User> resultado = userService.listar();

        assertThat(resultado).isEmpty();
    }
}