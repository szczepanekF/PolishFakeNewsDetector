import * as Icons from "react-icons/fi";
import "../css/forms.css";
import "../css/checker.css";
import React, {useEffect, useState} from "react";
import axios from "axios";
import {toast} from "react-toastify";
import {formatDateToYMDHis} from "../helper";

export default function Checker({userToken, setGuest}){
    const [checkForm, setCheckForm] = useState({text: ""});
    const [progress, setProgress] = useState({currentStep: 0, allSteps: 0});
    const [progressMessage, setProgressMessage] = useState("");
    const [isLoading, setIsLoading] = useState(false);
    const [result, setResult] = useState(null);
    const [evaluationId, setEvaluationId] = useState(null);
    const [textEmpty, setTextEmpty] = useState(false);

    const handleChange = (e) => {
        if(e.target.name === "text"){
            if(e.target.value.trim().length > 0){
                setTextEmpty(false);
            } else {
                setTextEmpty(true);
            }
        }
        setCheckForm({ ...checkForm, [e.target.name]: e.target.value });
    };
    const startEvaluationProccess = async (e) => {
        e.preventDefault()
        if(checkForm.text.trim().length > 0){
        try {
            setResult(null);
            const response = await axios.post(
                `${process.env.REACT_APP_LOGIC_API}/app/evaluate`,
                {
                    "text": checkForm.text
                },
                {
                    headers: {
                        'Authorization': `Bearer ${userToken}`,
                        'Content-Type': 'application/json'
                    }
                }
            );

            // Retrieve the token from the response's containedObject
            const id = response.data.contained_object.id;
            const currentStep = response.data.contained_object.current_step;
            const allSteps = response.data.contained_object.all_steps;            // Save the token to local storage
            setEvaluationId(id);
            setProgress({currentStep, allSteps});
            setIsLoading(true);

        } catch (error) {
            console.error("Error logging in:", error);
            await toast.error(
                error.response
                    ? error.response.data.error
                        ? error.response.data.error
                        : "Wystąpił błąd przy ewaluacji tekstu."
                    : "Wystąpił błąd przy ewaluacji tekstu",{
                    autoClose: 3000,
                }
            );
        }
        } else {
            setTextEmpty(true);
        }
    };
    useEffect(() => {
        if (!evaluationId) return;

        const interval = setInterval(async () => {
            try {
                const res = await axios.get(
                    `${process.env.REACT_APP_LOGIC_API}/app/status/${evaluationId}`,
                    {
                        headers: {
                            'Authorization': `Bearer ${userToken}`,
                        }
                    }
                );
            
                const currentStep = res.data.contained_object.current_step;
                const allSteps = res.data.contained_object.all_steps;
                setProgress({ currentStep, allSteps });
                const mess = res.data.contained_object.message;
                setProgressMessage(mess);

                if (currentStep === allSteps) {
                    clearInterval(interval);
                    setIsLoading(false);
                    setResult(res.data.contained_object.result)
                }
            } catch (err) {
                console.error(err);
                toast.error('Error while polling status.', {autoClose: 3000});
                clearInterval(interval);
                setIsLoading(false);
            }
        }, 300); // every .3s

        return () => clearInterval(interval);
    }, [evaluationId, userToken]);

    setGuest(false);

    return (
        <div className="checker">
            <div className="card">
                <div className="card-header">
                    <h2 className="card-title">Sprawdź treść</h2>
                </div>
                <div className="card-body">
                    <form onSubmit={startEvaluationProccess}>
                        <div className="form-row">
                            <div className="form-validation-input">
                                <textarea
                                    id="textToCheck"
                                    name="text" // Add name attribute
                                    className="form-control"
                                    value={checkForm.text}
                                    onChange={handleChange}
                                    placeholder="Twój tekst do sprawdzenia..."
                                    required
                                />
                                <p
                                    className={!textEmpty ? "text-success" : "text-danger"}
                                    style={{
                                        visibility:
                                            checkForm.text !== "" ? "visible" : "hidden",
                                    }}
                                >
                                    {!textEmpty
                                        ? ""
                                        : "Tekst nie może zostać pusty"}
                                </p>
                            </div>
                        </div>
                        <div className={"form-row"}>
                            <div className={"btn-container"}>
                                <button type={"submit"} className={"btn btn-primary"} disabled={isLoading}>Sprawdź</button>
                            </div>
                        </div>
                    </form>
                    <div className="answer-container">
                        {isLoading ?
                            <div className={"load-container"}>
                                <div className="load-bar"><div className={"progress"} style={{width: `${(progress.currentStep/progress.allSteps)*100}%`}}></div></div>
                                <div className={"label"}>{progressMessage}</div>
                            </div>
                            : null}
                        {result ?
                            <div style={{display: "contents"}}>
                        <div className={"answer-row"}><h2 className={"header"}>Wynik</h2> </div>
                        <div className={"answer-row"}><div className={"label"}>Werdykt:</div> <div className={"value"}> {result.label}</div></div>
                        {/*<div className={"answer-row"}><div className={"label"}>Ocena:</div>*/}
                        {/*    <div className={"value"}>{result.finalScore}</div></div>*/}
                            <div className={"answer-row"}><div className={"label"}>Wyjaśnienie:</div>
                        </div>
                            <div className={"answer-row"}><div className={"value"}>{result.explanation}</div></div>

                        {result.references?.length > 0 && (
                            <div style={{display: "contents"}}>
                                <div className={"answer-row"}><h4 className={"header"}>Odniesienia:</h4></div>
                                <div className="answer-row references">
                                    {result.references.map((ref, index) => (
                                            <a href={ref.link} className="reference-link" key={index} target="_blank" rel="noreferrer">
                                                <div>[{ref.footnote_number}]</div><div className={"date"}>{formatDateToYMDHis(ref.publication_date)}</div><div className={"label"}>{ref.link}</div>
                                            </a>
                                    ))}
                                </div>
                            </div>
                        )}
                            </div>
                            : null }
                    </div>
                </div>
            </div>
        </div>
    );
}