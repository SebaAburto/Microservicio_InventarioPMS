package com.example.Microservicio_InventarioPMS.controller;

import com.example.Microservicio_InventarioPMS.model.Categoria;
import com.example.Microservicio_InventarioPMS.service.CategoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;

    @GetMapping
    public ResponseEntity<List<Categoria>> listar() {
        try {
            return ResponseEntity.ok(categoriaService.listarCategorias());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Categoria> obtenerPorId(@PathVariable String id) {
        try {
            Categoria c = categoriaService.obtenerPorId(id);
            return (c != null) ? ResponseEntity.ok(c) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
}