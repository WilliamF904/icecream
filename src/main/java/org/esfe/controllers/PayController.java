package org.esfe.controllers;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import jakarta.validation.Valid;
import org.esfe.execpcion.PayNotFoundException;
import org.esfe.execpcion.ProductNotFoundException;
import org.esfe.models.Pay;
import org.esfe.models.Product;
import org.esfe.services.interfaces.IPayService;
import org.esfe.services.interfaces.IProductService;
import org.esfe.services.interfaces.IUserService;
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

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/pays")
public class PayController {

    @Autowired
    private IPayService payService;

    @Autowired
    private IUserService userService;

    @Autowired
    private IProductService productService;





    @GetMapping
    public String index(Model model,
                        @RequestParam("page") Optional<Integer> page,
                        @RequestParam("size") Optional<Integer> size,
                        @RequestParam(value = "nameclient", required = false) String nameclient,
                        @RequestParam(value = "startDate", required = false) String startDateStr,
                        @RequestParam(value = "endDate", required = false) String endDateStr) {

        int currentPage = page.orElse(1) - 1;
        int pageSize = size.orElse(5);
        Pageable pageable = PageRequest.of(currentPage, pageSize, Sort.by(Sort.Order.desc("id")));


        // Convertir String a LocalDate para manipular fechas
        LocalDate startLocalDate = null;
        LocalDate endLocalDate = null;

        try {
            if (startDateStr != null && !startDateStr.isEmpty()) {
                startLocalDate = LocalDate.parse(startDateStr);
            }
            if (endDateStr != null && !endDateStr.isEmpty()) {
                endLocalDate = LocalDate.parse(endDateStr);
            }
        } catch (DateTimeParseException e) {
            model.addAttribute("error", "Formato de fecha no válido.");
            return "pay/index"; // Redirige a la misma página con el mensaje de error
        }

        // Lógica de validación y ajuste de fechas
        if (startLocalDate != null && endLocalDate == null) {
            endLocalDate = LocalDate.now();
        } else if (startLocalDate == null && endLocalDate != null) {
            model.addAttribute("error", "Debe proporcionar la fecha de inicio.");
            return "pay/index"; // Redirige a la misma página con el mensaje de error
        }

        if (startLocalDate != null && endLocalDate != null && startLocalDate.isAfter(endLocalDate)) {
            model.addAttribute("error", "La fecha de inicio no puede ser después de la fecha de fin.");
            return "pay/index"; // Redirige a la misma página con el mensaje de error
        }

        // Ajustar startDate si es nulo y endDate tiene un valor
        if (startLocalDate == null && endLocalDate != null) {
            startLocalDate = endLocalDate.minusDays(5);
        }

        // Convertir LocalDate a Date para pasar a los servicios
        Date startDate = (startLocalDate != null) ? Date.valueOf(startLocalDate) : null;
        Date endDate = (endLocalDate != null) ? Date.valueOf(endLocalDate) : null;

        // Lógica de filtrado
        Page<Pay> pays;
        if (nameclient != null || startDate != null || endDate != null) {
            pays = payService.searchPays(nameclient, startDate, endDate, pageable);
        } else {
            pays = payService.findAll(pageable);
        }

        model.addAttribute("pays", pays);

        int totalPages = pays.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .boxed()
                    .collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }

