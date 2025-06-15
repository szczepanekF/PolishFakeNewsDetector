import * as Icons from "react-icons/fi";
import "../css/forms.css";
import "../css/checker.css";
import {useEffect, useState} from "react";
import axios from "axios";
import {toast} from "react-toastify";

export default function Checker({user, setGuest}){
    const [checkForm, setCheckForm] = useState({text: ""});
    const [answer, setAnswer] = useState("");
    const [status, setStatus] = useState(0);
    const token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIyNDI1NDQiLCJlbWFpbEFkZHJlc3MiOiIyNDI1NDRAZWR1LnAubG9kei5wbCIsInVzZXJJZCI6MSwiaWF0IjoxNzQ5MDQ3MzcwLCJleHAiOjEyMDAxNzQ5MDQ3MzcwfQ.AUZmO5GEH5mqX6fx7aaMTMq77uSmkhf2Zb92cTDIn3c";

    const handleChange = (e) => {
        setCheckForm({ ...checkForm, [e.target.name]: e.target.value });
    };
    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            const response = await axios.post(
                "http://localhost:8080/app/evaluate",
                checkForm,
                {
                'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json'}
            );

            // Retrieve the token from the response's containedObject
            const res = response.data.containedObject;
            // Save the token to local storage

            // set and display response
            setStatus(res.status);
            const responseStatus = await axios.get(
                `http://localhost:8080/app/status/${res.id}`,
                {
                    'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json'}
            );

            // Retrieve the token from the response's containedObject
            const status = responseStatus.data.containedObject;

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
    };

    // setGuest(false);

    useEffect(() => {
        setTimeout(() =>  setGuest(false), 2000);
    }, [setGuest]);

    return (
        <div className="checker">
            <div className="card">
                <div className="card-header">
                    <h2 className="card-title">Sprawdź treść</h2>
                </div>
                <div className="card-body">
                    <form onSubmit={handleSubmit}>
                        <div className="form-row">
                                <textarea
                                    id="textToCheck"
                                    name="text" // Add name attribute
                                    className="form-control"
                                    value={checkForm.text}
                                    onChange={handleChange}
                                    placeholder="Twój tekst do sprawdzenia..."
                                    required
                                />
                        </div>
                        <div className={"form-row"}>
                        <div className={"btn-container"}>
                        <button className={"btn btn-primary"}>Sprawdź</button>
                        </div></div>
                    </form>
                    <div className="answer-container">
                            <div className="answer">{answer}</div>
                    </div>
                </div>
            </div>
        </div>
    );
}