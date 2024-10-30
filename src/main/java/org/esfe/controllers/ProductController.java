package org.esfe.controllers;

import jakarta.validation.Valid;
import org.esfe.execpcion.InvalidFileFormatException;
import org.esfe.execpcion.ProductNotFoundException;
import org.esfe.execpcion.UserNotFoundException;
import org.esfe.models.Product;
import org.esfe.services.implement.ProductService;
import org.esfe.services.interfaces.IProductService;
import org.esfe.services.interfaces.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private IProductService productService;

    @Autowired
    private ICategoryService categoryService;

    @Autowired
    private org.esfe.services.implement.ProductService ProductService;



    @GetMapping
    public String index(Model model,
                        @RequestParam("page") Optional<Integer> page,
                        @RequestParam("size") Optional<Integer> size,
                        @RequestParam(value = "description", required = false) String description,
                        @RequestParam(value = "categoryId", required = false) Integer categoryId,
                        @RequestParam(value = "minPrice", required = false) BigDecimal minPrice,
                        @RequestParam(value = "maxPrice", required = false) BigDecimal maxPrice,
                        @RequestParam(value = "status", required = false) Integer status) {

        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("product", new Product()); // Producto vacío para el formulario del modal

        int currentPage = page.orElse(1) - 1;
        int pageSize = size.orElse(5);
        Pageable pageable = PageRequest.of(currentPage, pageSize, Sort.by(Sort.Order.desc("id")));

        // Lógica de filtrado
        Page<Product> products;
        if (description != null || categoryId != null || minPrice != null || maxPrice != null || status != null) {
            products = productService.searchProducts(description, categoryId, minPrice, maxPrice, status, pageable);
        } else {
            products = productService.findAll(pageable);
        }

        model.addAttribute("products", products);

        int totalPages = products.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .boxed()
                    .collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }

        return "product/index";
    }

    //generar pdf
    @RestController
    @RequestMapping("/products/pdf")
    public class ProductPdfController {

        @Autowired
        private IProductService productService;

        // Generar PDF de todos los productos
        @GetMapping("/all")
        public ResponseEntity<byte[]> generateAllProductsPdf() {
            byte[] pdfContent = productService.generateAllProductsPdfReport();
            return createPdfResponse(pdfContent, "todos_productos.pdf");
        }

        // Generar PDF de productos activos
        @GetMapping("/active")
        public ResponseEntity<byte[]> generateActiveProductsPdf() {
            byte[] pdfContent = productService.generateProductsPdfReportByStatus(1);
            return createPdfResponse(pdfContent, "productos_activos.pdf");
        }

        // Generar PDF de productos inactivos
        @GetMapping("/inactive")
        public ResponseEntity<byte[]> generateInactiveProductsPdf() {
            byte[] pdfContent = productService.generateProductsPdfReportByStatus(0);
            return createPdfResponse(pdfContent, "productos_inactivos.pdf");
        }

        @GetMapping("/products/{id}/pdf")
        public ResponseEntity<byte[]> generatePdf(@PathVariable("id") Integer id) throws IOException {
            byte[] pdfContent = productService.generatePdfReport(id);
            return createPdfResponse(pdfContent, "producto_" + id + ".pdf");
        }


        // Método auxiliar para crear la respuesta PDF
        private ResponseEntity<byte[]> createPdfResponse(byte[] pdfContent, String fileName) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", fileName);
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfContent);
        }
    }

    @GetMapping("/products/{id}/pdf")
    public ResponseEntity<byte[]> generatePdf(@PathVariable("id") Integer id) {
        byte[] pdfContent = productService.generatePdfReport(id);
        return createPdfResponse(pdfContent, "producto_" + id + ".pdf");
    }

    // Método auxiliar para crear la respuesta PDF
    private ResponseEntity<byte[]> createPdfResponse(byte[] pdfContent, String fileName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", fileName);
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfContent);
    }







    /*
    @GetMapping
    public String index(Model model,
                        @RequestParam("page") Optional<Integer> page,
                        @RequestParam("size") Optional<Integer> size,
                        @RequestParam(value = "description", required = false) String description,
                        @RequestParam(value = "categoryId", required = false) Integer categoryId,
                        @RequestParam(value = "minPrice", required = false) BigDecimal minPrice,
                        @RequestParam(value = "maxPrice", required = false) BigDecimal maxPrice,
                        @RequestParam(value = "status", required = false) Integer status) {

        model.addAttribute("categories", categoryService.findAll());

        int currentPage = page.orElse(1) - 1;
        int pageSize = size.orElse(5);
        Pageable pageable = PageRequest.of(currentPage, pageSize, Sort.by(Sort.Order.desc("id")));


        // Lógica de filtrado
        Page<Product> products;
        if (description != null || categoryId != null || minPrice != null || maxPrice != null || status != null) {
            products = productService.searchProducts(description, categoryId, minPrice, maxPrice, status, pageable);
        } else {
            products = productService.findAll(pageable);
        }

        model.addAttribute("products", products);

        int totalPages = products.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .boxed()
                    .collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }

        return "product/index";
    }
*/

    @GetMapping("/create")
    public String create(Product product, Model model){
        model.addAttribute("categories", categoryService.findAll());
        return "product/create";
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
    public String save(@Valid Product product, BindingResult result, Model model,
                       @RequestParam("urlImage") MultipartFile file,  // Captura el archivo
                       RedirectAttributes attributes){
        try {
        if(result.hasErrors()){
            model.addAttribute("categories", categoryService.findAll());
            attributes.addFlashAttribute("error", "No se pudo guardar debido a un error.");
            return "product/create";
        }
            // Maneja la subida de la imagen
            if (file != null && !file.isEmpty()) {
                String url = guardarImagen(file);  // Guarda la imagen y obtiene la URL
                product.setUrl(url);  // Asigna la URL al campo 'url' del objeto User
            }

        productService.createOrEditOne(product);
        attributes.addFlashAttribute("msg", "Producto creado correctamente");
        return "redirect:/products";

        } catch (IOException e) {
            attributes.addFlashAttribute("error", "Error al subir la imagen: " + e.getMessage());
            return "redirect:/products";
        }
    }


    @GetMapping("/details/{id}")
    public String details(@PathVariable("id") Integer id, Model model, RedirectAttributes attributes){
        Product product = productService.findOneById(id).orElseThrow(() -> new ProductNotFoundException("Producto no encontrado"));
        model.addAttribute("product", product);
        return "product/details";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Integer id, Model model, RedirectAttributes attributes){
        Product product = productService.findOneById(id).orElseThrow(() -> new ProductNotFoundException("Producto no encontrado"));
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.findAll());
        return "product/edit";
    }



    @GetMapping("/remove/{id}")
    public String remove(@PathVariable("id") Integer id, Model model, RedirectAttributes attributes){
        Product product = productService.findOneById(id).orElseThrow(() -> new ProductNotFoundException("Producto no encontrado"));
        model.addAttribute("product", product);
        return "product/delete";
    }

    @PostMapping("/delete")
    public String delete(Product product, RedirectAttributes attributes) {
        try {
            productService.deleteOneById(product.getId());
            attributes.addFlashAttribute("msg2", "Producto eliminado correctamente");
        } catch (Exception e) {
            attributes.addFlashAttribute("error", "No se puede eliminar el producto porque está asociado a uno o más pagos.");
        }
        return "redirect:/products";
    }



    @GetMapping("/plus/{id}")
    public String plus(@PathVariable("id") Integer id, Model model, RedirectAttributes attributes) {
        Product product = productService.findOneById(id).orElseThrow(() -> new ProductNotFoundException("Producto no encontrado"));
        model.addAttribute("product", product);
        return "product/plus"; // Redirige a la vista que se creará
    }

    @PostMapping("/addQuantity/{id}")
    public String addQuantity(@PathVariable("id") Integer id, @RequestParam("quantity") Integer quantity, RedirectAttributes attributes) {
        try {
            Product product = productService.findOneById(id).orElseThrow(() -> new ProductNotFoundException("Producto no encontrado"));
            product.setQuantity(product.getQuantity() + quantity); // Sumar la nueva cantidad a la existente
            productService.createOrEditOne(product); // Guardar el producto actualizado
            attributes.addFlashAttribute("msg", "Cantidad añadida/removida correctamente");
        } catch (Exception e) {
            attributes.addFlashAttribute("error", "Error al añadir cantidad: " + e.getMessage());
        }
        return "redirect:/products"; // Redirige a la lista de productos
    }



    @ExceptionHandler(ProductNotFoundException.class)
    public String handleProductNotFoundException(ProductNotFoundException ex, RedirectAttributes attributes) {
        attributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:/products";
    }



    @ExceptionHandler(InvalidFileFormatException.class)
    public String handleInvalidFileFormatException(InvalidFileFormatException ex, RedirectAttributes attributes) {
        attributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:/products";
    }

}
