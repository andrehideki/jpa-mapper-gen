package br.com.mapper_gen;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api")
public class ApiController {
    
    @Autowired
    private DatabaseService databaseService;

    @GetMapping
    public ResponseEntity<?> get(
        @RequestParam(defaultValue = "") String name
    ) {
        return ResponseEntity.ok(databaseService.getDatabaseTables(name));
    }

    @GetMapping("views")
    public ResponseEntity<?> getViews(
        @RequestParam(defaultValue = "") String name
    ) {
        return ResponseEntity.ok(databaseService.getDatabaseViews(name));
    }
}
