package org.example.orquestador;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class OrquestadorController {

    @Autowired
    private RestTemplate restTemplate;
    

    @GetMapping("/usuarios")
    public ResponseEntity<String> getUsuarios() {
        String url = "http://gestion-usuarios:8081/user";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        return response;
    }

    @GetMapping("/canchas")
    public ResponseEntity<String> getCanchas() {
        String url = "http://gestion-canchas:8082/field";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        return response;
    }

    @GetMapping("/pagos")
    public ResponseEntity<String> getPagos() {
        String url = "http://gestion-pagos:3000/payment-methods";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        return response;
    }

    @GetMapping("/reservas")
    public ResponseEntity<String> getReservas() {
        String url = "http://gestion-reservas:8000/api/v1/reservations";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        return response;
    }


    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/reservas/detalles/{id}")
    public ResponseEntity<Map<String, Object>> getReservationDetails(@PathVariable String id) throws Exception {
        String reservationUrl = "http://gestion-reservas:8000/api/v1/reservations/" + id;
        String reservationResponse = restTemplate.getForObject(reservationUrl, String.class);
        System.out.println("Reservation Response: " + reservationResponse);

        Map<String, Object> reservationJson = objectMapper.readValue(reservationResponse, HashMap.class);
        String userId = (String) reservationJson.get("user");
        String fieldId = (String) reservationJson.get("field");

        String userUrl = "http://gestion-usuarios:8081/user/get/" + userId;
        String userResponse = restTemplate.getForObject(userUrl, String.class);
        System.out.println("User Response: " + userResponse);

        String fieldUrl = "http://gestion-canchas:8082/field/" + fieldId;
        String fieldResponse = restTemplate.getForObject(fieldUrl, String.class);
        System.out.println("Field Response: " + fieldResponse);

        Map<String, Object> userJson = objectMapper.readValue(userResponse, HashMap.class);
        Map<String, Object> fieldJson = objectMapper.readValue(fieldResponse, HashMap.class);

        Map<String, Object> resultJson = new HashMap<>();
        resultJson.put("reservation", reservationJson);
        resultJson.put("user", userJson);
        resultJson.put("field", fieldJson);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(resultJson);
    }



    @PostMapping("/reserva-con-pago")
    public ResponseEntity<Map<String, Object>> crearReservaConPago(@RequestBody Map<String, Object> reservaRequest) {
        String reservaServiceUrl = "http://gestion-reservas:8000/api/v1/reservations/";
        String fieldServiceUrlBase = "http://gestion-canchas:8082/field/";
        String pagoServiceUrl = "http://gestion-pagos:3000/pagos";

        ResponseEntity<String> reservaResponse = restTemplate.postForEntity(reservaServiceUrl, reservaRequest, String.class);
        String reservaId = reservaResponse.getBody();

        if (reservaId != null && reservaId.startsWith("\"") && reservaId.endsWith("\"")) {
            reservaId = reservaId.substring(1, reservaId.length() - 1);
        }

        if (reservaId == null || reservaId.isEmpty()) {
            return ResponseEntity.status(500).body(Map.of("error", "Error creando la reserva"));
        }

        ResponseEntity<Map> reservaInfoResponse = restTemplate.getForEntity(reservaServiceUrl + reservaId, Map.class);
        Map<String, Object> reservaInfo = reservaInfoResponse.getBody();

        if (reservaInfo == null || !reservaInfo.containsKey("field")) {
            return ResponseEntity.status(500).body(Map.of("error", "No se pudo obtener la información de la reserva"));
        }

        String fieldId = reservaInfo.get("field").toString();

        ResponseEntity<Map> fieldInfoResponse = restTemplate.getForEntity(fieldServiceUrlBase + fieldId, Map.class);
        Map<String, Object> fieldInfo = fieldInfoResponse.getBody();

        if (fieldInfo == null || !fieldInfo.containsKey("hour_price")) {
            return ResponseEntity.status(500).body(Map.of("error", "No se pudo obtener la información del campo"));
        }

        double hourPrice = (double) fieldInfo.get("hour_price");

        Map<String, Object> pagoRequest = new HashMap<>();
        pagoRequest.put("amount", hourPrice);
        pagoRequest.put("status", "PAGADO");
        pagoRequest.put("paymentMethod", "CREDIT_CARD");
        pagoRequest.put("userId", reservaInfo.get("user"));
        pagoRequest.put("reserva", reservaId);

        ResponseEntity<Map> pagoResponse = restTemplate.postForEntity(pagoServiceUrl, pagoRequest, Map.class);

        if (pagoResponse.getStatusCode().is2xxSuccessful()) {
            Map<String, Object> pagoInfo = pagoResponse.getBody();

            Map<String, Object> response = new HashMap<>();
            response.put("reserva", reservaInfo);
            response.put("pago", pagoInfo);

            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(500).body(Map.of("error", "Error creando el pago"));
        }
    }

}