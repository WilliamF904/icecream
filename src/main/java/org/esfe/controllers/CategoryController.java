package org.esfe.controllers;

import org.esfe.execpcion.CategoryNotFoundException;
import org.esfe.execpcion.ProductNotFoundException;
import org.esfe.models.Category;
import org.esfe.models.Product;
import org.esfe.models.Rol;
import org.esfe.services.interfaces.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;
@Controller
@RequestMapping("/categories")
public class CategoryController {


    @Autowired
    private ICategoryService categoryService;


    @GetMapping
    public String index(Model model) {
        // Obtener todos los roles ordenados por ID en orden descendente
        List<Category> categories = categoryService.findAll(Sort.by(Sort.Order.desc("id")));
        model.addAttribute("product", categories); // Cambiar 'user' a 'roles' para que coincida con el nombre en la vista
        model.addAttribute("category", new Category());
        return "category/index";
    }


    @GetMapping("/create")
    public String create(Category category) {
        return "category/create";
    }

    @PostMapping("/save")
    public String save(Category category, BindingResult result, Model model, RedirectAttributes attributes) {
        if (result.hasErrors()) {
            model.addAttribute(category);
            attributes.addFlashAttribute("error", "No se pudo guardar debido a un error.");
            return "category/create";
        }

        categoryService.createOrEditOne(category);
        attributes.addFlashAttribute("msg", "Categoria creado correctamente");
        return "redirect:/categories";
    }

    @GetMapping("/details/{id}")
    public String details(@PathVariable("id") Integer id, Model model, RedirectAttributes attributes){
        Category category = categoryService.findOneById(id).orElseThrow(() -> new CategoryNotFoundException("Categoria no encontrado"));
        model.addAttribute("category", category);
        return "category/details";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Integer id, Model model, RedirectAttributes attributes){
        Category category = categoryService.findOneById(id).orElseThrow(() -> new CategoryNotFoundException("Categoria no encontrado"));
        model.addAttribute("category", category);
        return "category/edit";
    }

    @GetMapping("/remove/{id}")
    public String remove(@PathVariable("id") Integer id, Model model, RedirectAttributes attributes){
        Category category = categoryService.findOneById(id).orElseThrow(() -> new CategoryNotFoundException("Categoria no encontrado"));
        model.addAttribute("category", category);
        return "category/delete";
    }


    @PostMapping("/delete")
    public String delete(@RequestParam("id") Integer id, RedirectAttributes attributes) {
        Optional<Category> categoryOpt = categoryService.findOneById(id);
        if (categoryOpt.isPresent()) {
            Category category = categoryOpt.get();
            if (category.getProducts().isEmpty()) {
                categoryService.deleteOneById(id);
                attributes.addFlashAttribute("msg2", "Categoria eliminado correctamente");
            } else {
                attributes.addFlashAttribute("error", "No se puede eliminar la Categoria porque está asociado a uno o más productos.");
            }
        } else {
            attributes.addFlashAttribute("error", "Categoria no encontrado");
        }
        return "redirect:/categories";
    }



    @ExceptionHandler(CategoryNotFoundException.class)
    public String handleCategoryNotFoundException(CategoryNotFoundException ex, RedirectAttributes attributes) {
        attributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:/categories";
    }


}
