package pe.edu.utp.pdf;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.*;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;

public class LogoBoletaPdfTest {
    private byte[] boleta(boolean cabecera) throws IOException {
        try (PDDocument doc=new PDDocument(); ByteArrayOutputStream out=new ByteArrayOutputStream()) {
            PDPage page=new PDPage(PDRectangle.A4); doc.addPage(page);
            BufferedImage logo=new BufferedImage(400,200,BufferedImage.TYPE_INT_RGB);
            Graphics2D g=logo.createGraphics(); g.setColor(Color.RED); g.fillRect(0,0,400,200); g.dispose();
            BufferedImage qr=new BufferedImage(30,30,BufferedImage.TYPE_INT_RGB);
            for(int x=0;x<30;x++) for(int y=0;y<30;y++) qr.setRGB(x,y,(x+y)%2==0?Color.BLACK.getRGB():Color.WHITE.getRGB());
            try (PDPageContentStream stream=new PDPageContentStream(doc,page)) {
                stream.beginText(); stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA),12);
                stream.newLineAtOffset(30,790); stream.showText("BOLETA BA26-100001 / RUC 20604051984 / TOTAL S/ 50.00"); stream.endText();
                if(cabecera) stream.drawImage(LosslessFactory.createFromImage(doc,logo),400,715,160,85);
                stream.drawImage(LosslessFactory.createFromImage(doc,qr),240,40,90,90);
            }
            doc.save(out); return out.toByteArray();
        }
    }
    @Test public void reemplazaLogoConservandoTextoImportesYQr() throws Exception {
        byte[] original=boleta(true),copia=original.clone();
        byte[] logo=Files.readAllBytes(Paths.get("src/main/webapp/images/arquident-logo.png"));
        byte[] nuevo=LogoBoletaPdf.aplicar(original,logo);
        assertArrayEquals(copia,original);
        try (PDDocument a=Loader.loadPDF(original); PDDocument b=Loader.loadPDF(nuevo)) {
            assertEquals(a.getNumberOfPages(),b.getNumberOfPages());
            assertEquals(new PDFTextStripper().getText(a),new PDFTextStripper().getText(b));
            BufferedImage antes=new PDFRenderer(a).renderImageWithDPI(0,72);
            BufferedImage despues=new PDFRenderer(b).renderImageWithDPI(0,72);
            int[] qrAntes=antes.getRGB(230,700,120,120,null,0,120);
            int[] qrDespues=despues.getRGB(230,700,120,120,null,0,120);
            assertArrayEquals("El QR no debe alterarse",qrAntes,qrDespues);
            assertFalse("Debe reemplazarse el logo de cabecera",Arrays.equals(
                    antes.getRGB(400,42,160,85,null,0,160),despues.getRGB(400,42,160,85,null,0,160)));
            assertEquals("ArquiDent / Apache PDFBox",b.getDocumentInformation().getProducer());
        }
    }
    @Test public void noConfundeElQrConElLogo() throws Exception {
        byte[] logo=Files.readAllBytes(Paths.get("src/main/webapp/images/arquident-logo.png"));
        try { LogoBoletaPdf.aplicar(boleta(false),logo); fail("No debe sustituir otra imagen si falta el logo de cabecera"); }
        catch(IOException esperado) { assertTrue(esperado.getMessage().contains("cabecera")); }
    }
}
