package com.example.Microservicio_InventarioPMS.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Categoria {

    private String id;

    @NotBlank(message = "El nombre de la categoría es obligatorio")
    private String nombre;

    // Tortas Cuadradas
    // Tortas Circulares
    // Postres Individuales
    // Productos Sin Azúcar
    // Pastelería Tradicional
    // Productos sin gluten
    // Productos Vegana
    // Tortas Especiales
}