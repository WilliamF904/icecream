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
import org.esfe.models.User;
import org.esfe.services.interfaces.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import com.itextpdf.layout.properties.UnitValue;


@Service
public class PdfService {

    public byte[] generatePdfReport(Integer id) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            // Obtén el usuario por ID
            User user = userService.findById(id);
            if (user == null) {
                throw new RuntimeException("Usuario no encontrado con ID: " + id);
            }

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
            document.add(new Paragraph("Detalles del Usuario")
                    .setBold()
                    .setFontSize(16)
                    .setMarginBottom(-15)
                    .setTextAlignment(TextAlignment.LEFT));

            // Añadir fecha y hora actuales
            String currentDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            document.add(new Paragraph("Fecha de Generación: " + currentDateTime).setFontSize(10).setTextAlignment(TextAlignment.RIGHT));

            // Crear una tabla para alinear la imagen del usuario a la derecha y los detalles a la izquierda
            float[] columnWidths = {3, 1}; // Ancho de columnas: 3 para los detalles, 1 para la imagen
            Table infoTable = new Table(columnWidths);
            infoTable.setWidth(UnitValue.createPercentValue(100));

            // Crear una celda para los detalles del usuario
            Cell detailsCell = new Cell().setBorder(Border.NO_BORDER);
            detailsCell.add(new Paragraph("ID: " + user.getId()).setFontSize(12).setBold());
            detailsCell.add(new Paragraph("Nombre: " + user.getName()).setFontSize(12));
            detailsCell.add(new Paragraph("Apellido: " + user.getLastname()).setFontSize(12));
            detailsCell.add(new Paragraph("Correo Electrónico: " + user.getEmail()).setFontSize(12));
            detailsCell.add(new Paragraph("Rol: " + user.getRol().getRolname()).setFontSize(12));
            detailsCell.add(new Paragraph("Estado: " + (user.getStatus() == 1 ? "Activo" : "Inactivo")).setFontSize(12));

            infoTable.addCell(detailsCell);

            // Verificación de imagen del usuario o imagen predeterminada
            String userImagePath = user.getUrl() != null && !user.getUrl().isEmpty()
                    ? "src/main/resources/static/" + user.getUrl()
                    : "src/main/resources/static/img/generalusuario.png";

            Path pathUserImage = Paths.get(userImagePath).toAbsolutePath();

            try {
                if (!Files.exists(pathUserImage)) {
                    pathUserImage = Paths.get("src/main/resources/static/img/generalusuario.png").toAbsolutePath();
                }

                Image userImage = new Image(ImageDataFactory.create(pathUserImage.toUri().toURL()));
                userImage.setWidth(100); // Ajusta el tamaño de la imagen si es necesario
                Cell imageCell = new Cell().add(userImage)
                        .setBorder(Border.NO_BORDER)
                        .setHorizontalAlignment(HorizontalAlignment.RIGHT)
                        .setVerticalAlignment(VerticalAlignment.MIDDLE);
                infoTable.addCell(imageCell);
            } catch (MalformedURLException e) {
                System.err.println("Error al cargar la imagen del usuario: " + e.getMessage());
            }

            document.add(infoTable);

