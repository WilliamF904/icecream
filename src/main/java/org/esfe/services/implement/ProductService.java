package org.esfe.services.implement;


import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.VerticalAlignment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import com.itextpdf.layout.properties.UnitValue;




import org.esfe.models.Product;
import org.esfe.repository.ProductRepository;
import org.esfe.services.interfaces.IProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import java.math.BigDecimal;
import java.util.Optional;

@Service
public class ProductService implements IProductService {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public Page<Product> findAll(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Override
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    @Override
    public List<Product> getAll() {
        return productRepository.findAll();
    }

    @Override
    public Optional<Product> findOneById(Integer productId) {
        return productRepository.findById(productId);
    }

    @Override
    public Product createOrEditOne(Product product) {
        return productRepository.save(product);
    }

    @Override
    public void deleteOneById(Integer productId) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        if (isProductAssociatedWithPayments(product)) {
            throw new IllegalStateException("No se puede eliminar el producto porque está asociado a uno o más pagos.");
        }

        productRepository.deleteById(productId);
    }

    private boolean isProductAssociatedWithPayments(Product product) {
        // Lógica para verificar si el producto está asociado a algún pago.
        // Esto puede implicar una consulta a la base de datos para verificar asociaciones.
        return false; // Cambiar según la lógica real
    }


    @Override
    public Page<Product> searchProducts(String description, Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice, Integer status, Pageable pageable) {
        Specification<Product> spec = Specification.where(null);

        if (description != null && !description.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.like(root.get("description"), "%" + description + "%"));
        }

