package br.edu.ifpe.sistema_editais.repository;

import br.edu.ifpe.sistema_editais.entity.User;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    public boolean existsByEmail(String email);
    public boolean existsByCpf(String cpf);
}
