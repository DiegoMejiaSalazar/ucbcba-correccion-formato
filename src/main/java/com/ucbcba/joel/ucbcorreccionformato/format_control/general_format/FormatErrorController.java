package com.ucbcba.joel.ucbcorreccionformato.format_control.general_format;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.ser.std.StdKeySerializers.Default;
import com.ucbcba.joel.ucbcorreccionformato.format_control.general_format.format_error_response.BoundingRect;
import com.ucbcba.joel.ucbcorreccionformato.format_control.general_format.format_error_response.Comment;
import com.ucbcba.joel.ucbcorreccionformato.format_control.general_format.format_error_response.Content;
import com.ucbcba.joel.ucbcorreccionformato.format_control.general_format.format_error_response.FormatErrorResponse;
import com.ucbcba.joel.ucbcorreccionformato.format_control.general_format.format_error_response.Position;
import com.ucbcba.joel.ucbcorreccionformato.format_control.general_format.format_error_response.SpellCheckResponse;
import com.ucbcba.joel.ucbcorreccionformato.upload_download_file.service.FileStorageService;

import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;


@RestController
public class FormatErrorController {

        private final AtomicLong idHighlights = new AtomicLong();
        @Autowired
        private FileStorageService fileStorageService;

        @GetMapping(value="/api/ejemplo", produces=MediaType.APPLICATION_OCTET_STREAM_VALUE)
        public ResponseEntity<byte[]> download() throws IOException {
                try {
 
                        File file = ResourceUtils.getFile("paradescargar.docx");
                 
                        byte[] contents = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
                 
                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                        headers.setContentDisposition(ContentDisposition.builder("inline").name("paradescargar.docx").build());
                 
                        return new ResponseEntity<>(contents, headers, HttpStatus.OK);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
                    }
        }

        @GetMapping(value="/api/guiaucb", produces=MediaType.APPLICATION_OCTET_STREAM_VALUE)
        public @ResponseBody byte[] getPdf() throws IOException {
                InputStream in = getClass().getResourceAsStream("./guiaucb.pdf");
                return IOUtils.toByteArray(in);
        } 

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
                System.out.println("==================================================CALLING=========================");
                logger.warning("CALLING METHOD !!!!!");
                try {
                        String dirPdfFile = resource.getFile().getAbsolutePath();
                        PDDocument pdfdocument = PDDocument.load(new File(dirPdfFile));
                        int contentFirstPage = getContentFirstPage(coverPage, generalIndexEndPage, figureIndexEndPage,
                                        tableIndexEndPage);
                        FormatErrorDetector formatErrorDetector = new FormatErrorDetector(pdfdocument, idHighlights);
                        formatErrors.addAll(formatErrorDetector.getCoverPageFormatErrors(coverPage));
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
                        formatErrors.addAll(getSpellCheckErrors(resource, bibliographyStartPage, generalIndexEndPage, figureIndexEndPage, tableIndexEndPage));
                        pdfdocument.close();
                } catch (IOException e) {
                        logger.log(Level.SEVERE, "No se pudo analziar el archivo PDF", e);
                }
                return formatErrors;
        }

        private List<FormatErrorResponse> getSpellCheckErrors(Resource resource, Integer bibliographyStart,Integer generalIndexEndPage, Integer figureIndexEndPage, Integer tableIndexEndPage)
                        throws IOException, NumberFormatException {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.MULTIPART_FORM_DATA);
                MultiValueMap<String, Object> body = new LinkedMultiValueMap();
                body.add("file", resource);
                String serverUrl = "http://127.0.0.1:5000/spell-check?bibliographystart=" + bibliographyStart.toString() + "&figureIndexEndPage=" + figureIndexEndPage.toString() + "&generalIndexEndPage=" + generalIndexEndPage.toString() + "&tableIndexEndPage=" + tableIndexEndPage.toString();
                SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
                factory.setConnectTimeout(0);
                factory.setReadTimeout(0);
                factory.setBufferRequestBody(false);
                RestTemplate restTemplate = new RestTemplate(factory);
                HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<MultiValueMap<String, Object>>(body, headers);
                SpellCheckResponse response = restTemplate
                                .postForEntity(serverUrl, requestEntity, SpellCheckResponse.class)
                                .getBody();
                List<FormatErrorResponse> result = new ArrayList<>();
                response.get("errors").forEach(x -> {
                        String errorDescription = "";
                        if (x.get("type").equals("ortografia")) {
                                errorDescription = "No se pudo encontrar la palabra " + x.get("text")
                                                + " no quiso decir:";
                        } else {
                                errorDescription = "La palabra " + x.get("text")
                                                + " no concuerda con el contexto, no quiso decir: ";
                        }
                        Comment comment = new Comment(errorDescription, "");
                        Content content = new Content(x.get("suggestions").toString());
                        boolean error = true;
                        LinkedHashMap<String, LinkedHashMap<String, Double>> positions = (LinkedHashMap<String, LinkedHashMap<String, Double>>) x.get("position");
                        LinkedHashMap<String, Double> boundingrect = positions.get("boundingRect");
                        double x1 = boundingrect.get("x1");
                        double y1 = boundingrect.get("y1");
                        double x2 = boundingrect.get("x2");
                        double y2 = boundingrect.get("y2");
                        int width = 612;
                        int height = 792;
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
