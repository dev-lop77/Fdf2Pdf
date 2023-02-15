import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.FdfReader;
import com.itextpdf.text.pdf.GrayColor;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSmartCopy;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Fdf2Pdf extends java.lang.Object {

    public static void usage() {
        System.out.println("Fdf2Pdf");
        System.out.println("usage: Fdf2Pdf pdf1 fdf1 [pdf2 fdf2 ...] pdf editable:0|1 preview:0|1 modify:0|1");
    }
    
    public void createPdf(String args[]){
        int idx = 0;
        int n_outs = 4;
        boolean editable = args[args.length - 3].equals("1");
        boolean preview = args[args.length - 2].equals("1");
        boolean modify = args[args.length - 1].equals("1");
        
        int tot = (args.length - n_outs) / 2;
        String in_pdf = args[idx];
        String in_fdf;// = args[idx+1];
        String out_pdf = args[args.length-n_outs];
        
        PdfReader reader;
        FdfReader fdf;
        PdfStamper stamp;
        PdfContentByte over;
        
        Font FONT = new Font(Font.FontFamily.HELVETICA, 100, Font.BOLD, new GrayColor(0.85f));
        BaseFont FIELDFONT = null;
        try {
            FIELDFONT = BaseFont.createFont("fileldsfont.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        } catch (DocumentException | IOException ex) {
            Logger.getLogger(Fdf2Pdf.class.getName()).log(Level.SEVERE, null, ex);
        }

        ByteArrayOutputStream os;
        
        try{
            reader = new PdfReader(in_pdf);
            Document document = new Document(reader.getPageSizeWithRotation(1));            
            PdfSmartCopy writer = new PdfSmartCopy(document, new FileOutputStream(out_pdf));
            if(!modify){
                writer.setEncryption("".getBytes(),"doNot-@Mod1fy;".getBytes(),PdfWriter.ALLOW_PRINTING | PdfWriter.ALLOW_FILL_IN,PdfWriter.ENCRYPTION_AES_128 | PdfWriter.DO_NOT_ENCRYPT_METADATA);            
            }
            document.open();
            
            for(int n = 0; n < tot; n++){                
                in_pdf = args[idx+n*2];
                in_fdf = args[idx+1+n*2];
                
                try{
                    os = new ByteArrayOutputStream();
                    if(n > 0){
                        reader = new PdfReader(in_pdf);
                    }
                    stamp = new PdfStamper(reader, os);

                    fdf = new FdfReader(in_fdf);
                    stamp.getAcroFields().addSubstitutionFont(FIELDFONT);
                    stamp.getAcroFields().setFields(fdf);                    

                    if(!editable){
                        stamp.setFormFlattening(true);
                    }
                    if(preview){
                        for (int i = 1; i <= reader.getNumberOfPages(); i++) {
                            over = stamp.getUnderContent(i);
                            ColumnText.showTextAligned(over,
                                Element.ALIGN_CENTER & Element.ALIGN_MIDDLE, new Phrase("PREVIEW", FONT),
                                297.5f, 421, -45);
                        }
                    }
                    stamp.close();
                    
                    reader = new PdfReader(os.toByteArray());
                    for (int i = 1; i <= reader.getNumberOfPages(); i++) {
                        writer.addPage(writer.getImportedPage(reader, i));
                    }
                }
                catch (DocumentException | IOException e){
                    
                }
            }
            
            document.close();
        }
        catch (DocumentException | IOException e){
        }
    }
    
    public static void main(String args[]) {
        if (args.length < 5) {
            usage();
            return;
        }
        
        //long start = System.currentTimeMillis();
        new Fdf2Pdf().createPdf(args);        
        //System.out.println("Fdf2Pdf execution time : " + (System.currentTimeMillis() - start));
    }
   
}