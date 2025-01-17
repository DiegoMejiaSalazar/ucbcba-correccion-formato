import React, { Component } from 'react';
import PdfPreview from "./PdfPreview";

class StepAnnexed extends Component {
    constructor(props) {
        super(props);
    }

    render() {
        if (this.props.currentStep !== 6) {
            return null
        }

        var inputs = null;
        if (this.props.noAnnexes) {
            inputs = (
                <fieldset disabled>
                    <div className="leftForm">
                        <label className="myLabel">Página inicial:</label>
                        <input
                            name="annexesStartPage"
                            type="number"
                        />
                    </div>
                    <div className="leftForm">
                        <label className="myLabel">Página final:</label>
                        <input
                            name="annexesEndPage"
                            type="number"
                        />
                    </div>
                </fieldset>
            );
        } else {
            inputs = (
                <div>
                    <div className="leftForm">
                        <label className="myLabel">Página inicial:</label>
                        <input
                            name="annexesStartPage"
                            type="number"
                            value={this.props.annexesStartPage}
                            onChange={this.props.handleChange}
                            min="1"
                            required
                        />
                    </div>
                    <div className="leftForm">
                        <label className="myLabel">Página final:</label>
                        <input
                            name="annexesEndPage"
                            type="number"
                            value={this.props.annexesEndPage}
                            onChange={this.props.handleChange}
                            min="1"
                            required
                        />
                    </div>
                </div>
            );
        };
        var errorMessage = null;
        if (!this.props.validInputs) {
            errorMessage = (<div>
                <center>
                    <p className="alert alert-danger myAlert" role="alert"> Por favor ingrese un rango de páginas válido </p>
                </center>
            </div>);
        }

        return (
            <div>
                <center><h4 className="tittle-wizard"> Anexos </h4> </center>
                <div className="row">
                    <div className="col-lg-4">
                        <div className="inputs-buttons">
                            <div className="custom-control custom-checkbox checkBoxForm">
                                <input type="checkbox" className="custom-control-input" id="customCheck1" onChange={() => { this.props.handleAnnexes() }} checked={this.props.noAnnexes} />
                                <label className="custom-control-label" htmlFor="customCheck1">No tengo esta sección</label>
                            </div>
                            <form onSubmit={this.props.handleSubmit}>
                                {inputs}
                                {errorMessage}
                                <div className="next-previous-buttons">
                                    <div className="leftForm">
                                        <button
                                            className="btn btn-secondary button-previous"
                                            type="button" onClick={this.props.previousStep} >
                                            &laquo; Anterior
                                </button>
                                        <button
                                            className="btn btn-success "
                                            type="submit" >
                                            Enviar &raquo;
                                </button>
                                    </div>
                                </div>
                            </form>
                        </div>
                    </div>
                    <div className="col-lg-8">
                        <div className="scrollable">
                            <center>
                                <PdfPreview
                                    url={this.props.url}
                                    pageStart={this.props.annexesStartPage}
                                    pageEnd={this.props.annexesEndPage}
                                    active={this.props.noAnnexes}
                                />
                            </center>
                        </div>
                    </div>
                </div>
            </div>
        )
    }
}

export default StepAnnexed;