package cr.ac.ucr.paraiso.ie.c5k177.expresofast.business;

import cr.ac.ucr.paraiso.ie.c5k177.expresofast.data.EmpresaLogisticaRepository;
import cr.ac.ucr.paraiso.ie.c5k177.expresofast.domain.EmpresaLogistica;
import cr.ac.ucr.paraiso.ie.c5k177.expresofast.exception.DuplicateResourceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EmpresaLogisticaService {

    private final EmpresaLogisticaRepository empresaRepository;

    public EmpresaLogisticaService(EmpresaLogisticaRepository empresaRepository) {
        this.empresaRepository = empresaRepository;
    }

    @Transactional(readOnly = true)
    public List<EmpresaLogistica> listarTodas() {
        return empresaRepository.findAll();
    }

    @Transactional
    public EmpresaLogistica crear(EmpresaLogistica empresa) {
        if (empresaRepository.existsByCedulaJuridica(empresa.getCedulaJuridica())) {
            throw new DuplicateResourceException(
                "Ya existe una empresa registrada con la cedula juridica " + empresa.getCedulaJuridica()
            );
        }
        return empresaRepository.save(empresa);
    }
}