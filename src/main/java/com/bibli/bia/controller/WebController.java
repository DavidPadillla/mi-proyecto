package com.bibli.bia.controller;

import com.bibli.bia.Model.*;
import com.bibli.bia.repository.LibroRepository;
import com.bibli.bia.repository.ReservaRepository;
import com.bibli.bia.repository.RespuestaDashboardRepository;
import com.bibli.bia.repository.UsuarioRepository;
import com.bibli.bia.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/api")
public class WebController {

    @Autowired
    private ResenaService resenaService;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private LibroService libroService;

    @Autowired
    private RespuestaDashboardRepository respuestaRepository;

    @Autowired
    private LibroFisicoService libroFisicoService;
    @Autowired
    private RespuestaDashboardService respuestaService;

    @Autowired
    private UsuarioRepository usuariorepository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private RespuestaDashboardService respuestaDashboardService;

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private MultaService multaService;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private LibroRepository libroRepository;

    @GetMapping("/")
    public String rootRedirect() {
        // Redirige automáticamente al intro
        return "redirect:/intro";
    }

    @GetMapping("/intro")
    public String introPage() {
        // Carga intro.html desde templates
        return "intro";
    }

    @GetMapping("/index")
    public String homePage(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth.getPrincipal() instanceof String)) {
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            boolean isUser = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_USER"));

            if (isAdmin) {
                return "redirect:/api/admin";
            } else if (isUser) {
                return "redirect:/api/logiado";
            }
        }
        return "index"; // no autenticado
    }


    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            Model model) {
        if (error != null) model.addAttribute("error", "Usuario o contraseña incorrectos");
        if (logout != null) model.addAttribute("message", "Sesión cerrada correctamente");
        return "login";
    }
    @GetMapping("/cargarLibrosPorCategoria")
    @ResponseBody
    public List<LibroFisicoModel> cargarLibrosPorCategoria(@RequestParam String categoria) {
        return libroFisicoService.obtenerLibrosFisicosPorCategoria(categoria);
    }


    @PostMapping("/register")
    public String procesarRegistro(@RequestParam String username,
                                   @RequestParam String password,
                                   RedirectAttributes redirectAttributes) {
        if (usuarioRepository.findByUsername(username).isPresent()) {
            redirectAttributes.addAttribute("error", "El nombre de usuario ya está en uso");
            return "redirect:/api/register";
        }
        if (password.length() < 6) {
            redirectAttributes.addAttribute("error", "La contraseña debe tener al menos 6 caracteres");
            return "redirect:/api/register";
        }

        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setUsername(username);
        nuevoUsuario.setPassword(passwordEncoder.encode(password));
        nuevoUsuario.setRoles(Set.of("USER"));
        usuarioRepository.save(nuevoUsuario);

        redirectAttributes.addAttribute("success", true);
        return "redirect:/api/login";
    }


    @GetMapping("/logiado")
    public String logiado(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        model.addAttribute("username", auth.getName());
        return "logiado";
    }

    @GetMapping("/admin")
    public String adminPage() {
        return "admin";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @GetMapping("/mensajeRegistro")
    public String mensajeRegistro() {
        return "mensajeRegistro";
    }

    @GetMapping("/libroVirtualesLog")
    public String listarLibros(Model model) {
        model.addAttribute("libros", libroService.obtenerTodosLosLibros());
        model.addAttribute("librosNovela", libroService.obtenerLibrosPorCategoria("Novelas"));
        model.addAttribute("librosCiencia", libroService.obtenerLibrosPorCategoria("Ciencia"));
        model.addAttribute("librosHistoria", libroService.obtenerLibrosPorCategoria("Historia"));
        model.addAttribute("librosArte", libroService.obtenerLibrosPorCategoria("Arte"));
        model.addAttribute("librosFantasia", libroService.obtenerLibrosPorCategoria("Fantasia")); // Sin tilde
        model.addAttribute("librosFilosofia", libroService.obtenerLibrosPorCategoria("Filosofia")); // Sin tilde
        return "libroVirtualesLog";
    }

    @GetMapping("/reservaLibro")
    public String reservaLibroUsuario(@RequestParam(value = "categoria", required = false) String categoria, Model model) {
        model.addAttribute("categorias", List.of("Novelas", "Ciencia", "Historia", "Arte", "Fantasía", "Filosofía"));
        if (categoria != null) {
            model.addAttribute("librosPorCategoria", libroFisicoService.obtenerLibrosFisicosPorCategoria(categoria));
            model.addAttribute("categoriaSeleccionada", categoria);
        }
        return "reservaLibro";
    }

    @PostMapping("/reservar/{id}")
    public String reservarLibroFisico(@PathVariable String id, Model model) {
        try {
            boolean reservado = libroFisicoService.reservarLibroFisico(id);

            if (reservado) {
                model.addAttribute("mensajeExito", "✅ ¡Reserva exitosa! El stock se ha actualizado correctamente.");
            } else {
                model.addAttribute("mensajeError", "⚠️ No hay stock disponible o el libro no existe.");
            }

        } catch (Exception e) {
            System.err.println("❌ Error al reservar libro físico: " + e.getMessage());
            model.addAttribute("mensajeError", "❌ Ocurrió un error al procesar la reserva.");
        }

        // 🔄 Recargar lista de libros actualizada desde MongoDB
        model.addAttribute("librosFisicos", libroFisicoService.obtenerTodosLosLibrosFisicos());
        return "reservaLibro"; // nombre del template Thymeleaf
    }


    @PostMapping("/cancelar-reserva/{id}")
    public String cancelarReservaLibroFisico(@PathVariable String id, Model model) {
        try {
            boolean cancelado = libroFisicoService.cancelarReservaLibroFisico(id);

            if (cancelado) {
                model.addAttribute("mensaje", "✅ Reserva cancelada. El stock se ha restablecido.");
            } else {
                model.addAttribute("mensaje", "⚠️ No hay reservas para cancelar o el libro no existe.");
            }
        } catch (Exception e) {
            model.addAttribute("mensaje", "❌ Error al cancelar la reserva: " + e.getMessage());
        }

        // Recargar lista actualizada
        model.addAttribute("librosFisicos", libroFisicoService.obtenerTodosLosLibrosFisicos());
        return "reservaLibro";
    }




    @GetMapping("/carrito")
    public String carrito() {
        return "carrito";
    }

    @GetMapping("/inicio")
    public String inicio() {
        return "inicio";
    }
    @GetMapping("/libros")
    public String libros() {
        return "libros";
    }

    @GetMapping("/historia_pensamiento")
    public String historia_pensamiento() {
        return "historia_pensamiento";
    }
    @GetMapping("/desafio_principito")
    public String desafio_principito() {
        return "desafio_principito";
    }
    @GetMapping("/desafio_harrypotter")
    public String desafio_harrypotter() {
        return "desafio_harrypotter";
    }
    @GetMapping("/desafio_1984")
    public String desafio_1984() {
        return "desafio_1984";
    }

    @GetMapping("/librosPorCategoria/{categoria}")
    public String mostrarLibrosPorCategoria(@PathVariable String categoria, Model model) {
        List<LibroModel> libros = libroService.obtenerLibrosPorCategoria(categoria);
        model.addAttribute("libros", libros);
        model.addAttribute("categoria", categoria);
        return "librosPorCategoria";
    }
    @GetMapping("/devolucion")
    public String libroDevolucion(Model model) {
        List<ReservaModel> reservas = reservaService.obtenerTodasReservas();
        model.addAttribute("reservas", reservas);
        return "devolucion";
    }


    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }

    @GetMapping("/pago")
    public String pagoPage() {
        return "pago";
    }


    @GetMapping("/conocerte")
    public String conocertePage() {
        return "conocerte";
    }


    @GetMapping("/reservaAdmin")
    public String reserva(@RequestParam(value = "categoria", required = false) String categoria, Model model) {
        model.addAttribute("categorias", List.of("Novelas", "Ciencia", "Historia", "Arte", "Fantasía", "Filosofía"));
        if (categoria != null) {
            model.addAttribute("librosPorCategoria", libroFisicoService.obtenerLibrosFisicosPorCategoria(categoria));
            model.addAttribute("categoriaSeleccionada", categoria);
        }
        return "reservaAdmin";
    }

    @GetMapping("/noAdminConfig")
    public String noAdminConfig() {
        return "noAdminConfig";
    }

    @GetMapping("/mensajeReserva")
    public String mensaje() {
        return "mensajeReserva";
    }

    @GetMapping("/no")
    public String nou() {
        return "no";
    }

    @GetMapping("/addBook")
    public String addBook() {
        return "addBook";
    }


    @GetMapping("/confirmacionLibro")
    public String confirmacionLibro() {
        return "confirmacionLibro";
    }

    @PostMapping("/agregarLibro")
    @ResponseBody
    public ResponseEntity<?> agregarLibro(@RequestParam String titulo,
                                          @RequestParam String url,
                                          @RequestParam String autor,
                                          @RequestParam String descripcion,
                                          @RequestParam String categoria) {
        try {

            LibroModel libro = new LibroModel();
            libro.setTitulo(titulo);
            libro.setUrl(url);
            libro.setAutor(autor);
            libro.setDescripcion(descripcion);
            libro.setCategoria(categoria);
            libroService.guardarLibro(libro);


            return ResponseEntity.ok().body("Libro agregado exitosamente");
        } catch (Exception e) {

            return ResponseEntity.status(500).body("Hubo un error al agregar el libro");
        }
    }
    @PostMapping("/agregarLibroF")
    @ResponseBody
    public ResponseEntity<?> agregarLibroF(@RequestBody LibroFisicoModel libroFisico) {
        try {

            libroFisicoService.guardarLibroFisico(libroFisico);


            return ResponseEntity.ok().body("Libro agregado exitosamente");
        } catch (Exception e) {

            return ResponseEntity.status(500).body("Hubo un error al agregar el libro");
        }
    }


    @GetMapping("/multas")
    public String multasAdmin(@RequestParam(value = "nombre", required = false) String nombre, Model model) {
        List<MultaModel> multas;

        if (nombre != null && !nombre.isEmpty()) {
            multas = multaService.obtenerMultasPorNombre(nombre);
        } else {
            multas = multaService.obtenerTodasMultas();
        }

        model.addAttribute("multas", multas);
        return "multas";
    }


    @GetMapping("/usuariosAdmin")
    public String usuariosAdmin(Model model) {
        List<Usuario> usuarios = usuarioService.obtenerTodosLosUsuarios();
        model.addAttribute("usuarios", usuarios);
        return "usuariosAdmin";
    }

    @GetMapping("/librosVirtuales")
    public String librosVirtuales() {
        return "librosVirtuales";
    }

    @GetMapping("/addMulta")
    public String add() {
        return "addMulta";
    }

    @GetMapping("/resenasAdmin")
    public String resena(Model model) {
        List<ResenaModel> resenas = resenaService.obtenerTodasLasResenas();
        model.addAttribute("resenas", resenas);
        return "resenasAdmin";
    }

    @GetMapping("/mensajeReservaUsuario")
    public String mensajeReservaUsuario() {
        return "mensajeReservaUsuario";
    }
    @GetMapping("/addLibroF")
    public String addLibroF() {
        return "addLibroF";
    }

    @PostMapping("/guardarReservaUsuario")
    public String guardarReservaUsuario(@RequestParam String idUsuario,
                                        @RequestParam String nombreCompleto,
                                        @RequestParam String correo,
                                        @RequestParam String categoria,
                                        @RequestParam String libro,
                                        @RequestParam String fecha,
                                        RedirectAttributes redirectAttributes) {
        try {
            ReservaModel reserva = new ReservaModel(
                    idUsuario, nombreCompleto, correo, categoria, libro, LocalDate.parse(fecha)
            );
            reservaService.crearReserva(reserva);
            redirectAttributes.addFlashAttribute("mensaje", "Reserva realizada con éxito.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al realizar la reserva. Inténtalo nuevamente.");
        }
        return "redirect:/api/mensajeReservaUsuario";
    }


    @PostMapping("/guardarRespuestaDashboard")
    public String guardarRespuestaDashboard(@ModelAttribute RespuestaDashboard respuesta) {
        respuestaService.guardarRespuesta(respuesta);
        return "redirect:/api/logiado";
    }

    @PostMapping("/guardarResena")
    public String guardarResena(@RequestParam String nombre,
                                @RequestParam String comentario,
                                RedirectAttributes redirectAttributes) {
        try {
            ResenaModel resena = new ResenaModel(nombre, comentario);
            resenaService.guardarResena(resena);
            redirectAttributes.addFlashAttribute("mensaje", "¡Gracias por tu reseña!");
            return "redirect:/api/logiado";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al guardar la reseña");
            return "redirect:/api/logiado";
        }
    }


    @PostMapping("/procesar-devolucion")
    public String procesarDevolucion(
            @RequestParam String reservaId,
            @RequestParam(required = false) Integer diasRetraso,
            @RequestParam(required = false) Double valorMulta,
            @RequestParam String fechaDevolucion,
            RedirectAttributes redirectAttributes) {


        ReservaModel reserva = reservaService.obtenerReservaPorId(reservaId);

        if (reserva == null) {
            redirectAttributes.addFlashAttribute("error", "Reserva no encontrada");
            return "redirect:/api/devolucion";
        }


        LocalDate fechaDev = LocalDate.parse(fechaDevolucion);


        if (diasRetraso != null && diasRetraso > 0 && valorMulta != null) {
            MultaModel multa = new MultaModel(
                    reserva.getIdUsuario(),
                    reserva.getNombreCompleto(),
                    reserva.getLibro(),
                    reserva.getFecha(),
                    fechaDev,
                    diasRetraso,
                    valorMulta
            );
            multaService.crearMulta(multa);

            redirectAttributes.addFlashAttribute("mensaje",
                    "Devolución registrada con multa de $" + valorMulta + " COP");
        } else {
            redirectAttributes.addFlashAttribute("mensaje",
                    "Devolución registrada sin multa");
        }


        reservaService.eliminarReserva(reservaId);

        return "redirect:/api/devolucion";
    }
    @GetMapping
    public String listarMultas(@RequestParam(value = "nombre", required = false) String nombre, Model model) {
        List<MultaModel> multas = (nombre != null && !nombre.isEmpty())
                ? multaService.obtenerMultasPorNombre(nombre)
                : multaService.obtenerTodasMultas();
        model.addAttribute("multas", multas);
        return "multas";
    }

    @PostMapping("/multas/pagar/{id}")
    public ResponseEntity<?> pagarMulta(@PathVariable String id) {
        MultaModel multa = multaService.marcarComoPagada(id);
        if (multa != null) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/multas/{id}")
    public ResponseEntity<Void> eliminarMulta(@PathVariable String id) {
        multaService.eliminarMulta(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/api/cargarTodosLosLibros")
    @ResponseBody
    public List<LibroModel> cargarTodosLosLibros() {
        return libroRepository.findAll();
    }

}










