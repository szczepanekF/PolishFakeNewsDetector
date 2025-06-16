import * as Icons from "react-icons/fi";
import "../css/forms.css";
import "../css/checker.css";
import React, {useEffect, useState} from "react";
import axios from "axios";
import {toast} from "react-toastify";

export default function Checker({userToken, setGuest}){
    const [checkForm, setCheckForm] = useState({text: ""});
    const [progress, setProgress] = useState({currentStep: 0, allSteps: 0});
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
            const {currentStep, allSteps} = response.data.contained_object;
            // Save the token to local storage
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
                const res = await axios.get(`${process.env.REACT_APP_LOGIC_API}/app/status/${evaluationId}`);
                const { currentStep, allSteps } = res.data.contained_object;
                setProgress({ currentStep, allSteps });

                if (currentStep === allSteps) {
                    clearInterval(interval);

                    const resultRes = await axios.get(`${process.env.REACT_APP_LOGIC_API}/app/result/${evaluationId}`);
                    setResult(resultRes.data.contained_object);
                    setIsLoading(false);
                }
            } catch (err) {
                console.error(err);
                toast.error('Error while polling status.', {autoClose: 3000});
                clearInterval(interval);
                setIsLoading(false);
            }
        }, 3000); // every 3s

        return () => clearInterval(interval);
    }, [evaluationId]);

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
                            <div className="load-bar"><div className={"progress"} style={{width: `${(progress.currentStep/progress.allSteps)*100}%`}}></div></div>
                            : null}
                        {result ?
                            <div style={{display: "contents"}}>
                        <div className={"answer-row"}><h2 className={"header"}>Wynik</h2> </div>
                        <div className={"answer-row"}><div className={"label"}>Werdykt:</div> <div className={"value"}> {result.result.label}</div></div>
                        <div className={"answer-row"}><div className={"label"}>Ocena:</div>
                            <div className={"value"}>{result.result.finalScore}</div></div>
                            <div className={"answer-row"}><div className={"label"}>Wyjaśnienie:</div>
                        </div>
                            <div className={"answer-row"}><div className={"value"}>{result.result.explanation}</div></div>

                        {/*    {result.result.references?.length > 0 && (*/}
                        {/*    <div style={{display: "contents"}}>*/}
                        {/*        <div className={"answer-row"}><h4 className={"header"}>References:</h4></div>*/}
                        {/*        <ul className="list-disc list-inside">*/}
                        {/*            {result.result.references.map((ref, index) => (*/}
                        {/*                <li key={index}>*/}
                        {/*                    <a href={ref.url} target="_blank" rel="noopener noreferrer" className="text-blue-600 underline">*/}
                        {/*                        {ref.url}*/}
                        {/*                    </a>*/}
                        {/*                </li>*/}
                        {/*            ))}*/}
                        {/*        </ul>*/}
                        {/*    </div>*/}
                        {/*)}*/}
                            </div>
                            : null }
                    </div>
                </div>
            </div>
        </div>
    );
}