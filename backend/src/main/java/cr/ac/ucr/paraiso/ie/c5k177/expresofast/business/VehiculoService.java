package cr.ac.ucr.paraiso.ie.c5k177.expresofast.business;

import cr.ac.ucr.paraiso.ie.c5k177.expresofast.data.ConductorRepository;
import cr.ac.ucr.paraiso.ie.c5k177.expresofast.data.VehiculoRepository;
import cr.ac.ucr.paraiso.ie.c5k177.expresofast.domain.Conductor;
import cr.ac.ucr.paraiso.ie.c5k177.expresofast.domain.Vehiculo;
import cr.ac.ucr.paraiso.ie.c5k177.expresofast.exception.DuplicateResourceException;
import cr.ac.ucr.paraiso.ie.c5k177.expresofast.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VehiculoService {

    private final VehiculoRepository vehiculoRepository;
    private final ConductorRepository conductorRepository;

    public VehiculoService(VehiculoRepository vehiculoRepository,ConductorRepository conductorRepository) {
        this.vehiculoRepository = vehiculoRepository;
        this.conductorRepository = conductorRepository;
    }

    @Transactional(readOnly = true)
    public List<Vehiculo> listarTodos() {
        return vehiculoRepository.findAll();
    }

    @Transactional
    public Vehiculo registrarVehiculo(Vehiculo vehiculo) {
        if (vehiculoRepository.existsByPlaca(vehiculo.getPlaca())) {
            throw new DuplicateResourceException(
                "Ya existe un vehiculo registrado con la placa " + vehiculo.getPlaca()
            );
        }
        return vehiculoRepository.save(vehiculo);
    }

    @Transactional
    public Vehiculo asignarConductor(Integer vehiculoId, Integer conductorId) {
        Vehiculo vehiculo = vehiculoRepository.findById(vehiculoId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehiculo no encontrado"));

        Conductor conductor = conductorRepository.findById(conductorId)
                .orElseThrow(() -> new ResourceNotFoundException("Conductor no encontrado"));

        if (Boolean.FALSE.equals(conductor.getActivo())) {
            throw new IllegalStateException(
                "No se puede asignar el conductor " + conductor.getNombre()
                + " porque se encuentra inactivo"
            );
        }
        return vehiculo;
    }

    @Transactional
    public Vehiculo crear(Vehiculo vehiculo) {
        return registrarVehiculo(vehiculo);
    }
}