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
            // Verificar si ya existe una instancia para no inicializarla dos veces
            if (FirebaseApp.getApps().isEmpty()) {
                
                InputStream serviceAccount = getClass()
                        .getClassLoader()
                        .getResourceAsStream("serviceAccountKey.json"); //acceder a la llave en resources

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