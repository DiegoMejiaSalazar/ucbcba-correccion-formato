package com.ucbcba.joel.ucbcorreccionformato.page_calibration;

import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.IOException;

public class PdfDocumentDetector {
    private PDDocument pdfdocument;
    private int coverPage;
    private int generalIndexStartPage;
    private int generalIndexEndPage;
    private int figureIndexStartPage;
    private int figureIndexEndPage;
    private int tableIndexStartPage;
    private int tableIndexEndPage;
    private int bibliographyStartPage;
    private int bibliographyEndPage;
    private int annexesStartPage;
    private int annexesEndPage;

    public PdfDocumentDetector(PDDocument pdfdocument) throws IOException {
        this.pdfdocument = pdfdocument;
        PagesFinder pagesFinder = new PagesFinder(pdfdocument);
        this.coverPage = pagesFinder.getCoverPage();
        this.generalIndexStartPage = pagesFinder.getGeneralIndexStartPage();
        int lastIndexPage = pagesFinder.getLastIndexPage(generalIndexStartPage);
        this.generalIndexEndPage = pagesFinder.getGeneralIndexEndPage(generalIndexStartPage,lastIndexPage);
        this.figureIndexStartPage = pagesFinder.getFigureIndexStartPage(generalIndexEndPage,lastIndexPage);
        this.figureIndexEndPage = pagesFinder.getFigureIndexEndPage(figureIndexStartPage,lastIndexPage);
        this.tableIndexStartPage = pagesFinder.getTableIndexStartPage(generalIndexEndPage,lastIndexPage);
        this.tableIndexEndPage = pagesFinder.getTableIndexEndPage(tableIndexStartPage,lastIndexPage);
        this.bibliographyStartPage = pagesFinder.getBibliographyStartPage();
        this.annexesStartPage = pagesFinder.getAnnexesStartPage(bibliographyStartPage);
        this.bibliographyEndPage = pagesFinder.getBibliographyEndPage(bibliographyStartPage, annexesStartPage);
        this.annexesEndPage = pagesFinder.getAnnexesEndPage(annexesStartPage);
    }

    public PdfDocumentResponse getPdfPages(){
        return new PdfDocumentResponse(coverPage,generalIndexStartPage,generalIndexEndPage,figureIndexStartPage,figureIndexEndPage,
                tableIndexStartPage,tableIndexEndPage,bibliographyStartPage,bibliographyEndPage,annexesStartPage,annexesEndPage);
    }

    public int getCoverPage() {
        return coverPage;
    }

    public void setCoverPage(int coverPage) {
        this.coverPage = coverPage;
    }

    public int getGeneralIndexStartPage() {
        return generalIndexStartPage;
    }

    public void setGeneralIndexStartPage(int generalIndexStartPage) {
        this.generalIndexStartPage = generalIndexStartPage;
    }

    public int getGeneralIndexEndPage() {
        return generalIndexEndPage;
    }

    public void setGeneralIndexEndPage(int generalIndexEndPage) {
        this.generalIndexEndPage = generalIndexEndPage;
    }

    public int getAnnexesStartPage() {
        return annexesStartPage;
    }

    public void setAnnexesStartPage(int annexesStartPage) {
        this.annexesStartPage = annexesStartPage;
    }

    public PDDocument getPdfdocument() {
        return pdfdocument;
    }

    public int getBibliographyStartPage() {
        return bibliographyStartPage;
    }

    public void setBibliographyStartPage(int bibliographyStartPage) {
        this.bibliographyStartPage = bibliographyStartPage;
    }

    public int getFigureIndexStartPage() {
        return figureIndexStartPage;
    }

    public int getFigureIndexEndPage() {
        return figureIndexEndPage;
    }

    public int getTableIndexStartPage() {
        return tableIndexStartPage;
    }

    public int getTableIndexEndPage() {
        return tableIndexEndPage;
    }

    public int getBibliographyEndPage() {
        return bibliographyEndPage;
    }

    public int getAnnexesEndPage() {
        return annexesEndPage;
    }
}