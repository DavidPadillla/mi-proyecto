package com.bibli.bia.service;

import com.bibli.bia.Model.ReservaModel;
import com.bibli.bia.repository.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ReservaService {

    @Autowired
    private ReservaRepository reservaRepository;

    // --- Métodos existentes ---
    public ReservaModel crearReserva(ReservaModel reserva) {
        return reservaRepository.save(reserva);
    }

    public ReservaModel obtenerReservaPorId(String id) {
        return reservaRepository.findById(id).orElse(null);
    }

    public List<ReservaModel> obtenerTodasReservas() {
        return reservaRepository.findAll();
    }

    public void eliminarReserva(String id) {
        Optional<ReservaModel> reserva = reservaRepository.findById(id);
        if (reserva.isPresent()) {
            reservaRepository.deleteById(id);
        } else {
            throw new IllegalArgumentException("Reserva no encontrada");
        }
    }



}




