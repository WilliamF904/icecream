package org.esfe.controllers;

import org.esfe.services.implement.PdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;

@RestController
@RequestMapping("/pdf")
public class PdfUsuarioController {

    @Autowired
    private PdfService pdfService;

    // Generar PDF de todos los usuarios
    @GetMapping("/usuarios")
    public ResponseEntity<InputStreamResource> generarPdfUsuarios() {
        ByteArrayInputStream pdfStream = pdfService.crearPdfUsuarios();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=usuarios.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(pdfStream));
    }



    // Generar PDF de usuarios activos
    @GetMapping("/usuarios/activos")
    public ResponseEntity<InputStreamResource> generarPdfUsuariosActivos() {
        ByteArrayInputStream pdfStream = pdfService.crearPdfUsuariosPorEstado(1);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=usuarios_activos.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(pdfStream));
    }

    // Generar PDF de usuarios inactivos
    @GetMapping("/usuarios/inactivos")
    public ResponseEntity<InputStreamResource> generarPdfUsuariosInactivos() {
        ByteArrayInputStream pdfStream = pdfService.crearPdfUsuariosPorEstado(0);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=usuarios_inactivos.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(pdfStream));
    }
}
