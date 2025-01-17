package com.ucbcba.joel.ucbcorreccionformato.format_control.general_format.page_format_rules;

import com.ucbcba.joel.ucbcorreccionformato.format_control.WordLine;
import com.ucbcba.joel.ucbcorreccionformato.format_control.general_format.control_format_rules.Format;
import com.ucbcba.joel.ucbcorreccionformato.format_control.general_format.control_format_rules.PageFormat;
import com.ucbcba.joel.ucbcorreccionformato.format_control.general_format.format_error_response.FormatErrorResponse;
import com.ucbcba.joel.ucbcorreccionformato.format_control.GetterWordLines;
import com.ucbcba.joel.ucbcorreccionformato.format_control.general_format.ReportFormatError;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class NumerationPageFormat implements PageFormatRule {
    private PDDocument pdfdocument;
    private AtomicLong idHighlights;
    private int correctPageNumeration;

    public NumerationPageFormat(PDDocument pdfdocument, AtomicLong idHighlights, int correctPageNumeration) {
        this.pdfdocument = pdfdocument;
        this.idHighlights = idHighlights;
        this.correctPageNumeration = correctPageNumeration;
    }

    @Override
    public List<FormatErrorResponse> getFormatErrors(int page) throws IOException {
        List<FormatErrorResponse> formatErrors = new ArrayList<>();
        float pageWidth = pdfdocument.getPage(page-1).getMediaBox().getWidth();
        float pageHeight = pdfdocument.getPage(page-1).getMediaBox().getHeight();

        Format numerationFormat = new PageFormat(12,"Derecho",pageWidth,correctPageNumeration,false,false);

        GetterWordLines getterWordLines = new GetterWordLines(pdfdocument);
        WordLine pageNumerationPage = getterWordLines.getPageNumeration(page);

        if(pageNumerationPage!=null){
            List<String> comments = numerationFormat.getFormatErrorComments(pageNumerationPage);
            reportFormatErrors(comments, pageNumerationPage, formatErrors, pageWidth, pageHeight, page);
        }

        return formatErrors;
    }

    private void reportFormatErrors(List<String> comments, WordLine words, List<FormatErrorResponse> formatErrors, float pageWidth, float pageHeight, int page) {
        if (!comments.isEmpty()) {
            ReportFormatError reporter = new ReportFormatError(idHighlights);
            formatErrors.add(reporter.reportFormatError(comments, words, pageWidth, pageHeight, page,"numeracion"));
        }
    }

}
