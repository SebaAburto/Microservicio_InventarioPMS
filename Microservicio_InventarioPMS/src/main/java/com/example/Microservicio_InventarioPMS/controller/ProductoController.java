package com.example.Microservicio_InventarioPMS.controller;

import com.example.Microservicio_InventarioPMS.model.Producto;
import com.example.Microservicio_InventarioPMS.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    // POST: Guardar (Auto-gestiona categorías)
    @PostMapping("/guardar")
    public ResponseEntity<String> guardarProducto(@RequestBody Producto producto) {
        try {
            String id = productoService.guardarProducto(producto);
            return ResponseEntity.ok("✅ Producto guardado. ID: " + id);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("❌ Error: " + e.getMessage());
        }
    }

    // PUT: Editar (Auto-gestiona categorías y limpia las viejas)
    @PutMapping("/editar/{id}")
    public ResponseEntity<String> editarProducto(@PathVariable String id, @RequestBody Producto producto) {
        try {
            productoService.actualizarProducto(id, producto);
            return ResponseEntity.ok("✅ Producto actualizado correctamente");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("❌ Error: " + e.getMessage());
        }
    }

    // DELETE: Eliminar (Limpia categoría si queda vacía)
    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<String> eliminarProducto(@PathVariable String id) {
        try {
            productoService.eliminarProducto(id);
            return ResponseEntity.ok("✅ Producto eliminado y limpieza realizada");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("❌ Error: " + e.getMessage());
        }
    }

    // GET: Listar Todo
    @GetMapping
    public ResponseEntity<List<Producto>> listarProductos() {
        try {
            return ResponseEntity.ok(productoService.obtenerTodos());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // GET: Uno por ID
    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerPorId(@PathVariable String id) {
        try {
            Producto p = productoService.obtenerPorId(id);
            return (p != null) ? ResponseEntity.ok(p) : ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // POST: Comprar
    @PostMapping("/comprar/{id}")
    public ResponseEntity<String> comprar(@PathVariable String id, @RequestParam int cantidad) {
        try {
            return ResponseEntity.ok(productoService.reducirStock(id, cantidad));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("❌ Error: " + e.getMessage());
        }
    }

    // POST: Reponer Stock
    @PostMapping("/agregar-stock/{id}")
    public ResponseEntity<String> agregarStock(@PathVariable String id, @RequestParam int cantidad) {
        try {
            return ResponseEntity.ok(productoService.aumentarStock(id, cantidad));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("❌ Error: " + e.getMessage());
        }
    }
}