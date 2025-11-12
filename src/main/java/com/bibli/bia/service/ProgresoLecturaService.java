package com.bibli.bia.service;

import com.bibli.bia.Model.ProgresoLectura;
import com.bibli.bia.repository.ProgresoLecturaRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ProgresoLecturaService {

    private final ProgresoLecturaRepository progresoRepository;

    public ProgresoLecturaService(ProgresoLecturaRepository progresoRepository) {
        this.progresoRepository = progresoRepository;
    }

    public ProgresoLectura obtenerProgreso(String username) {
        Optional<ProgresoLectura> progreso = progresoRepository.findByUsername(username);

        if (progreso.isPresent()) {
            return progreso.get();
        } else {
            ProgresoLectura nuevoProgreso = new ProgresoLectura(username);
            return progresoRepository.save(nuevoProgreso);
        }
    }

    public ProgresoLectura guardarCapitulo(String username, String libroId, int capitulo, int totalCapitulos) {
        ProgresoLectura progreso = obtenerProgreso(username);

        if (!progreso.getCapitulosPorLibro().containsKey(libroId)) {
            progreso.getCapitulosPorLibro().put(libroId, new HashSet<>());
        }

        progreso.getCapitulosPorLibro().get(libroId).add(capitulo);

        if (progreso.getCapitulosPorLibro().get(libroId).size() == totalCapitulos) {
            boolean esNuevo = progreso.getLibrosCompletados().add(libroId);
            if (esNuevo) {
                progreso.setTotalLibrosLeidos(progreso.getTotalLibrosLeidos() + 1);
                progreso.setPuntos(progreso.getPuntos() + 100);
            }
        }

        progreso.setUltimaActualizacion(LocalDateTime.now());

        return progresoRepository.save(progreso);
    }

    public ProgresoLectura marcarLibroCompletado(String username, String libroId) {
        ProgresoLectura progreso = obtenerProgreso(username);

        boolean esNuevo = progreso.getLibrosCompletados().add(libroId);

        if (esNuevo) {
            progreso.setTotalLibrosLeidos(progreso.getTotalLibrosLeidos() + 1);
            progreso.setPuntos(progreso.getPuntos() + 100);
        }

        progreso.setUltimaActualizacion(LocalDateTime.now());

        return progresoRepository.save(progreso);
    }

    public boolean haCompletadoLibro(String username, String libroId) {
        ProgresoLectura progreso = obtenerProgreso(username);
        return progreso.getLibrosCompletados().contains(libroId);
    }

    public void reiniciarProgreso(String username) {
        Optional<ProgresoLectura> progreso = progresoRepository.findByUsername(username);
        progreso.ifPresent(progresoRepository::delete);
    }
}
