package ht.ueh.mbds.ismael_romelus.tp1_ismael_romelus.service;

import jakarta.enterprise.context.Dependent;
import java.io.Serializable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Dependent
public class LlmClientPourGemini implements Serializable {
    private static final String GEMINI_ENDPOINT =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

    private final String apiKey;

    public LlmClientPourGemini() {
        this.apiKey = System.getenv("GEMINI_KEY");
    }

    public String envoyerRequete(String jsonRequete) throws RequeteException {
        if (this.apiKey == null || this.apiKey.isBlank()) {
            throw new RequeteException("La variable d'environnement GEMINI_KEY n'est pas définie");
        }
        try {
            String url = GEMINI_ENDPOINT + "?key=" + apiKey;
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonRequete))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (RequeteException e) {
            throw e;
        } catch (Exception e) {
            throw new RequeteException("Erreur lors de l'envoi de la requête : " + e.getMessage(), e);
        }
    }
}
