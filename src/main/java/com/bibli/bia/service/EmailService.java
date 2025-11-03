package com.bibli.bia.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    public void enviarCorreoConfirmacion(String correo, String nombre, String libro, String fecha) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(correo);
        mensaje.setSubject("Confirmación de reserva - BookLab");
        mensaje.setText("Hola " + nombre + ",\n\n"
                + "Tu reserva del libro \"" + libro + "\" para la fecha " + fecha + " ha sido realizada con éxito.\n\n"
                + "Gracias por usar BookLab Biblioteca.");
        mailSender.send(mensaje);
    }
}

