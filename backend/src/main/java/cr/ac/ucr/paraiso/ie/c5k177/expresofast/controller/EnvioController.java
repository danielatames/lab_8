package cr.ac.ucr.paraiso.ie.c5k177.expresofast.controller;

import cr.ac.ucr.paraiso.ie.c5k177.expresofast.business.EnvioService;
import cr.ac.ucr.paraiso.ie.c5k177.expresofast.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/envios")
@CrossOrigin(origins = "*")
public class EnvioController {

    private final EnvioService envioService;

    public EnvioController(EnvioService envioService) {
        this.envioService = envioService;
    }

    @GetMapping("/optimizados")
    public ResponseEntity<List<EnvioResponseDTO>> listarOptimizado() {
        return ResponseEntity.ok(envioService.listarOptimizado());
    }

    @PostMapping
    public ResponseEntity<EnvioResponseDTO> registrarEnvio(@Valid @RequestBody EnvioRequestDTO dto) {
        EnvioResponseDTO guardado = envioService.registrarEnvio(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<EnvioResponseDTO> actualizarEstado(@PathVariable Integer id,
            @Valid @RequestBody CambioEstadoDTO dto) {
        EnvioResponseDTO actualizado = envioService.actualizarEstado(id, dto);
        return ResponseEntity.ok(actualizado);
    }

    @GetMapping("/{id}/bitacora")
    public ResponseEntity<List<BitacoraResponseDTO>> obtenerBitacora(@PathVariable Integer id) {
        return ResponseEntity.ok(envioService.obtenerBitacora(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EnvioResponseDTO> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(envioService.obtenerPorId(id));
    }
}