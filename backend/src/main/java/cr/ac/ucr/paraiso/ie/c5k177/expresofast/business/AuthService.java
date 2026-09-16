package cr.ac.ucr.paraiso.ie.c5k177.expresofast.business;

import cr.ac.ucr.paraiso.ie.c5k177.expresofast.data.UsuarioRepository;
import cr.ac.ucr.paraiso.ie.c5k177.expresofast.dto.AuthRequestDTO;
import cr.ac.ucr.paraiso.ie.c5k177.expresofast.dto.AuthResponseDTO;
import cr.ac.ucr.paraiso.ie.c5k177.expresofast.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UsuarioRepository usuarioRepository;

    public AuthService(AuthenticationManager authenticationManager,JwtTokenProvider jwtTokenProvider,UsuarioRepository usuarioRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.usuarioRepository = usuarioRepository;
    }

    public AuthResponseDTO login(AuthRequestDTO request) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        String token = jwtTokenProvider.generarToken(authentication);

        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(rol -> rol.startsWith("ROLE_"))
                .collect(Collectors.toList());

        usuarioRepository.findByUsername(request.getUsername()).orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));

        return new AuthResponseDTO(
                token,
                request.getUsername(),
                roles,
                jwtTokenProvider.getExpirationMs()
        );
    }
}