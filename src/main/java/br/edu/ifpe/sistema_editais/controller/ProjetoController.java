package br.edu.ifpe.sistema_editais.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.ifpe.sistema_editais.dto.ProjetoDto;
import br.edu.ifpe.sistema_editais.entity.Membro;
import br.edu.ifpe.sistema_editais.entity.PlanoDeTrabalho;
import br.edu.ifpe.sistema_editais.service.ProjetoService;

@RestController
@RequestMapping("/api/projeto")
public class ProjetoController {

    private final ProjetoService projetoService;

    public ProjetoController(ProjetoService projetoService) {
        this.projetoService = projetoService;
    }

    @PostMapping
    public ResponseEntity<Void> criar(@RequestBody ProjetoDto dto) {
        projetoService.criarProjeto(dto);
        return ResponseEntity.ok().build();
    }

    @PutMapping
    public ResponseEntity<Void> editar(@RequestBody ProjetoDto dto) {
        projetoService.editarProjeto(dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/membro")
    public ResponseEntity<String> adicionarMembro(@PathVariable Long id, @RequestBody Membro membro) {
        try {
            projetoService.adicionarMembro(id, membro);
            return ResponseEntity.status(201).body("Membro adicionado com sucesso!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}/membro/{index}")
    public ResponseEntity<String> removerMembro(@PathVariable Long id, @PathVariable int index) {
        try {
            projetoService.removerMembro(id, index);
            return ResponseEntity.ok("Membro removido com sucesso!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/plano")
    public ResponseEntity<String> adicionarPlano(@PathVariable Long id, @RequestBody PlanoDeTrabalho plano) {
        try {
            projetoService.adicionarPlano(id, plano);
            return ResponseEntity.status(201).body("Plano adicionado com sucesso!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}/plano/{index}")
    public ResponseEntity<String> removerPlano(@PathVariable Long id, @PathVariable int index) {
        try {
            projetoService.removerPlano(id, index);
            return ResponseEntity.ok("Plano removido com sucesso!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}