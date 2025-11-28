package com.example.Microservicio_InventarioPMS.service;

import com.example.Microservicio_InventarioPMS.model.Producto;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class ProductoService {

    private static final String COLLECTION_NAME = "productos";

    @Autowired
    private CategoriaService categoriaService;

    // GUARDAR
    public String guardarProducto(Producto producto) throws ExecutionException, InterruptedException {
        
        String catId = categoriaService.resolverIdPorNombre(producto.getCategoriaNombre());
        producto.setCategoriaId(catId);

        Firestore db = FirestoreClient.getFirestore();
        ApiFuture<DocumentReference> future = db.collection(COLLECTION_NAME).add(producto);
        return future.get().getId();
    }

    // ACTUALIZAR
    public void actualizarProducto(String idProducto, Producto productoNuevo) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        
        // Obtener el producto ANTIGUO
        Producto productoAntiguo = obtenerPorId(idProducto);
        String idCategoriaVieja = (productoAntiguo != null) ? productoAntiguo.getCategoriaId() : null;

        // resolver id de la categoria nueva por si el antiguo cambió
        String idCategoriaNueva = categoriaService.resolverIdPorNombre(productoNuevo.getCategoriaNombre());
        productoNuevo.setCategoriaId(idCategoriaNueva);

        // Actualizar el producto
        db.collection(COLLECTION_NAME).document(idProducto).set(productoNuevo);

        // LIMPIEZA de categoria
        if (idCategoriaVieja != null && !idCategoriaVieja.equals(idCategoriaNueva)) {
            categoriaService.verificarYBorrarSiVacia(idCategoriaVieja);
        }
    }

    // ELIMINAR
    public void eliminarProducto(String idProducto) throws ExecutionException, InterruptedException {
        
        Producto producto = obtenerPorId(idProducto);
        String idCategoria = (producto != null) ? producto.getCategoriaId() : null;

        
        Firestore db = FirestoreClient.getFirestore();
        db.collection(COLLECTION_NAME).document(idProducto).delete();

        // LIMPIEZA
        if (idCategoria != null) {
            // Damos un pequeño delay técnico o lo ejecutamos directo
            // Firestore es rápido, pero para asegurar consistencia:
            Thread.sleep(100); 
            categoriaService.verificarYBorrarSiVacia(idCategoria);
        }
    }

    // OBTENER POR ID
    public Producto obtenerPorId(String id) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentSnapshot doc = db.collection(COLLECTION_NAME).document(id).get().get();
        if (doc.exists()) {
            Producto p = doc.toObject(Producto.class);
            p.setId(doc.getId());
            return p;
        }
        return null;
    }

    // LISTAR TODOS
    public List<Producto> obtenerTodos() throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        List<QueryDocumentSnapshot> docs = db.collection(COLLECTION_NAME).get().get().getDocuments();
        List<Producto> lista = new ArrayList<>();
        for (QueryDocumentSnapshot doc : docs) {
            Producto p = doc.toObject(Producto.class);
            p.setId(doc.getId());
            lista.add(p);
        }
        return lista;
    }

    // REDUCIR STOCK (Compra)
    //[NO USADO EN LA VERSION FINAL]
    public String reducirStock(String productoId, int cantidad) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(productoId);

        return db.runTransaction(t -> {
            DocumentSnapshot snapshot = t.get(docRef).get();
            if (!snapshot.exists()) throw new RuntimeException("Producto no existe");

            Long stockActual = snapshot.getLong("stock");
            Long stockMin = snapshot.getLong("stockMinimo");
            int current = (stockActual != null) ? stockActual.intValue() : 0;
            int min = (stockMin != null) ? stockMin.intValue() : 0;

            if (current < cantidad) throw new RuntimeException("Stock insuficiente (" + current + ")");

            int nuevo = current - cantidad;
            t.update(docRef, "stock", nuevo);

            if (nuevo == 0) return "AGOTADO (0 stock)";
            if (nuevo <= min) return "Stock Bajo (" + nuevo + ")";
            return "OK. Quedan " + nuevo;
        }).get();
    }

    // AUMENTAR STOCK RAPIDO
    //[NO USADO EN LA VERSION FINAL]
    public String aumentarStock(String productoId, int cantidad) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(productoId);

        return db.runTransaction(t -> {
            DocumentSnapshot snapshot = t.get(docRef).get();
            if (!snapshot.exists()) throw new RuntimeException("Producto no existe");
            
            Long stockActual = snapshot.getLong("stock");
            int current = (stockActual != null) ? stockActual.intValue() : 0;
            int nuevo = current + cantidad;
            
            t.update(docRef, "stock", nuevo);
            return "Stock actualizado: " + nuevo;
        }).get();
    }
    
}