        if (categoryId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId));
        }

        if (minPrice != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), minPrice));
        }

        if (maxPrice != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), maxPrice));
        }

        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        return productRepository.findAll(spec, pageable);
    }







    @Override
    public byte[] generatePdfReport(Integer productId) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            // Obtén el producto por ID
            Optional<Product> productOpt = productRepository.findById(productId);
            if (!productOpt.isPresent()) {
                throw new RuntimeException("Producto no encontrado");
            }
            Product product = productOpt.get();

            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Configuración del encabezado con logo y texto "ICE CREAM"
            float[] columnWidthsHeader = {1, 4};
            Table headerTable = new Table(columnWidthsHeader);
            headerTable.setWidth(UnitValue.createPercentValue(50));

            // Ruta del logo
            String logoPath = "src/main/resources/static/img/sorvete.png";
            Path pathLogo = Paths.get(logoPath).toAbsolutePath();

            try {
                // Crear imagen del logo
                Image logo = new Image(ImageDataFactory.create(pathLogo.toUri().toURL()));
                logo.setWidth(50);
                Cell logoCell = new Cell().add(logo).setBorder(Border.NO_BORDER);
                headerTable.addCell(logoCell);
            } catch (MalformedURLException e) {
                System.err.println("Error al cargar el logo: " + e.getMessage());
            }

            // Texto "ICE CREAM"
            Paragraph iceCreamText = new Paragraph("ICE CREAM")
                    .setBold()
                    .setFontSize(22)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMarginTop(10);
            Cell textCell = new Cell().add(iceCreamText).setVerticalAlignment(VerticalAlignment.MIDDLE).setBorder(Border.NO_BORDER);
            headerTable.addCell(textCell);

            document.add(headerTable);

            // Título del PDF
            document.add(new Paragraph("Detalles del Producto")
                    .setBold()
                    .setFontSize(16)
                    .setMarginBottom(-15)
                    .setTextAlignment(TextAlignment.LEFT));

            // Añadir fecha y hora actuales
            String currentDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            document.add(new Paragraph("Fecha de Generación: " + currentDateTime).setFontSize(10).setTextAlignment(TextAlignment.RIGHT));

            // Crear una tabla para alinear la imagen del producto a la derecha y los detalles a la izquierda
            float[] columnWidths = {3, 4}; // Ancho de columnas: 3 para los detalles, 1 para la imagen
            Table infoTable = new Table(columnWidths);
            infoTable.setWidth(UnitValue.createPercentValue(100));

            // Crear una celda para los detalles del producto
            Cell detailsCell = new Cell().setBorder(Border.NO_BORDER);
            detailsCell.add(new Paragraph("ID: " + product.getId()).setFontSize(12).setBold());
            detailsCell.add(new Paragraph("Descripción: " + product.getDescription()).setFontSize(12));
            detailsCell.add(new Paragraph("Categoría: " + product.getCategory().getCategoryname()).setFontSize(12));
            detailsCell.add(new Paragraph("Precio: $" + product.getPrice()).setFontSize(12));
            detailsCell.add(new Paragraph("Cantidad en Stock: " + product.getQuantity()).setFontSize(12));
            detailsCell.add(new Paragraph("Estado: " + (product.getStatus() == 1 ? "Activo" : "Inactivo")).setFontSize(12));

            infoTable.addCell(detailsCell);

            // Verificación de imagen del producto o imagen predeterminada
            String productImagePath = product.getUrl() != null && !product.getUrl().isEmpty()
                    ? "src/main/resources/static/" + product.getUrl()
                    : "src/main/resources/static/img/generalproducto.png";

            Path pathProductImage = Paths.get(productImagePath).toAbsolutePath();

            try {
                if (!Files.exists(pathProductImage)) {
                    pathProductImage = Paths.get("src/main/resources/static/img/generalproducto.png").toAbsolutePath();
                }

                Image productImage = new Image(ImageDataFactory.create(pathProductImage.toUri().toURL()));
                productImage.setWidth(100); // Ajusta el tamaño de la imagen si es necesario
                Cell imageCell = new Cell().add(productImage)
                        .setBorder(Border.NO_BORDER)
                        .setHorizontalAlignment(HorizontalAlignment.RIGHT)
                        .setVerticalAlignment(VerticalAlignment.MIDDLE);
                infoTable.addCell(imageCell);
            } catch (MalformedURLException e) {
                System.err.println("Error al cargar la imagen del producto: " + e.getMessage());
            }

            document.add(infoTable);

            // Cerrar el documento
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return outputStream.toByteArray();
    }



    @Override
    public byte[] generateAllProductsPdfReport() {
        try {
            List<Product> products = productRepository.findAll();
            return generateProductListPdf(products, "Todos los Productos");
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el PDF de todos los productos: " + e.getMessage());
        }
    }

    @Override
    public byte[] generateProductsPdfReportByStatus(Integer status) {
        try {
            List<Product> products = productRepository.findByStatus(status);
            String title = status == 1 ? "Productos Activos" : "Productos Inactivos";
            return generateProductListPdf(products, title);
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el PDF de productos por estado: " + e.getMessage());
        }
    }

    private byte[] generateProductListPdf(List<Product> products, String title) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Configuración del encabezado con logo y texto
            float[] columnWidthsHeader = {1, 4};
            Table headerTable = new Table(columnWidthsHeader);
            headerTable.setWidth(UnitValue.createPercentValue(50));

            // Añadir logo a la tabla
            String logoPath = "src/main/resources/static/img/sorvete.png"; // Ruta relativa al logo
            Path path = Paths.get(logoPath).toAbsolutePath(); // Obtener ruta absoluta

            try {
                // Crear Image usando la ruta absoluta
                Image logo = new Image(ImageDataFactory.create(path.toUri().toURL()));
                logo.setWidth(50); // Ajusta el tamaño del logo si es necesario
                Cell logoCell = new Cell().add(logo).setBorder(Border.NO_BORDER);
                headerTable.addCell(logoCell);
            } catch (MalformedURLException e) {
                // Manejo de excepción: mostrar error en consola o log, según convenga
                System.err.println("Error al cargar el logo: " + e.getMessage());
            }

            // Añadir texto "ICE CREAM" alineado con el logo
            Paragraph iceCreamText = new Paragraph("ICE CREAM")
                    .setBold()
                    .setFontSize(22)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMarginTop(10);
            Cell textCell = new Cell().add(iceCreamText).setVerticalAlignment(VerticalAlignment.MIDDLE).setBorder(Border.NO_BORDER);
            headerTable.addCell(textCell);

            document.add(headerTable);

            // Añadir título de la lista de productos
            document.add(new Paragraph(title).setBold().setFontSize(16).setMarginBottom(-10).setTextAlignment(TextAlignment.LEFT));

            // Añadir fecha y hora actuales
            String currentDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            document.add(new Paragraph("Fecha de Generación: " + currentDateTime).setFontSize(10).setTextAlignment(TextAlignment.RIGHT));

            // Crear y estilizar la tabla de productos
            float[] columnWidths = {50f, 150f, 100f, 80f, 80f, 80f};
            Table table = new Table(columnWidths);

            // Encabezados de la tabla con fondo morado y texto blanco
            Cell headerId = new Cell().add(new Paragraph("ID").setBold().setFontColor(ColorConstants.WHITE)).setBackgroundColor(new DeviceRgb(122, 89, 168));
            Cell headerDescripcion = new Cell().add(new Paragraph("Descripción").setBold().setFontColor(ColorConstants.WHITE)).setBackgroundColor(new DeviceRgb(122, 89, 168));
            Cell headerCategoria = new Cell().add(new Paragraph("Categoría").setBold().setFontColor(ColorConstants.WHITE)).setBackgroundColor(new DeviceRgb(122, 89, 168));
            Cell headerPrecio = new Cell().add(new Paragraph("Precio").setBold().setFontColor(ColorConstants.WHITE)).setBackgroundColor(new DeviceRgb(122, 89, 168));
            Cell headerStock = new Cell().add(new Paragraph("Stock").setBold().setFontColor(ColorConstants.WHITE)).setBackgroundColor(new DeviceRgb(122, 89, 168));
            Cell headerEstado = new Cell().add(new Paragraph("Estado").setBold().setFontColor(ColorConstants.WHITE)).setBackgroundColor(new DeviceRgb(122, 89, 168));

            table.addHeaderCell(headerId);
            table.addHeaderCell(headerDescripcion);
            table.addHeaderCell(headerCategoria);
            table.addHeaderCell(headerPrecio);
            table.addHeaderCell(headerStock);
            table.addHeaderCell(headerEstado);

            // Filas de la tabla de productos
            for (Product product : products) {
                table.addCell(new Cell().add(new Paragraph(product.getId().toString())));
                table.addCell(new Cell().add(new Paragraph(product.getDescription())));
                table.addCell(new Cell().add(new Paragraph(product.getCategory().getCategoryname())));
                table.addCell(new Cell().add(new Paragraph("$" + product.getPrice().toString())));
                table.addCell(new Cell().add(new Paragraph(product.getQuantity().toString())));
                table.addCell(new Cell().add(new Paragraph(product.getStatus() == 1 ? "Activo" : "Inactivo")));
            }

            document.add(table);
            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el PDF: " + e.getMessage());
        }
    }



    private void addTableRow(Table table, String key, String value) {
        table.addCell(new Cell().add(new Paragraph(key)));
        table.addCell(new Cell().add(new Paragraph(value)));
    }









}
