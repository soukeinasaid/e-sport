package api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import entity.Tournoi;
import org.json.JSONArray;
import org.json.JSONObject;
import service.TournoiService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * HTTP Handler for Tournament REST API
 * Handles GET, POST, PUT, DELETE requests for tournaments
 */
public class TournoiApiHandler implements HttpHandler {

    private final TournoiService tournoiService = new TournoiService();
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            if ("OPTIONS".equals(method)) {
                handleOptions(exchange);
            } else if ("GET".equals(method)) {
                handleGet(exchange, path);
            } else if ("POST".equals(method)) {
                handlePost(exchange);
            } else if ("PUT".equals(method)) {
                handlePut(exchange, path);
            } else if ("DELETE".equals(method)) {
                handleDelete(exchange, path);
            } else {
                ApiApplication.sendResponse(exchange, 405,
                    ApiApplication.createErrorResponse("Method not allowed"));
            }
        } catch (Exception e) {
            ApiApplication.sendResponse(exchange, 500,
                ApiApplication.createErrorResponse("Internal server error: " + e.getMessage()));
        }
    }

    private void handleOptions(HttpExchange exchange) throws IOException {
        ApiApplication.sendResponse(exchange, 200, "");
    }

    private void handleGet(HttpExchange exchange, String path) throws IOException {
        try {
            String[] parts = path.split("/");

            if (parts.length == 3 && parts[2].isEmpty()) {
                // GET /api/tournois
                List<Tournoi> tournois = tournoiService.listerTous();
                JSONArray arr = new JSONArray();
                for (Tournoi t : tournois) {
                    arr.put(tournoiToJson(t));
                }
                ApiApplication.sendResponse(exchange, 200, arr.toString());
            } else if (parts.length == 4) {
                // GET /api/tournois/{id}
                try {
                    int id = Integer.parseInt(parts[3]);
                    List<Tournoi> tournois = tournoiService.listerTous();
                    Tournoi t = tournois.stream().filter(x -> x.getId() == id).findFirst().orElse(null);

                    if (t != null) {
                        ApiApplication.sendResponse(exchange, 200, tournoiToJson(t).toString());
                    } else {
                        ApiApplication.sendResponse(exchange, 404,
                            ApiApplication.createErrorResponse("Tournoi non trouvé"));
                    }
                } catch (NumberFormatException e) {
                    ApiApplication.sendResponse(exchange, 400,
                        ApiApplication.createErrorResponse("ID invalide"));
                }
            } else {
                ApiApplication.sendResponse(exchange, 400,
                    ApiApplication.createErrorResponse("Requête invalide"));
            }
        } catch (Exception e) {
            ApiApplication.sendResponse(exchange, 500,
                ApiApplication.createErrorResponse("Erreur: " + e.getMessage()));
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        try {
            String body = readBody(exchange);
            JSONObject json = new JSONObject(body);

            Tournoi t = jsonToTournoi(json);
            String validationError = validateTournoi(t);

            if (!validationError.isEmpty()) {
                ApiApplication.sendResponse(exchange, 400,
                    ApiApplication.createErrorResponse("Validation échouée: " + validationError));
                return;
            }

            tournoiService.ajouter(t);
            JSONObject response = new JSONObject()
                    .put("success", true)
                    .put("message", "Tournoi créé avec succès")
                    .put("data", tournoiToJson(t));

            ApiApplication.sendResponse(exchange, 201, response.toString());
        } catch (Exception e) {
            ApiApplication.sendResponse(exchange, 400,
                ApiApplication.createErrorResponse("Erreur JSON: " + e.getMessage()));
        }
    }

    private void handlePut(HttpExchange exchange, String path) throws IOException {
        try {
            String[] parts = path.split("/");
            if (parts.length != 4) {
                ApiApplication.sendResponse(exchange, 400,
                    ApiApplication.createErrorResponse("Requête invalide"));
                return;
            }

            int id = Integer.parseInt(parts[3]);
            String body = readBody(exchange);
            JSONObject json = new JSONObject(body);

            Tournoi t = jsonToTournoi(json);
            t.setId(id);

            String validationError = validateTournoi(t);
            if (!validationError.isEmpty()) {
                ApiApplication.sendResponse(exchange, 400,
                    ApiApplication.createErrorResponse("Validation échouée: " + validationError));
                return;
            }

            tournoiService.modifier(t);
            JSONObject response = new JSONObject()
                    .put("success", true)
                    .put("message", "Tournoi modifié avec succès")
                    .put("data", tournoiToJson(t));

            ApiApplication.sendResponse(exchange, 200, response.toString());
        } catch (NumberFormatException e) {
            ApiApplication.sendResponse(exchange, 400,
                ApiApplication.createErrorResponse("ID invalide"));
        } catch (Exception e) {
            ApiApplication.sendResponse(exchange, 400,
                ApiApplication.createErrorResponse("Erreur: " + e.getMessage()));
        }
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        try {
            String[] parts = path.split("/");
            if (parts.length != 4) {
                ApiApplication.sendResponse(exchange, 400,
                    ApiApplication.createErrorResponse("Requête invalide"));
                return;
            }

            int id = Integer.parseInt(parts[3]);
            tournoiService.supprimer(id);

            JSONObject response = new JSONObject()
                    .put("success", true)
                    .put("message", "Tournoi supprimé avec succès");

            ApiApplication.sendResponse(exchange, 200, response.toString());
        } catch (NumberFormatException e) {
            ApiApplication.sendResponse(exchange, 400,
                ApiApplication.createErrorResponse("ID invalide"));
        } catch (Exception e) {
            ApiApplication.sendResponse(exchange, 500,
                ApiApplication.createErrorResponse("Erreur: " + e.getMessage()));
        }
    }

    private JSONObject tournoiToJson(Tournoi t) {
        JSONObject obj = new JSONObject();
        obj.put("id", t.getId());
        obj.put("nom", t.getNom());
        obj.put("jeu", t.getJeu());
        obj.put("dateDebut", t.getDateDebut().format(DATE_FMT));
        obj.put("dateFin", t.getDateFin().format(DATE_FMT));
        obj.put("lieu", t.getLieu());
        obj.put("prix", t.getPrix());
        obj.put("nbEquipes", t.getNbEquipes());
        obj.put("centreId", t.getCentreId());
        return obj;
    }

    private Tournoi jsonToTournoi(JSONObject json) {
        String nom = json.optString("nom", "");
        String jeu = json.optString("jeu", "");
        LocalDate dateDebut = LocalDate.parse(json.optString("dateDebut", ""), DATE_FMT);
        LocalDate dateFin = LocalDate.parse(json.optString("dateFin", ""), DATE_FMT);
        String lieu = json.optString("lieu", "");
        double prix = json.optDouble("prix", 0);
        int nbEquipes = json.optInt("nbEquipes", 8);
        int centreId = json.optInt("centreId", 0);

        return new Tournoi(nom, jeu, dateDebut, dateFin, lieu, prix, nbEquipes, centreId);
    }

    private String validateTournoi(Tournoi t) {
        StringBuilder errors = new StringBuilder();

        if (t.getNom() == null || t.getNom().trim().isEmpty()) {
            errors.append("Nom requis. ");
        } else if (t.getNom().length() < 3) {
            errors.append("Nom doit contenir min 3 caractères. ");
        }

        if (t.getJeu() == null || t.getJeu().trim().isEmpty()) {
            errors.append("Jeu requis. ");
        }

        if (t.getDateDebut() == null) {
            errors.append("Date début requise. ");
        }
        if (t.getDateFin() == null) {
            errors.append("Date fin requise. ");
        }
        if (t.getDateDebut() != null && t.getDateFin() != null) {
            if (t.getDateFin().isBefore(t.getDateDebut())) {
                errors.append("Date fin doit être après début. ");
            }
            if (t.getDateDebut().isBefore(LocalDate.now())) {
                errors.append("Date début ne peut pas être dans le passé. ");
            }
        }

        if (t.getLieu() == null || t.getLieu().trim().isEmpty()) {
            errors.append("Lieu requis. ");
        }

        if (t.getPrix() <= 0) {
            errors.append("Prix doit être > 0. ");
        }

        if (t.getNbEquipes() < 2 || t.getNbEquipes() > 256) {
            errors.append("Équipes entre 2 et 256. ");
        }

        return errors.toString();
    }

    private String readBody(HttpExchange exchange) throws IOException {
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(exchange.getRequestBody()));
        StringBuilder body = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            body.append(line);
        }
        return body.toString();
    }
}

