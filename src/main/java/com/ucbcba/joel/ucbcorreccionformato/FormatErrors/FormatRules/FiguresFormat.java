package com.ucbcba.joel.ucbcorreccionformato.FormatErrors.FormatRules;

import com.ucbcba.joel.ucbcorreccionformato.FormatErrors.HighlightsReport.*;
import com.ucbcba.joel.ucbcorreccionformato.FormatErrors.ImagesOnPdf.ImageLocations;
import com.ucbcba.joel.ucbcorreccionformato.FormatErrors.ImagesOnPdf.PdfImage;
import com.ucbcba.joel.ucbcorreccionformato.General.GeneralSeeker;
import com.ucbcba.joel.ucbcorreccionformato.General.GetterWordLines;
import com.ucbcba.joel.ucbcorreccionformato.General.ReportFormatError;
import com.ucbcba.joel.ucbcorreccionformato.General.WordsProperties;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.util.Matrix;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class FiguresFormat implements FormatRule {

    private PDDocument pdfdocument;
    private GeneralSeeker seeker;
    private PDPage page;
    private AtomicLong idHighlights;

    public FiguresFormat(PDDocument pdfdocument, AtomicLong counter, PDPage page){
        this.pdfdocument = pdfdocument;
        this.seeker = new GeneralSeeker(pdfdocument);
        this.page = page;
        this.idHighlights = counter;
    }

    @Override
    public List<FormatErrorReport> getFormatErrors(int pageNum) throws IOException {

        float pageWidth = pdfdocument.getPage(pageNum-1).getMediaBox().getWidth();
        float pageHeight = pdfdocument.getPage(pageNum-1).getMediaBox().getHeight();

        List<FormatErrorReport> formatErrors = new ArrayList<>();
        final List<PdfImage> pdfImages = getPdfImages(pageNum, pageHeight);

        for (PdfImage image : pdfImages) {
            List<String> commentsFigureError = new ArrayList<>();
            List<String> commentsFigureWarnings = new ArrayList<>();
            if (!image.isFigureHorizontal()) {
                commentsFigureError.add("Figura vertical (Usar orientación horizontal en la presente hoja)");
                if(image.doesFigureRotateToTheRight()){
                    image.setY(image.getEndY());
                    image.setEndX(image.getX()+image.getHeightDisplayed());
                    image.setEndY(image.getY()+image.getWidthDisplayed());
                } else{
                    image.setX(image.getX()+image.getShearX());
                    image.setY(pageHeight - (image.getTranslateY() + image.getWidthDisplayed()));
                    image.setEndX(image.getX()+image.getHeightDisplayed());
                    image.setEndY(image.getY()+image.getWidthDisplayed());
                }
            }

            if(!hasTittleTheFigure(image, pageNum)){
                commentsFigureWarnings.add("La figura tenga su título");
            }
            if (!hasSourceTheFigure (image, pageNum)) {
                commentsFigureWarnings.add("La figura tenga su fuente");
            }
            reportFigureWarnings(formatErrors, image, commentsFigureWarnings, pageWidth, pageHeight, pageNum);
            reportFigureErrors(formatErrors, image, commentsFigureError, pageWidth, pageHeight, pageNum);
        }

        return formatErrors;
    }

    private List<PdfImage> getPdfImages(int pageNum, float pageHeight) throws IOException {
        final List<PdfImage> pdfImages = new ArrayList<PdfImage>();
        ImageLocations imageLocations = new ImageLocations(){
            @Override
            protected void processOperator(Operator operator, List<COSBase> operands) throws IOException
            {
                String operation = operator.getName();
                if( "Do".equals(operation) )
                {
                    COSName objectName = (COSName) operands.get( 0 );
                    PDXObject xobject = getResources().getXObject( objectName );
                    if( xobject instanceof PDImageXObject)
                    {
                        PDImageXObject image = (PDImageXObject)xobject;
                        Matrix ctmNew = getGraphicsState().getCurrentTransformationMatrix();
                        pdfImages.add(new PdfImage(image,ctmNew,pageNum,pageHeight));
                    }
                    else if(xobject instanceof PDFormXObject)
                    {
                        PDFormXObject form = (PDFormXObject)xobject;
                        showForm(form);
                    }
                }
                else
                {
                    super.processOperator( operator, operands);
                }
            }
        };
        imageLocations.processPage(page);

        List<PdfImage> pdfFigures = new ArrayList<PdfImage>();
        for (PdfImage image : pdfImages) {
            if (image.getWidthDisplayed() > 100 && image.getHeightDisplayed() > 50) {
                pdfFigures.add(image);
            }
        }

        Collections.sort(pdfFigures, new Comparator<PdfImage>() {
            @Override
            public int compare(PdfImage pdfImage1, PdfImage pdfImage2)
            {
                return (int) (pdfImage1.getY() - pdfImage2.getY());
            }
        });
        return pdfFigures;
    }

    private boolean hasTittleTheFigure(PdfImage image, int pageNum) throws IOException {
        boolean resp = false;
        GetterWordLines getterWordLines = new GetterWordLines(pdfdocument);
        List<WordsProperties> wordsLines = getterWordLines.getWordLines(pageNum);

        for(WordsProperties wordLine:wordsLines){
            String arr[] = wordLine.toString().split(" ", 2);
            String firstWordLine = arr[0];
            if (firstWordLine.contains("Figura")){
                if ((image.getEndY() > wordLine.getY()) && (image.getY() - 200 < wordLine.getY())) {
                    return true;
                }
            }
        }
        return resp;
    }

    private boolean hasSourceTheFigure(PdfImage image, int pageNum) throws IOException {
        boolean resp = false;
        GetterWordLines getterWordLines = new GetterWordLines(pdfdocument);
        List<WordsProperties> wordsLines = getterWordLines.getWordLines(pageNum);

        for(WordsProperties wordLine:wordsLines){
            String arr[] = wordLine.toString().split(" ", 2);
            String firstWordLine = arr[0];
            if (firstWordLine.contains("Fuente:")){
                if ((image.getY() < wordLine.getY()) && (image.getEndY() + 100 > wordLine.getY())) {
                    return true;
                }
            }
        }
        return resp;
    }


    private void reportFigureWarnings(List<FormatErrorReport> formatErrors, PdfImage image, List<String> commentsFigure,float pageWidth, float pageHeight, int page) {
        if (commentsFigure.size() != 0) {
            formatErrors.add(new ReportFormatError(idHighlights).reportFigureFormatWarning(commentsFigure, image, pageWidth, pageHeight, page));
        }
    }

    private void reportFigureErrors(List<FormatErrorReport> formatErrors, PdfImage image, List<String> commentsFigure,float pageWidth, float pageHeight, int page) {
        if (commentsFigure.size() != 0) {
            formatErrors.add(new ReportFormatError(idHighlights).reportFigureFormatError(commentsFigure, image, pageWidth, pageHeight, page));
        }
    }




}