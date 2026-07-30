package br.edu.ifpe.sistema_editais.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    @Test
    @DisplayName("CT-01 - Deve lançar exceção quando o campo obrigatório CPF não for preenchido")
    void deveFalharQuandoCpfNaoEhPreenchido() {
        UserRegistrationDto dto = new UserRegistrationDto(
                "José da Silva",
                null,
                "jose@gmail.com",
                "Recife",
                "Ciência da Computação",
                "Doutorado",
                "12345678",
                null,
                null,
                null,
                null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.cadastrar(dto));

        assertEquals(
                "Campos obrigatórios não preenchidos",
                exception.getMessage());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("CT-02 - Deve cadastrar com sucesso preenchendo todos os campos (obrigatórios e opcionais)")
    void deveCadastrarComSucessoComTodosOsCampos() {
        UserRegistrationDto dto = new UserRegistrationDto(
                "Maria Oliveira",
                "111.222.333-44",
                "maria@recife.ifpe.edu.br",
                "Recife",
                "Edificações",
                "Mestrado",
                "senha123",
                "Maria",
                "Feminino",
                "http://lattes.cnpq.br/1234567890",
                "(81) 99999-9999");

        when(userRepository.existsByEmail(dto.email())).thenReturn(false);
        when(userRepository.existsByCpf(dto.cpf())).thenReturn(false);
        when(passwordEncoder.encode(dto.senha())).thenReturn("$2a$10$hashFalsoDeExemplo");

        assertDoesNotThrow(() -> userService.cadastrar(dto));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User userSalvo = userCaptor.getValue();

        assertEquals("Maria Oliveira", userSalvo.getNome());
        assertEquals("111.222.333-44", userSalvo.getCpf());
        assertEquals("maria@recife.ifpe.edu.br", userSalvo.getEmail());
        assertEquals("Recife", userSalvo.getCampus());
        assertEquals("Edificações", userSalvo.getArea_formacao());
        assertEquals("Mestrado", userSalvo.getTitulacao());

        assertEquals("$2a$10$hashFalsoDeExemplo", userSalvo.getSenha());

        assertTrue(userSalvo.getPerfis().contains(Perfil.ROLE_COORDENADOR));
        assertTrue(userSalvo.getPerfis().contains(Perfil.ROLE_AVALIADOR));
    }

    @Test
    @DisplayName("CT-03 - Deve cadastrar com sucesso preenchendo apenas os campos obrigatórios")
    void deveCadastrarComSucessoApenasComCamposObrigatorios() {
        UserRegistrationDto dto = new UserRegistrationDto(
                "Carlos Eduardo da Silva",
                "555.666.777-88",
                "carlos@recife.ifpe.edu.br",
                "Recife",
                "Turismo",
                "Especialização",
                "password123",
                null,
                null,
                null,
                null);

        when(userRepository.existsByEmail(dto.email())).thenReturn(false);
        when(userRepository.existsByCpf(dto.cpf())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$outroHashFalso");

        assertDoesNotThrow(() -> userService.cadastrar(dto));

        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("CT-04 - Deve falhar ao tentar cadastrar com CPF já existente")
    void deveFalharQuandoCpfJaExiste() {
        UserRegistrationDto dto = new UserRegistrationDto(
                "Ana Souza",
                "111.222.333-44",
                "ana@recife.ifpe.edu.br",
                "Recife",
                "Ciência da Computação",
                "Doutorado",
                "12345678",
                null,
                null,
                null,
                null);

        when(userRepository.existsByCpf(dto.cpf())).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.cadastrar(dto));

        assertEquals(
                "CPF já cadastrado",
                exception.getMessage());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("CT-05 - Deve falhar ao tentar cadastrar com E-mail já existente")
    void deveFalharQuandoEmailJaExiste() {
        UserRegistrationDto dto = new UserRegistrationDto(
                "Fernando Costa",
                "999.888.777-66",
                "maria@recife.ifpe.edu.br",
                "Recife",
                "Redes de Computadores",
                "Graduação",
                "senha123",
                null,
                null,
                null,
                null);

        when(userRepository.existsByEmail(dto.email())).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.cadastrar(dto));

        assertEquals(
                "Email institucional já cadastrado",
                exception.getMessage());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("CT-06 - Deve falhar ao tentar cadastrar com senha menor que 6 caracteres")
    void deveFalharQuandoSenhaMenorQueSeisCaracteres() {
        UserRegistrationDto dto = new UserRegistrationDto(
                "Rafael Lima",
                "123.123.123-12",
                "rafael@recife.ifpe.edu.br",
                "Recife",
                "Engenharia Civil",
                "Doutorado",
                "12345",
                null,
                null,
                null,
                null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.cadastrar(dto));

        assertEquals(
                "Senha deve conter mais de 5 caracteres",
                exception.getMessage());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("CT-07 - Deve falhar ao tentar cadastrar sem preencher o campo obrigatório Nome")
    void deveFalharQuandoNomeNaoEhPreenchido() {
        UserRegistrationDto dto = new UserRegistrationDto(
                null,
                "111.222.333-44",
                "jose@gmail.com",
                "Recife",
                "Ciência da Computação",
                "Doutorado",
                "12345678",
                null,
                null,
                null,
                null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.cadastrar(dto));

        assertEquals(
                "Campos obrigatórios não preenchidos",
                exception.getMessage());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("CT-08 - Deve falhar ao tentar cadastrar sem preencher o campo obrigatório Senha")
    void deveFalharQuandoSenhaNaoEhPreenchida() {
        UserRegistrationDto dto = new UserRegistrationDto(
                "José da Silva",
                "111.222.333-44",
                "jose@gmail.com",
                "Recife",
                "Ciência da Computação",
                "Doutorado",
                null,
                null,
                null,
                null,
                null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.cadastrar(dto));

        assertEquals(
                "Campos obrigatórios não preenchidos",
                exception.getMessage());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("CT-09 - Deve falhar ao tentar cadastrar sem preencher o campo obrigatório Campus")
    void deveFalharQuandoCampusNaoEhPreenchido() {
        UserRegistrationDto dto = new UserRegistrationDto(
                "José da Silva",
                "111.222.333-44",
                "jose@gmail.com",
                null,
                "Ciência da Computação",
                "Doutorado",
                "12345678",
                null,
                null,
                null,
                null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.cadastrar(dto));

        assertEquals(
                "Campos obrigatórios não preenchidos",
                exception.getMessage());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("CT-10 - Deve falhar ao tentar cadastrar sem preencher o campo obrigatório Área de Formação")
    void deveFalharQuandoAreaFormacaoNaoEhPreenchida() {
        UserRegistrationDto dto = new UserRegistrationDto(
                "José da Silva",
                "111.222.333-44",
                "jose@gmail.com",
                "Recife",
                null,
                "Doutorado",
                "12345678",
                null,
                null,
                null,
                null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.cadastrar(dto));

        assertEquals(
                "Campos obrigatórios não preenchidos",
                exception.getMessage());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("CT-11 - Deve falhar ao tentar cadastrar sem preencher o campo obrigatório Titulação")
    void deveFalharQuandoTitulacaoNaoEhPreenchida() {
        UserRegistrationDto dto = new UserRegistrationDto(
                "José da Silva",
                "111.222.333-44",
                "jose@gmail.com",
                "Recife",
                "Ciência da Computação",
                null,
                "12345678",
                null,
                null,
                null,
                null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.cadastrar(dto));

        assertEquals(
                "Campos obrigatórios não preenchidos",
                exception.getMessage());

        verify(userRepository, never()).save(any(User.class));
    }

}
