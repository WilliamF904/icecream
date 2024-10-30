package org.esfe.controllers;

import jakarta.validation.Valid;
import org.esfe.execpcion.InvalidFileFormatException;
import org.esfe.execpcion.RolNotFoundException;
import org.esfe.execpcion.UserNotFoundException;
import org.esfe.models.Product;
import org.esfe.models.User;
import org.esfe.services.interfaces.IPayService;
import org.esfe.services.interfaces.IRolService;
import org.esfe.services.interfaces.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/users")
public class UserController {
    @Autowired
    private IPayService payService;

    @Autowired
    private IUserService userService;

    @Autowired
    private IRolService rolService;


    @GetMapping
    public String index(Model model,
                        @RequestParam("page") Optional<Integer> page,
                        @RequestParam("size") Optional<Integer> size,
                        @RequestParam(value = "name", required = false) String name,
                        @RequestParam(value = "lastname", required = false) String lastname,
                        @RequestParam(value = "rolId", required = false) Integer rolId,
                        @RequestParam(value = "status", required = false) Integer status
                        ){

        model.addAttribute("roles", rolService.findAll());
        model.addAttribute("user", new User());
        int currentPage = page.orElse(1) - 1;
        int pageSize = size.orElse(5);
        Pageable pageable = PageRequest.of(currentPage, pageSize, Sort.by(Sort.Order.desc("id")));



        // Lógica de filtrado
        Page<User> users;
        if (name != null || lastname != null || rolId != null || status != null) {
            users = userService.searchUsers(name, lastname, rolId, status, pageable);
        } else {
            users = userService.findAll(pageable);
        }

        model.addAttribute("users", users);

        int totalPages = users.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .boxed()
                    .collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }

        return "user/index";
    }



    // Método para mostrar el formulario de creación de usuario
    @GetMapping("/create")
    public String create(User user, Model model) {
        model.addAttribute("roles", rolService.findAll());
        return "user/create";
    }

    private String guardarImagen(MultipartFile file) throws IOException {
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
    }





    @PostMapping("/save")
    public String save(@Valid User user, BindingResult result, Model model,
                       @RequestParam("urlImage") MultipartFile file,  // Captura el archivo
                       RedirectAttributes attributes) {
        try {
            // Verifica si hay errores de validación en el formulario
            if (result.hasErrors()) {
                model.addAttribute("roles", rolService.findAll());
                attributes.addFlashAttribute("error", "No se pudo guardar debido a un error.");
                return "user/create";
            }

            // Maneja la subida de la imagen
            if (file != null && !file.isEmpty()) {
                String url = guardarImagen(file);  // Guarda la imagen y obtiene la URL
                user.setUrl(url);  // Asigna la URL al campo 'url' del objeto User
            }

            // Guarda el usuario
            userService.createOrEditOne(user);
            attributes.addFlashAttribute("msg", "Usuario creado correctamente");
            return "redirect:/users";

        } catch (IOException e) {
            attributes.addFlashAttribute("error", "Error al subir la imagen: " + e.getMessage());
            return "redirect:/users";
        }
    }

    @GetMapping("/details/{id}")
    public String details(@PathVariable("id") Integer id, Model model, RedirectAttributes attributes){
        User user = userService.findOneById(id).orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));
        model.addAttribute("user", user);
        return "user/details";
    }


    // Método para mostrar el formulario de edición de usuario
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Integer id, Model model, RedirectAttributes attributes) {
        User user = userService.findOneById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));
        model.addAttribute("user", user);
        model.addAttribute("roles", rolService.findAll());
        return "user/edit";
    }

    @GetMapping("/remove/{id}")
    public String remove(@PathVariable("id") Integer id, Model model){
        User user = userService.findOneById(id).orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));
        model.addAttribute("user", user);
        return "user/delete";
    }

    @PostMapping("/delete")
    public String delete(User user, RedirectAttributes attributes) {
        try {
            // Verificar si el usuario tiene pagos asociados
            if (!user.getPays().isEmpty()) {
                attributes.addFlashAttribute("error", "No se puede eliminar el usuario porque tiene pagos asociados.");
                return "redirect:/users";
            }

            // Si no tiene pagos asociados, se procede a eliminar
            userService.deleteOneById(user.getId());
            attributes.addFlashAttribute("msg2", "Usuario eliminado correctamente");

        } catch (DataIntegrityViolationException e) {
            attributes.addFlashAttribute("error", "No se puede eliminar el usuario debido a una violación de integridad referencial.");
        } catch (Exception e) {
            attributes.addFlashAttribute("error", "No se puede eliminar un usuario porque esta asociado a uno o mas pagos.");
        }
        return "redirect:/users";
    }



    @ExceptionHandler(UserNotFoundException.class)
    public String handleUserNotFoundException(UserNotFoundException ex, RedirectAttributes attributes) {
        attributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:/users";
    }



    @ExceptionHandler(InvalidFileFormatException.class)
    public String handleInvalidFileFormatException(InvalidFileFormatException ex, RedirectAttributes attributes) {
        attributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:/users";
    }

}


