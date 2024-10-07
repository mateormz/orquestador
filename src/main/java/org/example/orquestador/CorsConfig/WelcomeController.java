package org.example.orquestador.CorsConfig;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class WelcomeController {

    @GetMapping("/")
    public ResponseEntity<String> createComment() {
        String welcome = "welcome to orquestador";
        return ResponseEntity.ok(welcome);
    }

}
