package pe.edu.utp.pdf;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.contentstream.PDFGraphicsStreamEngine;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.*;

/** Personaliza la imagen de cabecera del PDF de Facturación Directa, conservando su contenido. */
public final class LogoBoletaPdf {
    private LogoBoletaPdf() { }

    public static byte[] aplicar(byte[] original,byte[] logo) throws IOException {
        BufferedImage marca=ImageIO.read(new ByteArrayInputStream(logo));
        if (marca==null) throw new IOException("Logo no disponible.");
        try (PDDocument pdf=Loader.loadPDF(original); ByteArrayOutputStream output=new ByteArrayOutputStream()) {
            if (pdf.isEncrypted() || pdf.getNumberOfPages()==0 || pdf.getNumberOfPages()>10)
                throw new IOException("Formato de boleta no compatible.");
            int cambios=0;
            for (PDPage page:pdf.getPages()) {
                Cabecera scan=new Cabecera(page);
                scan.processPage(page);
                for (Imagen imagen:scan.imagenes) {
                    BufferedImage ajustada=encajar(marca,imagen.ancho/imagen.alto);
                    try { imagen.recursos.put(imagen.nombre,LosslessFactory.createFromImage(pdf,ajustada)); }
                    finally { ajustada.flush(); }
                    cambios++;
                }
            }
            if (cambios==0) throw new IOException("No se encontró el logo de cabecera en la boleta.");
            pdf.getDocumentInformation().setProducer("ArquiDent / Apache PDFBox");
            pdf.save(output);
            return output.toByteArray();
        } finally { marca.flush(); }
    }
    private static BufferedImage encajar(BufferedImage logo,double aspecto) {
        int ancho=800,alto=(int)Math.round(ancho/aspecto);
        BufferedImage imagen=new BufferedImage(ancho,alto,BufferedImage.TYPE_INT_RGB);
        Graphics2D g=imagen.createGraphics();
        try {
            g.setColor(Color.WHITE); g.fillRect(0,0,ancho,alto);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            double escala=Math.min((double)ancho/logo.getWidth(),(double)alto/logo.getHeight());
            int w=(int)Math.round(logo.getWidth()*escala),h=(int)Math.round(logo.getHeight()*escala);
            g.drawImage(logo,(ancho-w)/2,(alto-h)/2,w,h,null);
        } finally { g.dispose(); }
        return imagen;
    }
    private static final class Imagen {
        final PDResources recursos; final COSName nombre; final double ancho,alto;
        Imagen(PDResources recursos,COSName nombre,Rectangle2D area) {
            this.recursos=recursos; this.nombre=nombre; this.ancho=area.getWidth(); this.alto=area.getHeight();
        }
    }
    private static final class Cabecera extends PDFGraphicsStreamEngine {
        final List<Imagen> imagenes=new ArrayList<>();
        private final PDRectangle pagina;
        private final GeneralPath path=new GeneralPath();
        Cabecera(PDPage page) { super(page); pagina=page.getCropBox(); }
        @Override public void drawImage(PDImage image) throws IOException {
            if (!(image instanceof PDImageXObject)) return;
            Rectangle2D area=getGraphicsState().getCurrentTransformationMatrix().createAffineTransform()
                    .createTransformedShape(new Rectangle2D.Double(0,0,1,1)).getBounds2D();
            // La plantilla del proveedor tiene el logo rectangular arriba a la derecha.
            // El QR cuadrado del pie queda fuera de esta selección.
            if (area.getMinX()<pagina.getLowerLeftX()+pagina.getWidth()*.60
                    || area.getMinY()<pagina.getLowerLeftY()+pagina.getHeight()*.80
                    || area.getMaxX()>pagina.getUpperRightX()+1 || area.getMaxY()>pagina.getUpperRightY()+1
                    || area.getWidth()<pagina.getWidth()*.10 || area.getHeight()<pagina.getHeight()*.04
                    || area.getWidth()<area.getHeight()*1.15 || area.getWidth()>area.getHeight()*5) return;
            PDResources resources=getResources();
            for (COSName name:resources.getXObjectNames()) {
                if (resources.getXObject(name).getCOSObject()==((PDImageXObject)image).getCOSObject()) {
                    imagenes.add(new Imagen(resources,name,area)); return;
                }
            }
        }
        @Override public void appendRectangle(Point2D a,Point2D b,Point2D c,Point2D d) {
            path.moveTo(a.getX(),a.getY()); path.lineTo(b.getX(),b.getY());
            path.lineTo(c.getX(),c.getY()); path.lineTo(d.getX(),d.getY()); path.closePath();
        }
        @Override public void clip(int windingRule) { path.setWindingRule(windingRule); getGraphicsState().intersectClippingPath(path); }
        @Override public void moveTo(float x,float y) { path.moveTo(x,y); }
        @Override public void lineTo(float x,float y) { path.lineTo(x,y); }
        @Override public void curveTo(float x1,float y1,float x2,float y2,float x3,float y3) { path.curveTo(x1,y1,x2,y2,x3,y3); }
        @Override public Point2D getCurrentPoint() { return path.getCurrentPoint(); }
        @Override public void closePath() { path.closePath(); }
        @Override public void endPath() { path.reset(); }
        @Override public void strokePath() { path.reset(); }
        @Override public void fillPath(int windingRule) { path.reset(); }
        @Override public void fillAndStrokePath(int windingRule) { path.reset(); }
        @Override public void shadingFill(COSName shadingName) { }
    }
}
