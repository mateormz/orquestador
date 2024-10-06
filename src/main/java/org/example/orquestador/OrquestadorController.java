package org.example.orquestador;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class OrquestadorController {

    @Autowired
    private RestTemplate restTemplate;

    // Endpoint para obtener usuarios
    @GetMapping("/usuarios")
    public ResponseEntity<String> getUsuarios() {
        String url = "http://gestion-usuarios:8081/user";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        return response;
    }

    // Endpoint para obtener canchas
    @GetMapping("/canchas")
    public ResponseEntity<String> getCanchas() {
        String url = "http://gestion-canchas:8082/canchas";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        return response;
    }

    // Endpoint para obtener pagos
    @GetMapping("/pagos")
    public ResponseEntity<String> getPagos() {
        String url = "http://gestion-pagos:3000/pagos";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        return response;
    }

    // Endpoint para obtener reservas
    @GetMapping("/reservas")
    public ResponseEntity<String> getReservas() {
        String url = "http://gestion-reservas:8000/reservas";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        return response;
    }
}