        return "pay/index";
    }

    @GetMapping("/details/{id}")
    public String details(@PathVariable("id") Integer id, Model model, RedirectAttributes attributes) {
        Pay pay = payService.findOneById(id)
                .orElseThrow(() -> new PayNotFoundException("Pago no encontrado"));

        // Calcular el total
        BigDecimal total = pay.getProductQuantities().entrySet().stream()
                .map(entry -> entry.getKey().getPrice().multiply(new BigDecimal(entry.getValue())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("pay", pay);
        model.addAttribute("total", total);  // Pasar el total a la vista
        return "pay/details";
    }




    @GetMapping("/create")
    public String create(Model model) {
        Pay pay = new Pay();
        // Establecer la fecha y hora actual
        pay.setPaydate(Date.valueOf(LocalDate.now()));
        pay.setPaytime(Time.valueOf(LocalTime.now().withNano(0))); // Sin nanosegundos
        List<Product> products = productService.getAll();
        model.addAttribute("pay", pay);
        model.addAttribute("users", userService.findAll());
        model.addAttribute("products", productService.findAll());
        return "pay/create";
    }


    @PostMapping("/save")
    public String save(@ModelAttribute Pay pay,
                       @RequestParam(value = "downloadPdf", required = false) Boolean downloadPdf,
                       Model model,
                       HttpServletResponse response,
                       RedirectAttributes redirectAttributes) throws IOException {
        // Validar que la lista de productos no esté vacía
        if (pay.getProductQuantities().isEmpty()) {
            model.addAttribute("error", "Debe seleccionar al menos un producto.");
            return "pay/create";
        }

        // Crear una lista de productos a partir del mapa de cantidades
        List<Product> products = pay.getProductQuantities().keySet().stream().collect(Collectors.toList());
        pay.setProducts(products);

        for (Map.Entry<Product, Integer> entry : pay.getProductQuantities().entrySet()) {
            Product product = entry.getKey();
            Integer quantity = entry.getValue();
            product.setQuantity(product.getQuantity() - quantity);
            productService.createOrEditOne(product);
        }

        // Guardar el pago en la base de datos
        payService.createOrEditOne(pay);

        if (Boolean.TRUE.equals(downloadPdf)) {
            // Guardar la URL de descarga del PDF como mensaje flash
            redirectAttributes.addFlashAttribute("pdfDownloadUrl", "/pays/" + pay.getId() + "/pdf");
            redirectAttributes.addFlashAttribute("msg", "Pago guardado exitosamente y PDF descargado.");
        }
        else {
            redirectAttributes.addFlashAttribute("msg", "Pago guardado exitosamente.");
        }

        return "redirect:/pays";
    }

//Tamaño de recibo
@GetMapping("/{id}/pdf")
@ResponseBody
public void generatePdf(@PathVariable("id") Integer id, HttpServletResponse response) throws IOException {
    Pay pay = payService.findOneById(id)
            .orElseThrow(() -> new PayNotFoundException("Pago no encontrado"));

    response.setContentType("application/pdf");
    response.setHeader("Content-Disposition", "attachment; filename=detalle_pago_" + id + ".pdf");

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PdfWriter writer = new PdfWriter(baos);
    PdfDocument pdfDoc = new PdfDocument(writer);

    PageSize receiptPageSize = new PageSize(80 * 2.83465f, 200 * 2.83465f);
    pdfDoc.setDefaultPageSize(receiptPageSize);

    Document document = new Document(pdfDoc);

    // Ajustes generales de espaciado y fuente
    float smallFontSize = 9;
    float headerFontSize = 12;
    float paragraphMarginBottom = 0.3f; // Ajuste del margen inferior
    document.setFontSize(smallFontSize);

    // Encabezado: logo y texto "ICE CREAM"
    float[] columnWidthsHeader = {1, 3};
    Table headerTable = new Table(columnWidthsHeader);
    headerTable.setWidth(UnitValue.createPercentValue(100));

    String logoPath = "src/main/resources/static/img/sorvete.png";
    Image logo = new Image(ImageDataFactory.create(logoPath));
    logo.setWidth(30);
    Cell logoCell = new Cell().add(logo).setBorder(Border.NO_BORDER);
    headerTable.addCell(logoCell);

    Paragraph iceCreamText = new Paragraph("ICE CREAM")
            .setBold()
            .setFontSize(headerFontSize)
            .setTextAlignment(TextAlignment.LEFT)
            .setMarginBottom(paragraphMarginBottom); // Margen inferior para el título
    Cell textCell = new Cell().add(iceCreamText)
            .setVerticalAlignment(VerticalAlignment.MIDDLE)
            .setBorder(Border.NO_BORDER);
    headerTable.addCell(textCell);
    document.add(headerTable);

    // Título "Detalles de Pago"
    document.add(new Paragraph("Detalles de Pago")
            .setBold()
            .setFontSize(smallFontSize)
            .setTextAlignment(TextAlignment.LEFT)
            .setMarginBottom(paragraphMarginBottom));

    // Información de pago, usando margen inferior para espaciado
    document.add(new Paragraph("Id de Pago: " + pay.getId()).setBold().setMarginBottom(paragraphMarginBottom));
    document.add(new Paragraph("Fecha de Pago: " + pay.getPaydate()).setMarginBottom(paragraphMarginBottom));
    document.add(new Paragraph("Hora de Pago: " + pay.getPaytime()).setMarginBottom(paragraphMarginBottom));
    document.add(new Paragraph("Usuario: " + pay.getUser().getName() + " " + pay.getUser().getLastname()).setMarginBottom(paragraphMarginBottom));
    document.add(new Paragraph("Nombre del Cliente: " + pay.getNameclient()).setMarginBottom(paragraphMarginBottom));
    document.add(new Paragraph("\n").setMarginBottom(paragraphMarginBottom));

    // Tabla de productos adaptada a un tamaño de recibo
    float[] columnWidths = {4, 1.5f, 2, 2};
    Table table = new Table(columnWidths);
    table.setWidth(UnitValue.createPercentValue(100));

    Cell headerProducto = new Cell().add(new Paragraph("Producto").setBold().setFontColor(ColorConstants.WHITE).setFontSize(smallFontSize)).setBackgroundColor(new DeviceRgb(122, 89, 168));
    Cell headerCantidad = new Cell().add(new Paragraph("Cant").setBold().setFontColor(ColorConstants.WHITE).setFontSize(smallFontSize)).setBackgroundColor(new DeviceRgb(122, 89, 168));
    Cell headerPrecio = new Cell().add(new Paragraph("Precio").setBold().setFontColor(ColorConstants.WHITE).setFontSize(smallFontSize)).setBackgroundColor(new DeviceRgb(122, 89, 168));
    Cell headerSubtotal = new Cell().add(new Paragraph("Subt").setBold().setFontColor(ColorConstants.WHITE).setFontSize(smallFontSize)).setBackgroundColor(new DeviceRgb(122, 89, 168));
    table.addHeaderCell(headerProducto);
    table.addHeaderCell(headerCantidad);
    table.addHeaderCell(headerPrecio);
    table.addHeaderCell(headerSubtotal);

    // Llenar la tabla con productos
    pay.getProductQuantities().forEach((product, quantity) -> {
        BigDecimal price = product.getPrice();
        BigDecimal subtotal = price.multiply(new BigDecimal(quantity));

        table.addCell(new Cell().add(new Paragraph(product.getDescription()).setFontSize(smallFontSize).setMarginBottom(paragraphMarginBottom)));
        table.addCell(new Cell().add(new Paragraph(quantity.toString())).setTextAlignment(TextAlignment.CENTER).setFontSize(smallFontSize).setMarginBottom(paragraphMarginBottom));
        table.addCell(new Cell().add(new Paragraph("$" + price.toString())).setTextAlignment(TextAlignment.CENTER).setFontSize(smallFontSize).setMarginBottom(paragraphMarginBottom));
        table.addCell(new Cell().add(new Paragraph("$" + subtotal.toString())).setTextAlignment(TextAlignment.CENTER).setFontSize(smallFontSize).setMarginBottom(paragraphMarginBottom));
    });

    // Fila de total
    Cell totalLabelCell = new Cell(1, 3)
            .add(new Paragraph("Total:"))
            .setBold()
            .setTextAlignment(TextAlignment.RIGHT)
            .setBorderTop(new SolidBorder(ColorConstants.BLACK, 1f))
            .setFontSize(smallFontSize)
            .setMarginBottom(paragraphMarginBottom);
    table.addCell(totalLabelCell);

    Cell totalValueCell = new Cell()
            .add(new Paragraph("$" + pay.getTotal().toString()))
            .setBold()
            .setTextAlignment(TextAlignment.CENTER)
            .setBorderTop(new SolidBorder(ColorConstants.BLACK, 1f))
            .setFontSize(smallFontSize)
            .setMarginBottom(paragraphMarginBottom);
    table.addCell(totalValueCell);

    document.add(table);
    document.close();

    response.getOutputStream().write(baos.toByteArray());
    response.getOutputStream().flush();
}

    //tamaño normal
 /*   @GetMapping("/{id}/pdf")
    @ResponseBody
    public void generatePdf(@PathVariable("id") Integer id, HttpServletResponse response) throws IOException {
        Pay pay = payService.findOneById(id)
                .orElseThrow(() -> new PayNotFoundException("Pago no encontrado"));

        // Configurar la respuesta para que sea un archivo PDF
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=detalle_pago_" + id + ".pdf");

        // Crear un documento PDF con iText
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        // Crear tabla para el logo y el texto "ICE CREAM" alineados horizontalmente
        float[] columnWidthsHeader = {1, 4}; // Ajusta los anchos de las columnas si es necesario
        Table headerTable = new Table(columnWidthsHeader);
        headerTable.setWidth(UnitValue.createPercentValue(50)); // Ajusta el ancho de la tabla si quieres un tamaño específico

        // Añadir logo a la tabla
        String logoPath = "src/main/resources/static/img/sorvete.png"; // Cambia esta ruta al logo que quieras usar
        Image logo = new Image(ImageDataFactory.create(logoPath));
        logo.setWidth(50); // Ajusta el tamaño del logo si es necesario
        Cell logoCell = new Cell().add(logo).setBorder(Border.NO_BORDER);
        headerTable.addCell(logoCell);

        // Añadir texto "ICE CREAM" alineado verticalmente con el logo
        Paragraph iceCreamText = new Paragraph("ICE CREAM")
                .setBold()
                .setFontSize(22)
                .setMarginTop(10) // Ajuste de margen superior para centrar verticalmente
                .setTextAlignment(TextAlignment.LEFT);
        Cell textCell = new Cell().add(iceCreamText).setVerticalAlignment(VerticalAlignment.MIDDLE).setBorder(Border.NO_BORDER);
        headerTable.addCell(textCell);

        // Agregar la tabla de encabezado al documento
        document.add(headerTable);

        // Añadir título "Detalles de Pago"
        document.add(new Paragraph("Detalles de Pago").setBold().setFontSize(18).setTextAlignment(TextAlignment.LEFT));

        // Añadir información de pago
        document.add(new Paragraph("Id de Pago: " + pay.getId()).setBold());
        document.add(new Paragraph("Fecha de Pago: " + pay.getPaydate()));
        document.add(new Paragraph("Hora de Pago: " + pay.getPaytime()));
        document.add(new Paragraph("Usuario: " + pay.getUser().getName() + " " + pay.getUser().getLastname()));
        document.add(new Paragraph("Nombre del Cliente: " + pay.getNameclient()));
        document.add(new Paragraph("\n"));

        // Crear y dar estilo a la tabla de productos
        float[] columnWidths = {4, 2, 2, 2};
        Table table = new Table(columnWidths);

        // Encabezado de la tabla con fondo morado y texto en blanco
        Cell headerProducto = new Cell().add(new Paragraph("Producto").setBold().setFontColor(ColorConstants.WHITE)).setBackgroundColor(new DeviceRgb(122, 89, 168));
        Cell headerCantidad = new Cell().add(new Paragraph("Cantidad").setBold().setFontColor(ColorConstants.WHITE)).setBackgroundColor(new DeviceRgb(122, 89, 168));
        Cell headerPrecio = new Cell().add(new Paragraph("Precio Unitario").setBold().setFontColor(ColorConstants.WHITE)).setBackgroundColor(new DeviceRgb(122, 89, 168));
        Cell headerSubtotal = new Cell().add(new Paragraph("Subtotal").setBold().setFontColor(ColorConstants.WHITE)).setBackgroundColor(new DeviceRgb(122, 89, 168));
        table.addHeaderCell(headerProducto);
        table.addHeaderCell(headerCantidad);
        table.addHeaderCell(headerPrecio);
        table.addHeaderCell(headerSubtotal);

        // Llenar la tabla con productos y centrar los valores de cantidad, precio y subtotal
        pay.getProductQuantities().forEach((product, quantity) -> {
            BigDecimal price = product.getPrice();
            BigDecimal subtotal = price.multiply(new BigDecimal(quantity));

            table.addCell(new Cell().add(new Paragraph(product.getDescription())));
            table.addCell(new Cell().add(new Paragraph(quantity.toString())).setTextAlignment(TextAlignment.CENTER));
            table.addCell(new Cell().add(new Paragraph("$" + price.toString())).setTextAlignment(TextAlignment.CENTER));
            table.addCell(new Cell().add(new Paragraph("$" + subtotal.toString())).setTextAlignment(TextAlignment.CENTER));
        });

        // Añadir fila para el total, con borde y centrado
        Cell totalLabelCell = new Cell(1, 3)
                .add(new Paragraph("Total:"))
                .setBold()
                .setTextAlignment(TextAlignment.RIGHT)
                .setBorderTop(new SolidBorder(ColorConstants.BLACK, 1f)); // Borde superior para el total
        table.addCell(totalLabelCell);

        Cell totalValueCell = new Cell()
                .add(new Paragraph("$" + pay.getTotal().toString())) // Agrega el símbolo $
                .setBold()
                .setTextAlignment(TextAlignment.CENTER) // Centra el valor total
                .setBorderTop(new SolidBorder(ColorConstants.BLACK, 1f)); // Borde superior
        table.addCell(totalValueCell);

        // Añadir la tabla al documento
        document.add(table);

        // Cerrar el documento PDF
        document.close();

        // Enviar el contenido PDF al cliente
        response.getOutputStream().write(baos.toByteArray());
        response.getOutputStream().flush();
    }

*/


  /*  @PostMapping("/save")
    public String save(@ModelAttribute Pay pay, Model model) {
        // Validar que la lista de productos no esté vacía
        if (pay.getProductQuantities().isEmpty()) {
            model.addAttribute("error", "Debe seleccionar al menos un producto.");
            return "pay/create";
        }

        // Crear una lista de productos a partir del mapa de cantidades
        List<Product> products = pay.getProductQuantities().keySet().stream().collect(Collectors.toList());
        pay.setProducts(products);

        for (Map.Entry<Product, Integer> entry : pay.getProductQuantities().entrySet()) {
            Product product = entry.getKey();
            Integer quantity = entry.getValue();
            product.setQuantity(product.getQuantity() - quantity);
            productService.createOrEditOne(product);
        }

        // Guardar el pago en la base de datos
        payService.createOrEditOne(pay);
        model.addAttribute("msg", "Pago guardado exitosamente");
        return "redirect:/pays";
    }
*/





/*
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Integer id, Model model, RedirectAttributes attributes) {
        Pay -pay = payService.findOneById(id).orElseThrow(() -> new PayNotFoundException("Pago no encontrado"));
        model.addAttribute("pay", pay);
        model.addAttribute("users", userService.findAll());
        model.addAttribute("products", productService.findAll());
        return "pay/edit";
    }
    @PostMapping("/delete")
    public String delete(@RequestParam("id") Integer id, RedirectAttributes attributes) {
        payService.deleteOneById(id);
        attributes.addFlashAttribute("msg2", "Pago eliminado correctamente");
        return "redirect:/pays";
    }
*/


    @ExceptionHandler(PayNotFoundException.class)
    public String handlePayNotFoundException(PayNotFoundException ex, RedirectAttributes attributes) {
        attributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:/pays";
    }
}
