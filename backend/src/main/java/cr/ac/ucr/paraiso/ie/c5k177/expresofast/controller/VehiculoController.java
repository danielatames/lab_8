package cr.ac.ucr.paraiso.ie.c5k177.expresofast.controller;

import cr.ac.ucr.paraiso.ie.c5k177.expresofast.business.VehiculoService;
import cr.ac.ucr.paraiso.ie.c5k177.expresofast.domain.Vehiculo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import cr.ac.ucr.paraiso.ie.c5k177.expresofast.dto.VehiculoResponseDTO;

import java.util.List;

@RestController
@RequestMapping("/api/vehiculos")
@CrossOrigin(origins = "*")
public class VehiculoController {

    private final VehiculoService vehiculoService;

    public VehiculoController(VehiculoService vehiculoService) {
        this.vehiculoService = vehiculoService;
    }

    @GetMapping
    public ResponseEntity<List<VehiculoResponseDTO>> listarTodos() {
        return ResponseEntity.ok(vehiculoService.listarTodosDTO());
    }

    @PostMapping
    public ResponseEntity<Vehiculo> crear(@RequestBody Vehiculo vehiculo) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vehiculoService.crear(vehiculo));
    }
}