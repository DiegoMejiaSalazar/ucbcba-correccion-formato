import React, { Component } from 'react';
import "../style/UploadFile.css";
import ucbImage from '../images/ucbcba.png';
import coverGif from '../images/caratula.gif';
import bibliographyGif from '../images/bibliografia.gif';
import warningGif from '../images/advertencias.png';
import ucblogo from '../images/ucblogo.png'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faDownload } from '@fortawesome/free-solid-svg-icons';
import { faExclamationTriangle } from '@fortawesome/free-solid-svg-icons';
import { Link } from 'react-router-dom';

class UploadFile extends Component {

    constructor(props) {
        super(props);
        this.handleSubmit = this.handleSubmit.bind(this);
        this.uploadSingleFile = this.uploadSingleFile.bind(this);
        this.fileInput = React.createRef();
    }

    state = {
        status: '',
        error: ''
    }

    componentDidMount() {
        document.body.style = 'background: none;';
        document.body.style = 'background-image: ../images/pattern_news.png;';
    }

    handleSubmit(event) {
        this.setState({ status: '' });
        this.setState({ error: '' });
        this.uploadSingleFile(this.fileInput.current.files[0]);
    }

    uploadSingleFile(file) {
        var formData = new FormData();
        formData.append("file", file);
        this.setState({ status: 'Cargando archivo...' })
        fetch(window["cfgApiBaseUrl"] + '/api/uploadFile', {
            method: 'POST',
            body: formData
        }).then(
            response => {
                this.setState({ status: '' });
                return response.json();
            }
        ).then(
            data => {
                if (data.status) {
                    throw new Error((data && data.message) || "Ocurrió algún error.");
                } else {
                    this.props.history.push(`/calibrarPaginas/${encodeURI(data.fileName)}`);
                }

            }
        ).catch(
            error => {
                this.setState({ error: error.message });
            }
        );

    }

    render() {
        var sBrowser, sUsrAg = navigator.userAgent;
        const isChrome = sUsrAg.indexOf('Chrome') > -1
        console.warn(isChrome)
        var messageCode = null;
        if (this.state.status !== '') {
            messageCode = (<div className="col">
                <center>
                    <p className="alert alert-info myAlert" role="alert"> {this.state.status} </p>
                </center>
            </div>);
        }
        var errorCode = null;
        if (this.state.error !== '') {
            messageCode = (<div className="col">
                <center>
                    <p className="alert alert-danger myAlert" role="alert"> {this.state.error} </p>
                </center>
            </div>);
        }
        return (
            <div >
                <div className="mynavbar">
                    <center>
                        <Link to="/">
                            <img src={ucbImage} />
                        </Link>
                    </center>
                </div>


                <div className="container main">
                    <div className="row justify-content-md-center">
                        <div className="presentation">
                            <center>
                                <h3 className="text-guide-1"> Revisa el formato de tu trabajo académico ¡aquí!</h3>
                                <h4 className="text-guide-2"> Sube tu documento en formato PDF por favor</h4>
                                {isChrome === false && <div className='alert alert-danger' role='alert'> SE RECOMIENDA USAR <a href="https://www.google.com/intl/es/chrome/?brand=YTUH&gclid=EAIaIQobChMImqun8Z3W-AIVs5BoCR3lJwLiEAAYASAAEgLmAPD_BwE&gclsrc=aw.ds" className='alert-link'>GOOGLE CHROME</a></div>}
                            </center>
                        </div>
                    </div>
                    <div className="row justify-content-end">
                        <div className="col-4">
                            <div className="left">
                                <a href="https://cba.ucb.edu.bo/wp-content/uploads/2019/12/GUIAUCB2020.pdf" download><FontAwesomeIcon icon={faDownload} /> Descargue la guia
                                </a>
                            </div>
                        </div>
                        <div className="col-4">
                            <center>
                                <button className="btn btn-primary btn-lg btn-file">
                                    Seleccione su archivo PDF
                                    <input type="file" name="file" accept="application/pdf" required ref={this.fileInput} onChange={this.handleSubmit} />
                                </button>
                                {messageCode}
                                {errorCode}
                            </center>
                        </div>
                        <div className="col-4">
                            <div className="right">
                                <a href={ucblogo} download><FontAwesomeIcon icon={faDownload} /> Descargue el logo
                                </a>
                            </div>
                        </div>
                    </div>
                </div>
                <div className="container main">
                    <h5 className="textTips"> Consejos y recomendaciones</h5>
                    <div className="card-deck">
                        <div className="card">
                            <div className="card-body">
                                <h5 className="card-title">Carátula</h5>
                                <p className="card-text cardText">Recuerda agregar sangría negativa a la primera línea de la carátula. De este modo la primera línea se encontrará entre los márgenes establecidos según la guía.</p>
                            </div>
                            <img className="card-img-top tipsCard" src={coverGif} />
                            <div className="card-footer">
                                <small className="text-muted">Consejo</small>
                            </div>
                        </div>
                        <div className="card">
                            <div className="card-body">
                                <h5 className="card-title">Bibliografía</h5>
                                <p className="card-text cardText">Entre cada referencia bibliográfica recuerda agregar un espaciado entre párrafos para que el sistema garantice una correcta revisión del formato en la bibliografía.</p>
                            </div>
                            <img className="card-img-top tipsCard" src={bibliographyGif} />
                            <div className="card-footer">
                                <small className="text-muted">Recomendación</small>
                            </div>
                        </div>
                        <div className="card">
                            <div className="card-body">
                                <h5 className="card-title">Advertencias</h5>
                                <p className="card-text cardText">Recuerda que las advertencias <i className="warningColor"> <FontAwesomeIcon icon={faExclamationTriangle} /> </i> son sólo posibles errores de incumplimiento en el formato. Usar un criterio adecuado ante las advertencias.</p>
                            </div>
                            <img className="card-img-top tipsCard" src={warningGif} />
                            <div className="card-footer">
                                <small className="text-muted">Recomendación</small>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        );
    }
}

export default UploadFile;
