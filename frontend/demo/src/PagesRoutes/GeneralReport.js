// @flow

import React, { Component } from "react";

import URLSearchParams from "url-search-params";

import {
  PdfLoader,
  PdfHighlighter,
  Tip,
  Highlight,
  Popup,
  AreaHighlight
} from "../../../src";


import Spinner from "./Components/Spinner";
import Sidebar from "./Components/Sidebar";

import type { T_Highlight, T_NewHighlight } from "../../src/types";
import { Link } from 'react-router-dom';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faRedo } from '@fortawesome/free-solid-svg-icons'

import "../style/GeneralReport.css";

type T_ManuscriptHighlight = T_Highlight;

type Props = {};

type State = {
  highlights: Array<T_ManuscriptHighlight>
};

const parseIdFromHash = () => location.hash.slice("#highlight-".length);

const resetHash = () => {
  location.hash = "";
};

const HighlightPopup = ({ comment }) =>
  comment.text ? (
    <div className="Highlight__popup">
      {comment.emoji} {comment.text}
    </div>
  ) : null;


const searchParams = new URLSearchParams(location.search);
var url = " ";

class GeneralReport extends Component<Props, State> {

  constructor(props) {
    super(props);
    url = window["cfgApiBaseUrl"] + "/api/downloadFile/" + `${encodeURI(this.props.match.params.name)}.pdf`;
  }

  state = {
    highlights: [],
    basicFormatReport: [],
    isLoading: true,
    error: false
  };

  state: State;

  scrollViewerTo = (highlight: any) => { };

  scrollToHighlightFromHash = () => {
    const highlight = this.getHighlightById(parseIdFromHash());

    if (highlight) {
      this.scrollViewerTo(highlight);
    }
  };

  async componentDidMount() {
    document.body.style = 'background-image: none;';
    document.body.style = 'background: #717579;';
    await fetch(window["cfgApiBaseUrl"] + `/api/basicFormat/${encodeURI(this.props.match.params.name)}.pdf` + `${(this.props.location.search)}`, {
      method: 'POST',
      headers: {
        'connection': 'keep-alive'
      }
    }).then(
      response => {
        return response.json();
      }
    ).then(
      basicFormatReportJson => {
        if (basicFormatReportJson.status) {
          throw new Error("Ocurrió algún error.");
        } else {
          this.setState({ basicFormatReport: basicFormatReportJson });
        }
      }
    ).catch(
      error => {
        this.setState({ error: true });
      }
    );
    await fetch(window["cfgApiBaseUrl"] + `/api/hightlight/errors/${encodeURI(this.props.match.params.name)}.pdf` + `${(this.props.location.search)}`, {
      method: 'POST'
    }).then(
      response => {
        return response.json();
      }
    ).then(
      highlightsJson => {
        if (highlightsJson.status) {
          throw new Error("Ocurrió algún error.");
        } else {
          this.setState({ highlights: highlightsJson });
        }
      }
    ).catch(
      error => {
        this.setState({ error: true });
      }
    );
    this.setState({ isLoading: false });
    window.addEventListener(
      "hashchange",
      this.scrollToHighlightFromHash,
      false
    );
  }

  getHighlightById(id: string) {
    const { highlights } = this.state;
    return highlights.find(highlight => highlight.id === id);
  }

  removeHighlight = (_id) => {
    this.setState({
      highlights: this.state.highlights.filter((highlight, i) => {
        return highlight.id !== _id
      })
    });
  }

  render() {
    if (this.state.isLoading) {
      return (
        <div>
          <div className="center-loader">
            <center>
              <div className="lds-spinner"><div></div><div></div><div></div><div></div><div></div><div></div><div></div><div></div><div></div><div></div><div></div><div></div></div>
              <h5> Analizando archivo PDF...</h5>
            </center>
          </div>
        </div>);
    }
    if (this.state.error) {
      return (
        <div>
          <div className="container main">
            <div className="row justify-content-md-center">
              <div className="presentation">
                <center>
                  <h5 className="alert alert-danger" role="alert"> Error al cargar el archivo PDF</h5>
                  <Link to="/">
                    <button type="button" className="btn btn-outline-primary btn-lg"> Intenta de nuevo <FontAwesomeIcon icon={faRedo} /></button>
                  </Link>
                </center>
              </div>
            </div>
          </div>
        </div>);
    }
    const { highlights, basicFormatReport } = this.state;

    return (
      <div className="App" style={{ display: "flex", height: "100vh" }}>
        <Sidebar
          highlights={highlights}
          basicFormatReport={basicFormatReport}
          removeHighlight={this.removeHighlight}
        />
        <div
          style={{
            height: "100vh",
            width: "75vw",
            overflowY: "scroll",
            position: "relative"
          }}
        >
          <PdfLoader url={url} beforeLoad={<Spinner />}>
            {pdfDocument => (
              <PdfHighlighter
                pdfDocument={pdfDocument}
                onScrollChange={resetHash}
                scrollRef={scrollTo => {
                  this.scrollViewerTo = scrollTo;

                  this.scrollToHighlightFromHash();
                }}
                highlightTransform={(
                  highlight,
                  index,
                  setTip,
                  hideTip,
                  viewportToScaled,
                  screenshot,
                  isScrolledTo
                ) => {
                  const isTextHighlight = !Boolean(
                    highlight.content && highlight.content.image
                  );

                  const component = isTextHighlight ? (
                    <Highlight
                      isScrolledTo={isScrolledTo}
                      position={highlight.position}
                      comment={highlight.comment}
                    />
                  ) : (
                    <AreaHighlight
                      highlight={highlight}
                    />
                  );

                  return (
                    <Popup
                      popupContent={<HighlightPopup {...highlight} />}
                      onMouseOver={popupContent =>
                        setTip(highlight, highlight => popupContent)
                      }
                      onMouseOut={hideTip}
                      key={index}
                      children={component}
                    />
                  );
                }}
                highlights={highlights}
              />
            )}
          </PdfLoader>
        </div>
      </div>
    );
  }
}

export default GeneralReport;