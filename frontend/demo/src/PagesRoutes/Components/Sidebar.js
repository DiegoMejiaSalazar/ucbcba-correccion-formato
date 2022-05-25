// @flow

import React from "react";
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faTimes } from '@fortawesome/free-solid-svg-icons'
import { faCheck } from '@fortawesome/free-solid-svg-icons'
import Section from "./Section";
import type { T_Highlight } from "../../src/types";
type T_ManuscriptHighlight = T_Highlight;
import { Link } from 'react-router-dom';

type Props = {
  highlights: Array<T_ManuscriptHighlight>,
  basicFormatReport: Array<>,
  resetHighlights: () => void,
};

const updateHash = highlight => {
  location.hash = `highlight-${highlight.id}`;
};

function Sidebar({ highlights, resetHighlights, basicFormatReport, removeHighlight }: Props) {
  var coverformatErrors = [];
  var indexformatErrors = [];
  var figureindexformatErrors = [];
  var tableindexformatErrors = [];
  var numerationformatErrors = [];
  var figuretableformatErrors = [];
  var englishwordsformatErrors = [];
  var biographyformatErrors = [];
  var spellCheckingErrors = [];
  var contextualErrors = []
  highlights.forEach(highlight => {
    if (highlight.type === "caratula") {
      coverformatErrors.push(highlight);
    }
    else if (highlight.type === "indiceGeneral") {
      indexformatErrors.push(highlight);
    }
    else if (highlight.type === "FIGURAS") {
      figureindexformatErrors.push(highlight);
    }
    else if (highlight.type === "TABLAS") {
      tableindexformatErrors.push(highlight);
    }
    else if (highlight.type === "numeracion") {
      numerationformatErrors.push(highlight);
    }
    else if (highlight.type === "tablaFigura") {
      figuretableformatErrors.push(highlight);
    }
    else if (highlight.type === "extranjerismo") {
      englishwordsformatErrors.push(highlight);
    }
    else if (highlight.type === "bibliografia") {
      biographyformatErrors.push(highlight);
    }
    else if (highlight.type === "ortografia") {
      spellCheckingErrors.push(highlight);
    }
    else if (highlight.type === "contextual") {
      contextualErrors.push(highlight);
    }
  });

  return (
    <div className="sidebar" >
      <div className="description">
        <h2 >Reporte General</h2>
        <p className="backHome">
          <Link to="/">
            Volver al Inicio
          </Link>
        </p>
      </div>
      <hr></hr>
      <div className="formalAspects">
        <h4 > Aspetos formales</h4>
        <table className="table table-bordered tableFormalAspects">
          <thead className="thead-light">
            <tr>
              <th scope="col">Formato básico</th>
              <th scope="col">Cumple</th>
            </tr>
          </thead>
          <tbody>
            {basicFormatReport.map((basicFormat, index) => (
              <tr
                key={index}
              >
                <td className="grey" >{basicFormat.format}</td>
                {basicFormat.correct == true ? (
                  <td style={{ color: "green" }}> <center><FontAwesomeIcon icon={faCheck} /></center></td>
                ) : (<td style={{ color: "red" }}>  <center><FontAwesomeIcon icon={faTimes} /></center> </td>)}
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <hr></hr>
      <Section
        section="Carátula"
        formatErros={coverformatErrors}
        removeHighlight={removeHighlight}
      />


      <Section
        section="Índice General"
        formatErros={indexformatErrors}
        removeHighlight={removeHighlight}
      />


      <Section
        section="Índice de Figuras"
        formatErros={figureindexformatErrors}
        removeHighlight={removeHighlight}
      />


      <Section
        section="Índice de Tablas"
        formatErros={tableindexformatErrors}
        removeHighlight={removeHighlight}
      />


      <Section
        section="Paginación"
        formatErros={numerationformatErrors}
        removeHighlight={removeHighlight}
      />


      <Section
        section="Tablas y Figuras"
        formatErros={figuretableformatErrors}
        removeHighlight={removeHighlight}
      />


      <Section
        section="Extranjerismo (Inglés)"
        formatErros={englishwordsformatErrors}
        removeHighlight={removeHighlight}
      />


      <Section
        section="Bibliografía"
        formatErros={biographyformatErrors}
        removeHighlight={removeHighlight}
      />

      <Section
        section="Ortografía"
        formatErros={spellCheckingErrors}
        removeHighlight={removeHighlight}
      />

      <Section
        section="Contextual"
        formatErros={contextualErrors}
        removeHighlight={removeHighlight}
      />
    </div>
  );
}

export default Sidebar;