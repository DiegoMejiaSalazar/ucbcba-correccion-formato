package com.ucbcba.joel.ucbcorreccionformato.format_control.general_format;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ucbcba.joel.ucbcorreccionformato.format_control.general_format.format_error_response.BoundingRect;
import com.ucbcba.joel.ucbcorreccionformato.format_control.general_format.format_error_response.Comment;
import com.ucbcba.joel.ucbcorreccionformato.format_control.general_format.format_error_response.Content;
import com.ucbcba.joel.ucbcorreccionformato.format_control.general_format.format_error_response.FormatErrorResponse;
import com.ucbcba.joel.ucbcorreccionformato.format_control.general_format.format_error_response.Position;
import com.ucbcba.joel.ucbcorreccionformato.format_control.general_format.format_error_response.SpellCheckResponse;
import com.ucbcba.joel.ucbcorreccionformato.upload_download_file.service.FileStorageService;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class FormatErrorController {

        private final AtomicLong idHighlights = new AtomicLong();
        @Autowired
        private FileStorageService fileStorageService;

        @PostMapping("/api/hightlight/errors/{fileName:.+}")
        public List<FormatErrorResponse> getHightlightErrors(@PathVariable String fileName,
                        @RequestParam(value = "coverPage") Integer coverPage,
                        @RequestParam(value = "generalIndexStartPage") Integer generalIndexStartPage,
                        @RequestParam(value = "generalIndexEndPage") Integer generalIndexEndPage,
                        @RequestParam(value = "figureIndexStartPage") Integer figureIndexStartPage,
                        @RequestParam(value = "figureIndexEndPage") Integer figureIndexEndPage,
                        @RequestParam(value = "tableIndexStartPage") Integer tableIndexStartPage,
                        @RequestParam(value = "tableIndexEndPage") Integer tableIndexEndPage,
                        @RequestParam(value = "bibliographyStartPage") Integer bibliographyStartPage,
                        @RequestParam(value = "bibliographyEndPage") Integer bibliographyEndPage,
                        @RequestParam(value = "annexesStartPage") Integer annexesStartPage,
                        @RequestParam(value = "annexesEndPage") Integer annexesEndPage,
                        @RequestParam(value = "bibliograhyType") String bibliograhyType) {
                List<FormatErrorResponse> formatErrors = new ArrayList<>();
                Resource resource = fileStorageService.loadFileAsResource(fileName);
                Logger logger = Logger.getLogger(
                                "com.ucbcba.joel.ucbcorreccionformato.control_format_rules.general_format.FormatErrorController");
                try {
                        String dirPdfFile = resource.getFile().getAbsolutePath();
                        PDDocument pdfdocument = PDDocument.load(new File(dirPdfFile));
                        int contentFirstPage = getContentFirstPage(coverPage, generalIndexEndPage, figureIndexEndPage,
                                        tableIndexEndPage);
                        FormatErrorDetector formatErrorDetector = new FormatErrorDetector(pdfdocument, idHighlights);
                        // formatErrors.addAll(formatErrorDetector.getCoverPageFormatErrors(coverPage));
                        formatErrors.addAll(
                                        formatErrorDetector.getGeneralIndexFormatErrors(generalIndexStartPage,
                                                        generalIndexEndPage));
                        formatErrors
                                        .addAll(formatErrorDetector.getFigureIndexFormatErrors(figureIndexStartPage,
                                                        figureIndexEndPage));
                        formatErrors.addAll(formatErrorDetector.getTableIndexFormatErrors(tableIndexStartPage,
                                        tableIndexEndPage));
                        formatErrors.addAll(formatErrorDetector.getPageNumerationFormatErrors(contentFirstPage,
                                        annexesStartPage,
                                        annexesEndPage));
                        formatErrors.addAll(formatErrorDetector.getFigureTableFormatErrors(contentFirstPage,
                                        bibliographyStartPage));
                        formatErrors.addAll(formatErrorDetector.getEnglishWordsFormatErrors(contentFirstPage,
                                        bibliographyStartPage));
                        formatErrors.addAll(formatErrorDetector.getBibliographyFormatErrors(bibliographyStartPage,
                                        bibliographyEndPage, bibliograhyType));
                        formatErrors.addAll(getSpellCheckErrors(resource));
                        pdfdocument.close();
                } catch (IOException e) {
                        logger.log(Level.SEVERE, "No se pudo analziar el archivo PDF", e);
                }
                return formatErrors;
        }

        private List<FormatErrorResponse> getSpellCheckErrors(Resource resource)
                        throws IOException, NumberFormatException {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.MULTIPART_FORM_DATA);
                MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
                body.add("file", resource);
                String serverUrl = "http://127.0.0.1:5000/spell-check";
                RestTemplate restTemplate = new RestTemplate();
                HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity(body, headers);
                SpellCheckResponse response = restTemplate
                                .postForEntity(serverUrl, requestEntity, SpellCheckResponse.class)
                                .getBody();
                List<FormatErrorResponse> result = new ArrayList<>();
                response.get("errors").forEach(x -> {
                        LinkedHashMap<String, Object> spellError = x;
                        Comment comment = new Comment(
                                        "No se pudo encontrar la palabra " + x.get("text") + " no quiso decir:", "");
                        Content content = new Content(x.get("suggestions").toString());
                        boolean error = true;
                        float x1 = Float.parseFloat(
                                        (((LinkedHashMap<String, LinkedHashMap<String, String>>) x.get("position")))
                                                        .get("boundingRect").get("x1"));
                        float y1 = Float.parseFloat(
                                        (((LinkedHashMap<String, LinkedHashMap<String, String>>) x.get("position")))
                                                        .get("boundingRect").get("y1"));
                        float x2 = Float.parseFloat(
                                        (((LinkedHashMap<String, LinkedHashMap<String, String>>) x.get("position")))
                                                        .get("boundingRect").get("x2"));
                        float y2 = Float.parseFloat(
                                        (((LinkedHashMap<String, LinkedHashMap<String, String>>) x.get("position")))
                                                        .get("boundingRect").get("y2"));
                        float width = Float.parseFloat(
                                        (((LinkedHashMap<String, LinkedHashMap<String, String>>) x.get("position")))
                                                        .get("boundingRect").get("width"));
                        float height = Float.parseFloat(
                                        (((LinkedHashMap<String, LinkedHashMap<String, String>>) x.get("position")))
                                                        .get("boundingRect").get("height"));
                        List<BoundingRect> spellBoundingRectList = new ArrayList<>();
                        BoundingRect brect = new BoundingRect(x1, y1, x2, y2, width, height);
                        spellBoundingRectList.add(brect);
                        int pageNumber = ((LinkedHashMap<String, Integer>) x.get("position")).get("pageNumber");
                        Position position = new Position(brect, spellBoundingRectList, pageNumber);
                        result.add(new FormatErrorResponse(content, position, comment,
                                        String.valueOf(idHighlights.incrementAndGet()), error,
                                        x.get("type").toString()));
                });
                return result;
        }

        private int getContentFirstPage(Integer coverPage, Integer generalIndexPageEnd, Integer figureIndexPageEnd,
                        Integer tableIndexPageEnd) {
                int indexPageEnd;
                if (generalIndexPageEnd > figureIndexPageEnd) {
                        if (generalIndexPageEnd > tableIndexPageEnd) {
                                indexPageEnd = generalIndexPageEnd;
                        } else {
                                indexPageEnd = tableIndexPageEnd;
                        }
                } else if (figureIndexPageEnd > tableIndexPageEnd) {
                        indexPageEnd = figureIndexPageEnd;
                } else {
                        indexPageEnd = tableIndexPageEnd;
                }
                if (coverPage > indexPageEnd) {
                        return coverPage + 1;
                }
                return indexPageEnd + 1;
        }

}
