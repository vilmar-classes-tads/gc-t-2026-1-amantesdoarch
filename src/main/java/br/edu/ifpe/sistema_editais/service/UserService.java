package br.edu.ifpe.sistema_editais.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import br.edu.ifpe.sistema_editais.dto.UserRegistrationDto;
import br.edu.ifpe.sistema_editais.entity.Perfil;
import br.edu.ifpe.sistema_editais.entity.User;
import br.edu.ifpe.sistema_editais.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    public void cadastrar(UserRegistrationDto dto) {
        // 1. Verificar campos obrigatórios
        if (dto.nome() == null || 
            dto.email() == null || 
            dto.cpf() == null || dto.campus() == null || 
            dto.area_formacao() == null || dto.titulacao() == null || 
            dto.senha() == null
        ) {
            throw new IllegalArgumentException("Campos obrigatórios não preenchidos");
        }

        // 2. Verificar unicidade de email
        if (userRepository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("Email institucional já cadastrado");
        }

        // 3. Verificar unicidade de CPF
        if (userRepository.existsByCpf(dto.cpf())) {
            throw new IllegalArgumentException("CPF já cadastrado");
        }

        // 4. Verificar se a senha tem mais de 5 caracteres
        if (dto.senha().length() <= 5) {
            throw new IllegalArgumentException("Senha deve conter mais de 5 caracteres");
        }
        
        // 5. Criar o usuário e salvar no banco
        User user = new User();
        user.setNome(dto.nome());
        user.setEmail(dto.email());
        user.setCpf(dto.cpf());
        user.setCampus(dto.campus());
        user.setArea_formacao(dto.area_formacao());
        user.setTitulacao(dto.titulacao());
        user.setSenha(passwordEncoder.encode(dto.senha())); // Criptografa a senha antes de salvar
        user.setPerfis(Arrays.asList(Perfil.ROLE_AVALIADOR, Perfil.ROLE_COORDENADOR));

        userRepository.save(user);
    }

    public List<User> listar() {
        return userRepository.findAll();
    }

}
