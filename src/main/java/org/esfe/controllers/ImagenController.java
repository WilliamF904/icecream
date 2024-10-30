package org.esfe.controllers;
import org.esfe.execpcion.InvalidFileFormatException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ImagenController {

    private final String IMAGE_DIRECTORY = "src/main/resources/static/img/";
    private final List<String> IMAGENES_PROTEGIDAS = List.of("sorvete.png", "sorvete.ico", "generalproducto.png", "generalusuario.png");

    @GetMapping("/imagenes")
    public String listarImagenes(@RequestParam(defaultValue = "0") int page, Model model) {
        int pageSize = 5;
        List<String> imagenes = obtenerListaDeImagenes();

        // Calcular las imágenes a mostrar en la página actual
        int totalImages = imagenes.size();
        int fromIndex = Math.min(page * pageSize, totalImages);
        int toIndex = Math.min((page + 1) * pageSize, totalImages);
        List<String> imagenesPaginadas = imagenes.subList(fromIndex, toIndex);

        // Cálculo del total de páginas
        int totalPages = (int) Math.ceil((double) totalImages / pageSize);

        model.addAttribute("imagenes", imagenesPaginadas);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);

        return "imagen/index";
    }


    @PostMapping("/imagenes/eliminar")
    public String eliminarImage(String nombreArchivo, RedirectAttributes redirectAttributes) throws IOException {
        if (IMAGENES_PROTEGIDAS.contains(nombreArchivo)) {
            redirectAttributes.addFlashAttribute("error", "No se puede eliminar la imagen protegida: " + nombreArchivo);
        } else {
            eliminarImagen(nombreArchivo);
            redirectAttributes.addFlashAttribute("msg", "Imagen eliminada correctamente: " + nombreArchivo);
        }
        return "redirect:/imagenes";
    }

    @PostMapping("/imagenes/reemplazar")
    public String reemplazarImagen(MultipartFile file, String nombreArchivo, RedirectAttributes redirectAttributes) throws IOException {
        if (IMAGENES_PROTEGIDAS.contains(nombreArchivo)) {
            guardarImagen(file, nombreArchivo);
            redirectAttributes.addFlashAttribute("msg2", "Imagen protegida reemplazada exitosamente: " + nombreArchivo);
        } else {
            eliminarImagen(nombreArchivo);
            guardarImagen(file, nombreArchivo);
            redirectAttributes.addFlashAttribute("msg", "Imagen reemplazada correctamente: " + nombreArchivo);
        }
        return "redirect:/imagenes";
    }

    private List<String> obtenerListaDeImagenes() {
        File dir = new File(IMAGE_DIRECTORY);
        return List.of(dir.listFiles())
                .stream()
                .map(File::getName)
                .collect(Collectors.toList());
    }

    private String guardarImagen(MultipartFile file, String nombreArchivo) throws IOException {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new InvalidFileFormatException("Solo se permiten archivos de imagen.");
        }
        String ruta = Paths.get(IMAGE_DIRECTORY, nombreArchivo).toString();
        Files.copy(file.getInputStream(), Paths.get(ruta), StandardCopyOption.REPLACE_EXISTING);
        return "/img/" + nombreArchivo;
    }

    private void eliminarImagen(String nombreArchivo) throws IOException {
        Files.deleteIfExists(Paths.get(IMAGE_DIRECTORY, nombreArchivo));
    }

    /*private String guardarImagen(MultipartFile file) throws IOException {
        if (file != null && !file.isEmpty()) {
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new InvalidFileFormatException("Solo se permiten archivos de imagen.");
            }

            String nombreArchivo = file.getOriginalFilename();
            String ruta = Paths.get("src/main/resources/static/img", nombreArchivo).toString();
            Files.copy(file.getInputStream(), Paths.get(ruta), StandardCopyOption.REPLACE_EXISTING);
            return "/img/" + nombreArchivo;
        }
        return null;
    }*/




}

