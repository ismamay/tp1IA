package ht.ueh.mbds.ismael_romelus.tp1_ismael_romelus.service;

import jakarta.enterprise.context.Dependent;
import jakarta.json.*;
import jakarta.json.stream.JsonGenerator;

import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Dependent
public class JsonAdapterPourGemini implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<String[]> historique = new ArrayList<>();
    private String systemInstruction;

    public void setSystemInstruction(String instruction) {
        this.systemInstruction = instruction;
    }

    public LlmInteraction envoyerRequete(String question) throws RequeteException {
        ajouteQuestionDansJsonRequete(question);
        String jsonRequete = construireJsonRequete();
        LlmClientPourGemini client = new LlmClientPourGemini();
        String jsonReponse = client.envoyerRequete(jsonRequete);
        String reponseTexte = extractReponse(jsonReponse);
        historique.add(new String[]{"model", reponseTexte});
        return new LlmInteraction(reponseTexte, prettyPrinting(jsonRequete), prettyPrinting(jsonReponse));
    }

    private void ajouteQuestionDansJsonRequete(String question) {
        historique.add(new String[]{"user", question});
    }

    private String construireJsonRequete() {
        JsonObjectBuilder requestBuilder = Json.createObjectBuilder();
        if (systemInstruction != null && !systemInstruction.isBlank()) {
            requestBuilder.add("system_instruction", Json.createObjectBuilder()
                    .add("parts", Json.createArrayBuilder()
                            .add(Json.createObjectBuilder().add("text", systemInstruction))));
        }
        JsonArrayBuilder contentsBuilder = Json.createArrayBuilder();
        for (String[] msg : historique) {
            contentsBuilder.add(Json.createObjectBuilder()
                    .add("role", msg[0])
                    .add("parts", Json.createArrayBuilder()
                            .add(Json.createObjectBuilder().add("text", msg[1]))));
        }
        requestBuilder.add("contents", contentsBuilder);
        return requestBuilder.build().toString();
    }

    private String extractReponse(String jsonReponse) throws RequeteException {
        try (JsonReader reader = Json.createReader(new StringReader(jsonReponse))) {
            JsonObject response = reader.readObject();
            if (response.containsKey("error")) {
                JsonObject error = response.getJsonObject("error");
                throw new RequeteException("Erreur API Gemini : " + error.getString("message"));
            }
            return response.getJsonArray("candidates")
                    .getJsonObject(0)
                    .getJsonObject("content")
                    .getJsonArray("parts")
                    .getJsonObject(0)
                    .getString("text");
        } catch (RequeteException e) {
            throw e;
        } catch (Exception e) {
            throw new RequeteException("Erreur extraction réponse JSON : " + e.getMessage(), e);
        }
    }

    private String prettyPrinting(String json) {
        try (JsonReader reader = Json.createReader(new StringReader(json))) {
            JsonValue jsonValue = reader.readValue();
            StringWriter writer = new StringWriter();
            try (JsonWriter jsonWriter = Json.createWriterFactory(
                    Map.of(JsonGenerator.PRETTY_PRINTING, true)).createWriter(writer)) {
                jsonWriter.write(jsonValue);
            }
            return writer.toString();
        } catch (Exception e) {
            return json;
        }
    }

}
