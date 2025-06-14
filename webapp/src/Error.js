import {useLocation} from "react-router-dom";

export default function Error({code}) {
    const location = useLocation();
    return (
        <div className="error-view">
            <div className='card card-primary'>
                <div className='card-header'>
                    <h2 className="card-title center-title large-title label-error">{code === '404' ? code+" - Nie znaleziono strony" : code+" - Brak dostepu"}</h2>
                </div>
                <div className='card-body'>
                    <div className="row">
                        <div className="col-md-12"><label className='error-content'>
                            {code === '404' ?
                                "Brak treści pod adresem: "+location.pathname
                                : "Ups :( Wygląda, że nie masz dostępu do szukanej treści."}
                        </label></div>
                    </div>
                </div>
            </div>
        </div>
    );
}