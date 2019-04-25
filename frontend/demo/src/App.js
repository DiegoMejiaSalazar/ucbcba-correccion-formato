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
} from "../../src";


import Spinner from "./Spinner";
import Sidebar from "./Sidebar";

import type { T_Highlight, T_NewHighlight } from "../../src/types";

import "./style/App.css";

type T_ManuscriptHighlight = T_Highlight;

type Props = {};

type State = {
  highlights: Array<T_ManuscriptHighlight>
};

const getNextId = () => String(Math.random()).slice(2);

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

class App extends Component<Props, State> {

  constructor(props) {
    super(props);
    url = "/api/downloadFile/" + `${encodeURI(this.props.match.params.name)}`;
  }

  state = {
    highlights: [],
    basicFormatReport: [],
    coverformatErrors: [],
    indexformatErrors: [],
    figureindexformatErrors: [],
    tableindexformatErrors: [],
    numerationformatErrors: [],
    englishwordsformatErrors: [],
    biographyformatErrors: [],
    figuretableformatErrors: []
  };

  state: State;

  resetHighlights = () => {
    this.setState({
      highlights: []
    });
  };

  scrollViewerTo = (highlight: any) => { };

  scrollToHighlightFromHash = () => {
    const highlight = this.getHighlightById(parseIdFromHash());

    if (highlight) {
      this.scrollViewerTo(highlight);
    }
  };

  async componentDidMount() {
    var basicFormatReportJson = await (await fetch(`/api/basicFormat/${encodeURI(this.props.match.params.name)}` + `${(this.props.location.search)}`)).json();
    this.setState({ basicFormatReport: basicFormatReportJson });

    var coverformatErrorJson = await (await fetch(`/api/coverpage/errors/${encodeURI(this.props.match.params.name)}` + `${(this.props.location.search)}`)).json();
    this.setState({ coverformatErrors: coverformatErrorJson });

    var indexformatErrorJson = await (await fetch(`/api/indexpage/errors/${encodeURI(this.props.match.params.name)}` + `${(this.props.location.search)}`)).json();
    this.setState({ indexformatErrors: indexformatErrorJson });

    var figureindexformatErrorsJson = await (await fetch(`/api/figureindex/errors/${encodeURI(this.props.match.params.name)}` + `${(this.props.location.search)}`)).json();
    this.setState({ figureindexformatErrors: figureindexformatErrorsJson });

    var tableindexformatErrorsJson = await (await fetch(`/api/tableindex/errors/${encodeURI(this.props.match.params.name)}` + `${(this.props.location.search)}`)).json();
    this.setState({ tableindexformatErrors: tableindexformatErrorsJson });

    var numerationErrorJson = await (await fetch(`/api/numeration/errors//${encodeURI(this.props.match.params.name)}` + `${(this.props.location.search)}`)).json();
    this.setState({ numerationformatErrors: numerationErrorJson });

    var englishwordsformatErrorsJson = await (await fetch(`/api/englishwords/errors//${encodeURI(this.props.match.params.name)}` + `${(this.props.location.search)}`)).json();
    this.setState({ englishwordsformatErrors: englishwordsformatErrorsJson });

    var biographyErrorsJson = await (await fetch(`/api/biography/errors//${encodeURI(this.props.match.params.name)}` + `${(this.props.location.search)}`)).json();
    this.setState({ biographyformatErrors: biographyErrorsJson });

    var figuretableformatErrorsJson = await (await fetch(`/api/figuretable/errors//${encodeURI(this.props.match.params.name)}` + `${(this.props.location.search)}`)).json();
    this.setState({ figuretableformatErrors: figuretableformatErrorsJson });

    this.setState({
      highlights: [...this.state.coverformatErrors, ...this.state.indexformatErrors,
      ...this.state.figureindexformatErrors, ...this.state.tableindexformatErrors,
      ...this.state.numerationformatErrors, ...this.state.figuretableformatErrors,
      ...this.state.englishwordsformatErrors, ...this.state.biographyformatErrors]
    });
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

  addHighlight(highlight: T_NewHighlight) {
    const { highlights } = this.state;

    console.log("Saving highlight", highlight);

    this.setState({
      highlights: [{ ...highlight, id: getNextId() }, ...highlights]
    });
  }

  updateHighlight(highlightId: string, position: Object, content: Object) {
    console.log("Updating highlight", highlightId, position, content);

    this.setState({
      highlights: this.state.highlights.map(h => {
        return h.id === highlightId
          ? {
            ...h,
            position: { ...h.position, ...position },
            content: { ...h.content, ...content }
          }
          : h;
      })
    });
  }

  render() {
    const { highlights, basicFormatReport, coverformatErrors,
      indexformatErrors, figureindexformatErrors, tableindexformatErrors,
      numerationformatErrors, englishwordsformatErrors, biographyformatErrors,
      figuretableformatErrors } = this.state;

    return (
      <div className="App" style={{ display: "flex", height: "100vh" }}>
        <Sidebar
          highlights={highlights}
          resetHighlights={this.resetHighlights}
          basicFormatReport={basicFormatReport}
          coverformatErrors={coverformatErrors}
          indexformatErrors={indexformatErrors}
          figureindexformatErrors={figureindexformatErrors}
          tableindexformatErrors={tableindexformatErrors}
          numerationformatErrors={numerationformatErrors}
          englishwordsformatErrors={englishwordsformatErrors}
          figuretableformatErrors={figuretableformatErrors}
          biographyformatErrors={biographyformatErrors}

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
                enableAreaSelection={event => event.altKey}
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
                        onChange={boundingRect => {
                          this.updateHighlight(
                            highlight.id,
                            { boundingRect: viewportToScaled(boundingRect) },
                            { image: screenshot(boundingRect) }
                          );
                        }}
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

export default App;