            // Cerrar el documento
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return outputStream.toByteArray();
    }





    @Autowired
    private IUserService userService;

    public ByteArrayInputStream crearPdfUsuarios() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

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

        // Añadir título "Listado de Usuarios"
        document.add(new Paragraph("Listado de Usuarios").setBold().setFontSize(16).setMarginBottom(-10).setTextAlignment(TextAlignment.LEFT));

        // Añadir fecha y hora actuales
        String currentDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        document.add(new Paragraph("Fecha de Generación: " + currentDateTime).setFontSize(10).setTextAlignment(TextAlignment.RIGHT));

        List<User> usuarios = userService.getAll();

        // Crear y estilizar la tabla de usuarios
        float[] columnWidths = {40f, 110f, 110f, 165f, 80f, 50f};
        Table table = new Table(columnWidths);

        // Encabezados de la tabla con fondo morado y texto blanco
        Cell headerId = new Cell().add(new Paragraph("ID").setBold().setFontColor(ColorConstants.WHITE)).setBackgroundColor(new DeviceRgb(122, 89, 168));
        Cell headerNombre = new Cell().add(new Paragraph("Nombre").setBold().setFontColor(ColorConstants.WHITE)).setBackgroundColor(new DeviceRgb(122, 89, 168));
        Cell headerApellido = new Cell().add(new Paragraph("Apellido").setBold().setFontColor(ColorConstants.WHITE)).setBackgroundColor(new DeviceRgb(122, 89, 168));
        Cell headerEmail = new Cell().add(new Paragraph("Email").setBold().setFontColor(ColorConstants.WHITE)).setBackgroundColor(new DeviceRgb(122, 89, 168));
        Cell headerRol = new Cell().add(new Paragraph("Rol").setBold().setFontColor(ColorConstants.WHITE)).setBackgroundColor(new DeviceRgb(122, 89, 168));
        Cell headerEstado = new Cell().add(new Paragraph("Estado").setBold().setFontColor(ColorConstants.WHITE)).setBackgroundColor(new DeviceRgb(122, 89, 168));
        table.addHeaderCell(headerId);
        table.addHeaderCell(headerNombre);
        table.addHeaderCell(headerApellido);
        table.addHeaderCell(headerEmail);
        table.addHeaderCell(headerRol);
        table.addHeaderCell(headerEstado);

        // Filas de la tabla de usuarios
        for (User usuario : usuarios) {
            table.addCell(new Cell().add(new Paragraph(usuario.getId().toString())));
            table.addCell(new Cell().add(new Paragraph(usuario.getName())));
            table.addCell(new Cell().add(new Paragraph(usuario.getLastname())));
            table.addCell(new Cell().add(new Paragraph(usuario.getEmail())));
            table.addCell(new Cell().add(new Paragraph(usuario.getRol().getRolname())));
            table.addCell(new Cell().add(new Paragraph(usuario.getStatus() == 1 ? "Activo" : "Inactivo")));
        }

        document.add(table);
        document.close();

        return new ByteArrayInputStream(out.toByteArray());
    }





    public ByteArrayInputStream crearPdfUsuariosPorEstado(int estado) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Configuración del encabezado con logo y texto
        float[] columnWidthsHeader = {1, 4};
        Table headerTable = new Table(columnWidthsHeader);
        headerTable.setWidth(UnitValue.createPercentValue(50));

        // Añadir logo a la tabla
        String logoPath = "src/main/resources/static/img/sorvete.png"; // Ruta relativa al logo
        Path path = Paths.get(logoPath).toAbsolutePath(); // Obtener ruta absoluta

        try {
            Image logo = new Image(ImageDataFactory.create(path.toUri().toURL()));
            logo.setWidth(50); // Ajusta el tamaño del logo si es necesario
            Cell logoCell = new Cell().add(logo).setBorder(Border.NO_BORDER);
            headerTable.addCell(logoCell);
        } catch (MalformedURLException e) {
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

        // Título de acuerdo al estado de los usuarios
        String titulo = estado == 1 ? "Listado de Usuarios Activos" : "Listado de Usuarios Inactivos";
        document.add(new Paragraph(titulo).setBold().setFontSize(16).setMarginBottom(-10).setTextAlignment(TextAlignment.LEFT));

        // Añadir fecha y hora actuales
        String currentDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        document.add(new Paragraph("Fecha de Generación: " + currentDateTime).setFontSize(10).setTextAlignment(TextAlignment.RIGHT));

        // Lista de usuarios filtrados por estado
        List<User> usuarios = userService.getUsersByStatus(estado);

        // Crear la tabla de usuarios con el tamaño y estilo del segundo controlador
        float[] columnWidths = {40f, 110f, 110f, 165f, 80f, 50f};
        Table table = new Table(columnWidths);

        // Encabezados de la tabla con fondo morado y texto blanco
        Cell headerId = new Cell().add(new Paragraph("ID").setBold().setFontColor(ColorConstants.WHITE)).setBackgroundColor(new DeviceRgb(122, 89, 168));
        Cell headerNombre = new Cell().add(new Paragraph("Nombre").setBold().setFontColor(ColorConstants.WHITE)).setBackgroundColor(new DeviceRgb(122, 89, 168));
        Cell headerApellido = new Cell().add(new Paragraph("Apellido").setBold().setFontColor(ColorConstants.WHITE)).setBackgroundColor(new DeviceRgb(122, 89, 168));
        Cell headerEmail = new Cell().add(new Paragraph("Email").setBold().setFontColor(ColorConstants.WHITE)).setBackgroundColor(new DeviceRgb(122, 89, 168));
        Cell headerRol = new Cell().add(new Paragraph("Rol").setBold().setFontColor(ColorConstants.WHITE)).setBackgroundColor(new DeviceRgb(122, 89, 168));
        Cell headerEstado = new Cell().add(new Paragraph("Estado").setBold().setFontColor(ColorConstants.WHITE)).setBackgroundColor(new DeviceRgb(122, 89, 168));
        table.addHeaderCell(headerId);
        table.addHeaderCell(headerNombre);
        table.addHeaderCell(headerApellido);
        table.addHeaderCell(headerEmail);
        table.addHeaderCell(headerRol);
        table.addHeaderCell(headerEstado);

        // Filas de la tabla de usuarios
        for (User usuario : usuarios) {
            table.addCell(new Cell().add(new Paragraph(usuario.getId().toString())));
            table.addCell(new Cell().add(new Paragraph(usuario.getName())));
            table.addCell(new Cell().add(new Paragraph(usuario.getLastname())));
            table.addCell(new Cell().add(new Paragraph(usuario.getEmail())));
            table.addCell(new Cell().add(new Paragraph(usuario.getRol().getRolname())));
            table.addCell(new Cell().add(new Paragraph(usuario.getStatus() == 1 ? "Activo" : "Inactivo")));
        }

        document.add(table);
        document.close();

        return new ByteArrayInputStream(out.toByteArray());
    }





}