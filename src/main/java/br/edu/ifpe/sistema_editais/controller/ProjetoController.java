package br.edu.ifpe.sistema_editais.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.ifpe.sistema_editais.dto.ProjetoDto;
import br.edu.ifpe.sistema_editais.entity.Projeto;
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

}
