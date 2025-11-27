package com.example.Microservicio_InventarioPMS.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Producto {

    private String id;

    @NotBlank(message = "El SKU es obligatorio")
    private String sku;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    private String descripcion;

    @NotNull(message = "El precio es obligatorio")
    @Min(value = 0, message = "El precio no puede ser negativo")
    private Double precio;

    @Builder.Default
    private Double precioOferta = 0.0;

    private String imagen;

    private String categoriaId; 

    @NotBlank(message = "El nombre de la categoría es obligatorio")
    private String categoriaNombre;

    @Builder.Default
    private Boolean enOferta = false;

    @Builder.Default
    private Boolean destacado = false;
    
    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock;

    @Builder.Default
    private Integer stockMinimo = 5;
}