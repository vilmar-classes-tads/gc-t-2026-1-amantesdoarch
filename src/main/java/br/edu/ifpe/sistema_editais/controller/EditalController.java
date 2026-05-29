package br.edu.ifpe.sistema_editais.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.ifpe.sistema_editais.dto.EditalDto;
import br.edu.ifpe.sistema_editais.entity.Edital;
import br.edu.ifpe.sistema_editais.service.EditalService;

@RestController
@RequestMapping("/api/edital")
public class EditalController {

    private final EditalService editalService;

    public EditalController(EditalService editalService) {
        this.editalService = editalService;
    }

    @GetMapping
    public ResponseEntity<?> listar() {
        try {
            List<Edital> editais = editalService.listar();
            return ResponseEntity.ok(editais);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<String> criar(@RequestBody EditalDto dto) {
        try {
            editalService.criar(dto);
            return ResponseEntity.status(201).body("Edital criado com sucesso!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> editar(@PathVariable Long id, @RequestBody EditalDto dto) {
        try {
            editalService.editar(id, dto);
            return ResponseEntity.ok("Edital atualizado com sucesso!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }
}