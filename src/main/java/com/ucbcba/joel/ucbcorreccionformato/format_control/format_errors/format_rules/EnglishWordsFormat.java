package com.ucbcba.joel.ucbcorreccionformato.format_control.format_errors.format_rules;

import com.ucbcba.joel.ucbcorreccionformato.format_control.format_errors.others.dictionaries.Diccionario;
import com.ucbcba.joel.ucbcorreccionformato.format_control.format_errors.others.dictionaries.Dictionary;
import com.ucbcba.joel.ucbcorreccionformato.format_control.format_errors.format_control.EnglishWordFormat;
import com.ucbcba.joel.ucbcorreccionformato.format_control.format_errors.format_control.Format;
import com.ucbcba.joel.ucbcorreccionformato.format_control.format_errors.format_error_response.FormatErrorResponse;
import com.ucbcba.joel.ucbcorreccionformato.format_control.format_errors.ReportFormatError;
import com.ucbcba.joel.ucbcorreccionformato.format_control.WordsProperties;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class EnglishWordsFormat implements FormatRule {

    private PDDocument pdfdocument;
    private AtomicLong idHighlights;
    private Dictionary dictionary;
    private Diccionario diccionario;

    public EnglishWordsFormat(PDDocument pdfdocument, AtomicLong idHighlights){
        this.pdfdocument = pdfdocument;
        this.idHighlights = idHighlights;
        try {
            this.dictionary = new Dictionary();
            this.diccionario = new Diccionario();
        } catch (IOException e) {
            e.printStackTrace();
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
                    String thisChar = text.getUnicode();
                    if (thisChar != null) {
                        if (thisChar.length() >= 1) {
                            if (!thisChar.equals(wordSeparator)) {
                                word.add(text);
                            } else if (!word.isEmpty()) {
                                if(isEnglishWord(word)){
                                    WordsProperties englihWord =new WordsProperties(word);
                                    List<String> formatErrorscomments = englishWordFormat.getFormatErrorComments(englihWord);
                                    reportFormatErrors(formatErrorscomments, englihWord, formatErrors, pageWidth, pageHeight, page);
                                }
                                word.clear();
                            }
                        }
                    }
                }
                if (!word.isEmpty()) {
                    if(isEnglishWord(word)){
                        WordsProperties englihWord =new WordsProperties(word);
                        List<String> formatErrorscomments = englishWordFormat.getFormatErrorComments(englihWord);
                        reportFormatErrors(formatErrorscomments, englihWord, formatErrors, pageWidth, pageHeight, page);
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

    private void reportFormatErrors(List<String> comments, WordsProperties words, List<FormatErrorResponse> formatErrors, float pageWidth, float pageHeight, int page) {
        if (!comments.isEmpty()) {
            formatErrors.add(new ReportFormatError(idHighlights).reportFormatWarning(comments, words, pageWidth, pageHeight, page,"extranjerismo"));
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
        if (result.length() > 2 && dictionary.contains(result) && !diccionario.contains(result) && !isPluralSpanishWord(result)){
            resp = true;
        }
        return resp;
    }

    private boolean isPluralSpanishWord(String word){
        boolean resp = false;
        if(isVowel(word.charAt(word.length()-2)) && word.charAt(word.length()-1) == 's'){
            if(word.charAt(word.length()-2) == 'e'){
                if (diccionario.contains(removeLastTwoChars(word))){
                    resp = true;
                }
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