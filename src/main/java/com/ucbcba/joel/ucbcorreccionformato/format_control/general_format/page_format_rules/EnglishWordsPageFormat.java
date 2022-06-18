package com.ucbcba.joel.ucbcorreccionformato.format_control.general_format.page_format_rules;

import com.ucbcba.joel.ucbcorreccionformato.format_control.WordLine;
import com.ucbcba.joel.ucbcorreccionformato.format_control.general_format.others.dictionaries.Diccionario;
import com.ucbcba.joel.ucbcorreccionformato.format_control.general_format.others.dictionaries.Dictionary;
import com.ucbcba.joel.ucbcorreccionformato.format_control.general_format.control_format_rules.EnglishWordFormat;
import com.ucbcba.joel.ucbcorreccionformato.format_control.general_format.control_format_rules.Format;
import com.ucbcba.joel.ucbcorreccionformato.format_control.general_format.format_error_response.FormatErrorResponse;
import com.ucbcba.joel.ucbcorreccionformato.format_control.general_format.ReportFormatError;
import com.ucbcba.joel.ucbcorreccionformato.format_control.SingleLine;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EnglishWordsPageFormat implements PageFormatRule {

    private PDDocument pdfdocument;
    private AtomicLong idHighlights;
    private Dictionary dictionary;
    private Diccionario diccionario;
    private static final List<String> wordsToIgnore = List.of("homofonía",
    "homógrafas",
    "homófonas",
    "heterógrafas",
    "heterógrafas",
    "objetivables",
    "homonía",
    "weight",
    "Rof",
    "softmax",
    "reLU",
    "sigmoide",
    "retropropagación",
    "Hunspell",
    "Pyspellchecker",
    "boundingrect",
    "DeepAI",
    "grafemas",
    "homófonos"
    );

    public EnglishWordsPageFormat(PDDocument pdfdocument, AtomicLong idHighlights){
        this.pdfdocument = pdfdocument;
        this.idHighlights = idHighlights;
        Logger LOGGER = Logger.getLogger("com.ucbcba.joel.ucbcorreccionformato.control_format_rules.general_format.page_format_rules.EnglishWordsPageFormat");
        try {
            this.dictionary = new Dictionary();
            this.diccionario = new Diccionario();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,"No se pudo abrir los diccionarios de texto", e);
        }
    }

    @Override
    public List<FormatErrorResponse> getFormatErrors(int page) throws IOException {
        List<FormatErrorResponse> formatErrors = new ArrayList<>();
        float pageWidth = pdfdocument.getPage(page-1).getMediaBox().getWidth();
        float pageHeight = pdfdocument.getPage(page-1).getMediaBox().getHeight();
        Format englishWordFormat = new EnglishWordFormat(12,true);
        PDFTextStripper stripper = new PDFTextStripper() {
            @Override
            protected void writeString(String string, List<TextPosition> textPositions) throws IOException {
                String wordSeparator = getWordSeparator();
                List<TextPosition> word = new ArrayList<>();
                for (TextPosition text : textPositions) {
                    String currentChar = text.getUnicode();
                    if (currentChar != null && currentChar.length() >= 1) {
                        if (!currentChar.equals(wordSeparator)) {
                            word.add(text);
                        } else if (!word.isEmpty()) {
                            if(isEnglishWord(word)){
                                SingleLine englihWord =new SingleLine(word);
                                List<SingleLine> singleLines = new ArrayList<>();
                                singleLines.add(englihWord);
                                WordLine wordLine = new WordLine(singleLines);
                                List<String> formatErrorscomments = englishWordFormat.getFormatErrorComments(wordLine);
                                reportFormatErrors(formatErrorscomments, wordLine, formatErrors, pageWidth, pageHeight, page);
                            }
                            word.clear();
                        }
                    }
                }
                if (!word.isEmpty()) {
                    if(isEnglishWord(word)){
                        SingleLine englihWord =new SingleLine(word);
                        List<SingleLine> singleLines = new ArrayList<>();
                        singleLines.add(englihWord);
                        WordLine wordLine = new WordLine(singleLines);
                        List<String> formatErrorscomments = englishWordFormat.getFormatErrorComments(wordLine);
                        reportFormatErrors(formatErrorscomments, wordLine, formatErrors, pageWidth, pageHeight, page);
                    }
                    word.clear();
                }
                super.writeString(string, textPositions);
            }
        };
        stripper.setSortByPosition(true);
        stripper.setStartPage(page);
        stripper.setEndPage(page);
        stripper.getText(pdfdocument);
        return formatErrors;
    }

    private void reportFormatErrors(List<String> comments, WordLine words, List<FormatErrorResponse> formatErrors, float pageWidth, float pageHeight, int page) {
        if (!comments.isEmpty()) {
            ReportFormatError reporter = new ReportFormatError(idHighlights);
            formatErrors.add(reporter.reportFormatWarning(comments, words, pageWidth, pageHeight, page,"extranjerismo"));
        }
    }


    private boolean isEnglishWord(List<TextPosition> word) {
        boolean resp = false;
        StringBuilder builder = new StringBuilder();
        for (TextPosition text : word) {
            builder.append(text.getUnicode());
        }
        String  result = builder.toString().replaceAll("[^\\w\\sáéíóúAÉÍÓÚÑñ]","");
        result = result.toLowerCase();
        if (result.length() > 2 && dictionary.contains(result) && !diccionario.contains(result) && !isPluralSpanishWord(result) && !EnglishWordsPageFormat.wordsToIgnore.contains(result)){
            resp = true;
        }
        return resp;
    }

    private boolean isPluralSpanishWord(String word){
        boolean resp = false;
        if(isVowel(word.charAt(word.length()-2)) && word.charAt(word.length()-1) == 's'){
            if(word.charAt(word.length()-2) == 'e' && diccionario.contains(removeLastTwoChars(word))){
                resp = true;
            }
            if (diccionario.contains(removeLastChar(word))){
                resp = true;
            }
        }
        return resp;
    }

    private boolean isVowel(char c) {
        return "AEIOUaeiou".indexOf(c) != -1;
    }

    private String removeLastTwoChars(String s) {
        if (s == null || s.length() == 0) {
            return s;
        }
        return s.substring(0, s.length()-2);
    }

    private String removeLastChar(String s) {
        if (s == null || s.length() == 0) {
            return s;
        }
        return s.substring(0, s.length()-1);
    }

}
