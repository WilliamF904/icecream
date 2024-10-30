package org.esfe.controllers;

import org.esfe.execpcion.RolNotFoundException;
import org.esfe.models.Product;
import org.esfe.models.Rol;
import org.esfe.services.interfaces.IRolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/roles")
public class RolController {

    @Autowired
    private IRolService rolService;


    @GetMapping
    public String index(Model model) {
        // Obtener todos los roles ordenados por ID en orden descendente
        List<Rol> roles = rolService.findAll(Sort.by(Sort.Order.desc("id")));
        model.addAttribute("user", roles); // Cambiar 'user' a 'roles' para que coincida con el nombre en la vista
        model.addAttribute("rol", new Rol());
        return "rol/index";
    }





    @GetMapping("/create")
    public String create(Rol rol){
        return "rol/create";
    }

    @PostMapping("/save")
    public String save(Rol rol, BindingResult result, Model model, RedirectAttributes attributes){
        if(result.hasErrors()){
            model.addAttribute(rol);
            attributes.addFlashAttribute("error", "No se pudo guardar debido a un error.");
            return "rol/create";
        }

        rolService.createOrEditOne(rol);
        attributes.addFlashAttribute("msg", "Rol creado correctamente");
        return "redirect:/roles";
    }


    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Integer id, Model model, RedirectAttributes attributes) {
        Rol rol = rolService.findOneById(id)
                .orElseThrow(() -> new RolNotFoundException("Rol no encontrado"));
        model.addAttribute("rol", rol);
        return "rol/edit";
    }




    @GetMapping("/details/{id}")
    public String details(@PathVariable("id") Integer id, Model model, RedirectAttributes attributes){
        Rol rol = rolService.findOneById(id).orElseThrow(() -> new RolNotFoundException("Rol no encontrado"));
        model.addAttribute("rol", rol);
        return "rol/details";
    }

    @GetMapping("/remove/{id}")
    public String remove(@PathVariable("id") Integer id, Model model, RedirectAttributes attributes) {
        Rol rol = rolService.findOneById(id)
                .orElseThrow(() -> new RolNotFoundException("Rol no encontrado"));
        model.addAttribute("rol", rol);
        return "rol/delete";
    }

    @PostMapping("/delete")
    public String delete(@RequestParam("id") Integer id, RedirectAttributes attributes) {
        Optional<Rol> rolOpt = rolService.findOneById(id);
        if (rolOpt.isPresent()) {
            Rol rol = rolOpt.get();
            if (rol.getUsers().isEmpty()) {
                rolService.deleteOneById(id);
                attributes.addFlashAttribute("msg2", "Rol eliminado correctamente");
            } else {
                attributes.addFlashAttribute("error", "No se puede eliminar el rol porque está asociado a uno o más usuarios.");
            }
        } else {
            attributes.addFlashAttribute("error", "Rol no encontrado");
        }
        return "redirect:/roles";
    }

    @ExceptionHandler(RolNotFoundException.class)
    public String handleRolNotFoundException(RolNotFoundException ex, RedirectAttributes attributes) {
        attributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:/roles";
    }

}