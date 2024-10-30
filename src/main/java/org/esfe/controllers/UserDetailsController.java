package org.esfe.controllers;

import org.esfe.services.implement.PdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/usuarios")
public class UserDetailsController {

    @Autowired
    private PdfService pdfService;

    @GetMapping("/pdf/{id}")
    public ResponseEntity<byte[]> generarPdfUsuario(@PathVariable("id") Integer userId) {
        try {
            // Generar el contenido del PDF como byte[]
            byte[] pdfContent = pdfService.generatePdfReport(userId);

            if (pdfContent == null || pdfContent.length == 0) {
                throw new RuntimeException("El contenido del PDF está vacío o nulo.");
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "usuario_" + userId + ".pdf");

            return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

}
