package cr.ac.ucr.paraiso.ie.c5k177.expresofast.business;

import cr.ac.ucr.paraiso.ie.c5k177.expresofast.data.*;
import cr.ac.ucr.paraiso.ie.c5k177.expresofast.domain.*;
import cr.ac.ucr.paraiso.ie.c5k177.expresofast.dto.*;
import cr.ac.ucr.paraiso.ie.c5k177.expresofast.exception.CapacidadExcedidaException;
import cr.ac.ucr.paraiso.ie.c5k177.expresofast.exception.InvalidStateTransitionException;
import cr.ac.ucr.paraiso.ie.c5k177.expresofast.exception.ResourceNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EnvioService {

    private final EnvioRepository envioRepository;
    private final VehiculoRepository vehiculoRepository;
    private final ConductorRepository conductorRepository;
    private final UsuarioRepository usuarioRepository;
    private final BitacoraEnvioRepository bitacoraEnvioRepository;

    private static final Set<String> ESTADOS_FINALES = Set.of("ENTREGADO", "CANCELADO");
    private static final Set<String> ESTADOS_RETROCESO = Set.of("PENDIENTE", "EN_TRANSITO");

    public EnvioService(EnvioRepository envioRepository,
            VehiculoRepository vehiculoRepository,
            ConductorRepository conductorRepository,
            UsuarioRepository usuarioRepository,
            BitacoraEnvioRepository bitacoraEnvioRepository) {
        this.envioRepository = envioRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.conductorRepository = conductorRepository;
        this.usuarioRepository = usuarioRepository;
        this.bitacoraEnvioRepository = bitacoraEnvioRepository;
    }

    @Transactional(readOnly = true)
    public List<EnvioResponseDTO> listarOptimizado() {
        return envioRepository.findAllOptimizado().stream().map(this::mapearAResponseDTO) .collect(Collectors.toList());
    }

    @Transactional
    public EnvioResponseDTO registrarEnvio(EnvioRequestDTO dto) {
        Vehiculo vehiculo = vehiculoRepository.findById(dto.getVehiculoId()).orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado"));

        Conductor conductor = conductorRepository.findById(dto.getConductorId()).orElseThrow(() -> new ResourceNotFoundException("Conductor no encontrado"));

        if (dto.getPesoKg().compareTo(vehiculo.getCapacidadKg()) > 0) {
            throw new CapacidadExcedidaException( "El peso del envío (" + dto.getPesoKg() + " kg) supera la capacidad del vehículo (" + vehiculo.getCapacidadKg() + " kg)");
        }

        Envio envio = new Envio();
        envio.setCodigoRastreo(dto.getCodigoRastreo());
        envio.setDireccionDestino(dto.getDireccionDestino());
        envio.setPesoKg(dto.getPesoKg());
        envio.setCosto(dto.getCosto());
        envio.setVehiculo(vehiculo);
        envio.setConductor(conductor);
        envio.setEstadoEnvio("PENDIENTE");

        Envio guardado = envioRepository.save(envio);
        return mapearAResponseDTO(guardado);
    }

    @Transactional
    public EnvioResponseDTO actualizarEstado(Integer envioId, CambioEstadoDTO dto) {
        Envio envio = envioRepository.findById(envioId) .orElseThrow(() -> new ResourceNotFoundException("Envío no encontrado"));

        String estadoAnterior = envio.getEstadoEnvio();
        String estadoNuevo = dto.getNuevoEstado();

        //no se puede retroceder desde un estado final
        if (ESTADOS_FINALES.contains(estadoAnterior) && ESTADOS_RETROCESO.contains(estadoNuevo)) {
            throw new InvalidStateTransitionException("Transición de estado no permitida para el envío " + envio.getCodigoRastreo());
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByUsername(username) .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        envio.setEstadoEnvio(estadoNuevo);

        //registro automático en la bitácora
        BitacoraEnvio bitacora = new BitacoraEnvio();
        bitacora.setEnvio(envio);
        bitacora.setEstadoAnterior(estadoAnterior);
        bitacora.setEstadoNuevo(estadoNuevo);
        bitacora.setFechaCambio(LocalDateTime.now());
        bitacora.setUsuario(usuario);
        bitacora.setObservaciones(dto.getObservaciones());
        bitacoraEnvioRepository.save(bitacora);

        return mapearAResponseDTO(envio);
    }

    @Transactional(readOnly = true)
    public List<BitacoraResponseDTO> obtenerBitacora(Integer envioId) {
        if (!envioRepository.existsById(envioId)) {
            throw new ResourceNotFoundException("Envío no encontrado");
        }

        return bitacoraEnvioRepository.findByEnvioIdOrderByFechaCambioDesc(envioId).stream()
                .map(b -> new BitacoraResponseDTO(
                        b.getId(),
                        b.getEstadoAnterior(),
                        b.getEstadoNuevo(),
                        b.getFechaCambio(),
                        b.getUsuario().getNombreCompleto(),
                        b.getObservaciones()))
                .collect(Collectors.toList());
    }

    private EnvioResponseDTO mapearAResponseDTO(Envio envio) {
        return new EnvioResponseDTO(
                envio.getId(),
                envio.getCodigoRastreo(),
                envio.getDireccionDestino(),
                envio.getPesoKg(),
                envio.getCosto(),
                envio.getEstadoEnvio(),
                envio.getVehiculo().getPlaca(),
                envio.getConductor().getNombre() + " " + envio.getConductor().getApellidos(),
                envio.getFechaCreacion(),
                envio.getFechaModificacion());
    }
}