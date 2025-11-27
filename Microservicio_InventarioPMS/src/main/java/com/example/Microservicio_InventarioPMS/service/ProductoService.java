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

    // 1. GUARDAR (Resuelve nombre a ID)
    public String guardarProducto(Producto producto) throws ExecutionException, InterruptedException {
        // Obtenemos ID de categoría basado en el nombre
        String catId = categoriaService.resolverIdPorNombre(producto.getCategoriaNombre());
        producto.setCategoriaId(catId);

        Firestore db = FirestoreClient.getFirestore();
        ApiFuture<DocumentReference> future = db.collection(COLLECTION_NAME).add(producto);
        return future.get().getId();
    }

    // 2. ACTUALIZAR (Maneja cambio de categoría y limpieza)
    public void actualizarProducto(String idProducto, Producto productoNuevo) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        
        // A. Obtenemos el producto ANTIGUO para saber cuál era su categoría vieja
        Producto productoAntiguo = obtenerPorId(idProducto);
        String idCategoriaVieja = (productoAntiguo != null) ? productoAntiguo.getCategoriaId() : null;

        // B. Resolvemos el ID de la categoría NUEVA (por si cambió el nombre)
        String idCategoriaNueva = categoriaService.resolverIdPorNombre(productoNuevo.getCategoriaNombre());
        productoNuevo.setCategoriaId(idCategoriaNueva);

        // C. Actualizamos el producto
        db.collection(COLLECTION_NAME).document(idProducto).set(productoNuevo);

        // D. LIMPIEZA: Si la categoría cambió, verificamos si la vieja quedó vacía
        if (idCategoriaVieja != null && !idCategoriaVieja.equals(idCategoriaNueva)) {
            categoriaService.verificarYBorrarSiVacia(idCategoriaVieja);
        }
    }

    // 3. ELIMINAR (Maneja limpieza de categoría)
    public void eliminarProducto(String idProducto) throws ExecutionException, InterruptedException {
        // A. Obtenemos la categoría antes de borrar
        Producto producto = obtenerPorId(idProducto);
        String idCategoria = (producto != null) ? producto.getCategoriaId() : null;

        // B. Borramos el producto
        Firestore db = FirestoreClient.getFirestore();
        db.collection(COLLECTION_NAME).document(idProducto).delete();

        // C. LIMPIEZA: Verificamos si esa categoría quedó vacía
        if (idCategoria != null) {
            // Damos un pequeño delay técnico o lo ejecutamos directo
            // Firestore es rápido, pero para asegurar consistencia:
            Thread.sleep(100); 
            categoriaService.verificarYBorrarSiVacia(idCategoria);
        }
    }

    // 4. OBTENER POR ID
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

    // 5. LISTAR TODOS
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

    // 6. REDUCIR STOCK (Compra)
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

    // 7. AUMENTAR STOCK
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
    
    // Buscar por categoría (Método auxiliar)
    public List<Producto> buscarPorCategoria(String categoriaId) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        List<QueryDocumentSnapshot> docs = db.collection(COLLECTION_NAME).whereEqualTo("categoriaId", categoriaId).get().get().getDocuments();
        List<Producto> lista = new ArrayList<>();
        for(QueryDocumentSnapshot doc : docs) {
            Producto p = doc.toObject(Producto.class);
            p.setId(doc.getId());
            lista.add(p);
        }
        return lista;
    }
}