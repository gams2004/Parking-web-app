package co.edu.javeriana.parkingApp.controller;

import java.util.List;
import java.util.Optional;

import org.apache.catalina.authenticator.SpnegoAuthenticator.AuthenticateAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import co.edu.javeriana.parkingApp.model.UserEntity;
import co.edu.javeriana.parkingApp.repository.UserRepository;
import co.edu.javeriana.parkingApp.security.JWTGenerator;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/usuario")
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JWTGenerator jwtGenerator;

    @Autowired
    PasswordEncoder passwordEncoder;

    
    @CrossOrigin
    @GetMapping("/find/correo/{correo}")
    @Operation(summary = "Obtener un usuario por correo")
    public ResponseEntity<Optional<UserEntity>> findByCorreo(@PathVariable String correo) {
        Optional<UserEntity> usuarioEncontrada = userRepository.findByCorreo(correo);
        return ResponseEntity.ok(usuarioEncontrada);
    }

    
    @PostMapping("/login")
    public ResponseEntity loginUser(@RequestBody UserEntity user) {
        Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(user.getCorreo(), user.getContrasenia())
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        String token = jwtGenerator.generateToken(auth);
        return new ResponseEntity<String>(token, HttpStatus.OK);
    }

    @Operation(summary = "Agregar un Usuario")
    @PostMapping("/add")
    public ResponseEntity agregar(@RequestBody UserEntity user) {
        
        if (user == null) {
            ResponseEntity<UserEntity> response = new ResponseEntity<>(user, HttpStatus.BAD_REQUEST);
            return response;
        }
        else if (userRepository.findByCorreo(user.getCorreo()).isPresent()) {
            ResponseEntity<String> response = new ResponseEntity<String>("user already exists", HttpStatus.BAD_REQUEST);
            return response;
        }
        user.setContrasenia(passwordEncoder.encode(user.getContrasenia()));
        userRepository.save(user);
        ResponseEntity<UserEntity> response = new ResponseEntity<>(user, HttpStatus.CREATED);
        return response;
    }

    @GetMapping("/all")
    @Operation(summary = "Obtener todos los usuarios")
    public ResponseEntity<List<UserEntity>> getAll() {
        List<UserEntity> usuarios = userRepository.findAll();

        ResponseEntity<List<UserEntity>> response = new ResponseEntity<>(usuarios, HttpStatus.OK);
        return response;
    }
    
    @PutMapping("/modificar")
    public ResponseEntity<UserEntity> modificar (@RequestBody UserEntity user)
    {
        Optional<UserEntity> usuarioEncontrada = userRepository.findById(user.getId());
        UserEntity Final=usuarioEncontrada.get();
        Final.setNombre(user.getNombre());
        Final.setCorreo(user.getCorreo());
        Final.setContrasenia(user.getContrasenia());
        Final.setRol(user.getRol());
        userRepository.save(Final);
        return ResponseEntity.ok(Final);
    }

@GetMapping("/details")
public ResponseEntity<UserEntity> buscarUser() {
    // Obtener el correo del usuario autenticado
    String correo = SecurityContextHolder.getContext().getAuthentication().getName();

    // Buscar el usuario por correo
    Optional<UserEntity> userOptional = userRepository.findByCorreo(correo);

    // Verificar si el Optional contiene un valor (usuario)
    if (!userOptional.isPresent()) {
        // Si no se encuentra el usuario, devuelve una respuesta con HttpStatus.NOT_FOUND
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Si se encuentra el usuario, extraer el valor de Optional y devolverlo en la respuesta
    UserEntity user = userOptional.get();
    return new ResponseEntity<>(user, HttpStatus.OK);
}

}