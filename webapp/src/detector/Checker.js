import * as Icons from "react-icons/fi";
import "../css/forms.css";
import "../css/checker.css";
import React, {useEffect, useState} from "react";
import axios from "axios";
import {toast} from "react-toastify";

export default function Checker({userToken, setGuest}){
    const [checkForm, setCheckForm] = useState({text: ""});
    const [answer, setAnswer] = useState("");
    const [status, setStatus] = useState(0);
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
    const handleSubmit = async (e) => {
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
            const res = response.data.containedObject.text;
            // Save the token to local storage
            setAnswer(res);

            // set and display response
            // setStatus(res.status);
            // const responseStatus = await axios.get(
            //     `http://localhost:8080/app/status/${res.id}`,
            //     {
            //         'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json'}
            // );
            //
            // // Retrieve the token from the response's containedObject
            // const status = responseStatus.data.containedObject;

        } catch (error) {
            console.error("Error logging in:", error);
            await toast.error(
                error.response
                    ? error.response.data.message
                        ? error.response.data.message
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

    setGuest(false);

    return (
        <div className="checker">
            <div className="card">
                <div className="card-header">
                    <h2 className="card-title">Sprawdź treść</h2>
                </div>
                <div className="card-body">
                    <form onSubmit={handleSubmit}>
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
                                <button className={"btn btn-primary"}>Sprawdź</button>
                            </div>
                        </div>
                    </form>
                    <div className="answer-container">
                        <div className="answer">{answer}</div>
                    </div>
                </div>
            </div>
        </div>
    );
}