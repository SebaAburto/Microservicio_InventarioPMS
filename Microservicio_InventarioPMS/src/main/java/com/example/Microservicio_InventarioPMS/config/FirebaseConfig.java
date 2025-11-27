package com.example.Microservicio_InventarioPMS.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initialize() {
        try {
            // Verifica si ya existe una instancia para no inicializarla dos veces
            if (FirebaseApp.getApps().isEmpty()) {
                
                // Busca el archivo en la carpeta resources
                InputStream serviceAccount = getClass()
                        .getClassLoader()
                        .getResourceAsStream("serviceAccountKey.json");

                if (serviceAccount == null) {
                    throw new IOException("No se encontró el archivo serviceAccountKey.json en resources");
                }

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                FirebaseApp.initializeApp(options);
                System.out.println("Conexión a Firebase exitosa");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error conectando a Firebase: " + e.getMessage());
        }
    }
}