package com.bibli.bia.service;

import com.bibli.bia.Model.LibroFisicoModel;
import com.bibli.bia.repository.LibroFisicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LibroFisicoService {

    @Autowired
    private LibroFisicoRepository libroFisicoRepository;

    // ✅ Guardar o actualizar libro físico
    public LibroFisicoModel guardarLibroFisico(LibroFisicoModel libroFisico) {
        // Evitar nulos en stock o reservado
        if (libroFisico.getStock() < 0) libroFisico.setStock(0);
        if (libroFisico.getReservado() < 0) libroFisico.setReservado(0);
        return libroFisicoRepository.save(libroFisico);
    }

    // ✅ Obtener todos los libros físicos
    public List<LibroFisicoModel> obtenerTodosLosLibrosFisicos() {
        return libroFisicoRepository.findAll();
    }

    // ✅ Obtener libros físicos por categoría
    public List<LibroFisicoModel> obtenerLibrosFisicosPorCategoria(String categoria) {
        return libroFisicoRepository.findByCategoria(categoria);
    }

    // ✅ Reservar un libro físico (disminuye stock y aumenta reservas)
    public boolean reservarLibroFisico(String id) {
        Optional<LibroFisicoModel> libroOptional = libroFisicoRepository.findById(id);

        if (libroOptional.isEmpty()) {
            System.err.println("❌ Libro no encontrado con ID: " + id);
            return false;
        }

        LibroFisicoModel libro = libroOptional.get();

        if (libro.getStock() <= 0) {
            System.out.println("⚠️ No hay stock disponible para el libro: " + libro.getTitulo());
            return false;
        }

        // 🔹 Actualizar valores
        libro.setStock(libro.getStock() - 1);
        libro.setReservado(libro.getReservado() + 1);

        // 🔹 Guardar en MongoDB
        libroFisicoRepository.save(libro);

        // 🔹 Confirmación en consola
        System.out.printf("✅ Reserva realizada: %s | Nuevo stock: %d | Reservado: %d%n",
                libro.getTitulo(), libro.getStock(), libro.getReservado());

        return true;
    }

    // ✅ Cancelar reserva (devuelve stock y reduce reservas)
    public boolean cancelarReservaLibroFisico(String id) {
        Optional<LibroFisicoModel> libroOptional = libroFisicoRepository.findById(id);

        if (libroOptional.isEmpty()) {
            System.err.println("❌ Libro no encontrado con ID: " + id);
            return false;
        }

        LibroFisicoModel libro = libroOptional.get();

        if (libro.getReservado() <= 0) {
            System.out.println("⚠️ No hay reservas para cancelar en el libro: " + libro.getTitulo());
            return false;
        }

        // 🔹 Actualizar valores
        libro.setStock(libro.getStock() + 1);
        libro.setReservado(libro.getReservado() - 1);

        // 🔹 Guardar en MongoDB
        libroFisicoRepository.save(libro);

        // 🔹 Confirmación en consola
        System.out.printf("🔁 Reserva cancelada: %s | Nuevo stock: %d | Reservado: %d%n",
                libro.getTitulo(), libro.getStock(), libro.getReservado());

        return true;
    }
}


