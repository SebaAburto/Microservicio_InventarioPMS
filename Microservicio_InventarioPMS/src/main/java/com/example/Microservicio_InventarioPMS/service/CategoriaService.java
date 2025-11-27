package com.example.Microservicio_InventarioPMS.service;

import com.example.Microservicio_InventarioPMS.model.Categoria;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class CategoriaService {

    private static final String COLLECTION_CATEGORIAS = "categorias";
    private static final String COLLECTION_PRODUCTOS = "productos";

    // --- 1. LÓGICA INTELIGENTE: BUSCAR O CREAR ---
    public String resolverIdPorNombre(String nombreCategoria) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        
        // Buscamos si ya existe por nombre
        ApiFuture<QuerySnapshot> query = db.collection(COLLECTION_CATEGORIAS)
                .whereEqualTo("nombre", nombreCategoria)
                .limit(1)
                .get();
        
        List<QueryDocumentSnapshot> documents = query.get().getDocuments();

        if (!documents.isEmpty()) {
            return documents.get(0).getId(); // Ya existe, devolvemos ID
        } else {
            // No existe, creamos nueva
            Categoria nuevaCat = Categoria.builder().nombre(nombreCategoria).build();
            ApiFuture<DocumentReference> addedDoc = db.collection(COLLECTION_CATEGORIAS).add(nuevaCat);
            return addedDoc.get().getId(); // Devolvemos el nuevo ID
        }
    }

    // --- 2. LÓGICA DE LIMPIEZA: BORRAR SI ESTÁ VACÍA ---
    public void verificarYBorrarSiVacia(String categoriaId) throws ExecutionException, InterruptedException {
        if (categoriaId == null) return;

        Firestore db = FirestoreClient.getFirestore();

        // Consultamos: ¿Hay algún producto que use este categoriaId?
        ApiFuture<QuerySnapshot> query = db.collection(COLLECTION_PRODUCTOS)
                .whereEqualTo("categoriaId", categoriaId)
                .limit(1) // Solo necesitamos saber si hay al menos 1
                .get();

        if (query.get().isEmpty()) {
            // Si está vacía (no hay productos), borramos la categoría
            System.out.println("🧹 Limpieza automática: Borrando categoría vacía ID: " + categoriaId);
            db.collection(COLLECTION_CATEGORIAS).document(categoriaId).delete();
        }
    }

    // --- MÉTODOS ESTÁNDAR ---
    public List<Categoria> listarCategorias() throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_CATEGORIAS).get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();

        List<Categoria> lista = new ArrayList<>();
        for (QueryDocumentSnapshot doc : documents) {
            Categoria cat = doc.toObject(Categoria.class);
            cat.setId(doc.getId());
            lista.add(cat);
        }
        return lista;
    }

    public Categoria obtenerPorId(String id) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentSnapshot doc = db.collection(COLLECTION_CATEGORIAS).document(id).get().get();
        if(doc.exists()) {
            Categoria cat = doc.toObject(Categoria.class);
            cat.setId(doc.getId());
            return cat;
        }
        return null;
    }

    public void actualizarCategoria(String id, Categoria categoria) {
        FirestoreClient.getFirestore().collection(COLLECTION_CATEGORIAS).document(id).set(categoria);
    }
    
    public void eliminarCategoria(String id) {
        FirestoreClient.getFirestore().collection(COLLECTION_CATEGORIAS).document(id).delete();
    }
}