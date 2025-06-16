import { useEffect, useState } from "react";
import "../css/forms.css";
import "../css/guest.css";
import axios from "axios";
import { useLocation, useNavigate } from "react-router-dom";
import { toast } from "react-toastify";

export default function ResetPassword() {
    const navigate = useNavigate();
    const location = useLocation();
    const [isTokenSent, setIsTokenSent] = useState(false);
    const [isValidEmail, setIsValidEmail] = useState(true);
    const [emailAddress, setEmailAddress] = useState("");
    const [token, setToken] = useState("");

    const handleEmailChange = (event) => {
        setEmailAddress(event.target.value);
        setIsValidEmail(event.target.checkValidity());
    };

    const handleTokenChange = (event) => {
        setToken(event.target.value);
    };

    // const goToChangePasswordPage = async (e) => {
    //     e.preventDefault();
    //     try {
    //         // Make a POST request to decodeJWT endpoint with the token
    //         const response = await axios.post(
    //             `${process.env.REACT_APP_AUTH_API}/passwordRecovery`,
    //         {"emailAddress": emailAddress}
    //         );
    //         if (response.status === 200) {
    //             const springUserId = response.data.containedObject;
    //             location.pathname = `/change-password/${springUserId}`;
    //             navigate(`/change-password/${springUserId}`);
    //         }
    //     } catch (error) {
    //         console.error("Error while email and token pair validation:", error);
    //         toast.error(
    //             "Wystąpił błąd przy walidowaniu tokenu.",{
    //                 autoClose: 3000,
    //             }
    //         );
    //     }
    // };

    const sendEmailWithRecoveryToken = async (e) => {
        e.preventDefault();
        try {
            // Make a POST request to decodeJWT endpoint with the token
            const response = await axios.post(
                `${process.env.REACT_APP_AUTH_API}/app/user/passwordRecovery?emailAddress=${encodeURIComponent(emailAddress)}`,
                {}, // empty body
                {
                    headers: { "Content-Type": "application/json" }
                }
            );
                await toast.success(response.data.contained_object);
                // location.pathname = `/change-password/${springUserId}`;
                // navigate(`/change-password/${springUserId}`);

        } catch (error) {
            console.error("Error while email and token pair validation:", error);
            toast.error(
                "Wystąpił błąd przy wysyłaniu maila.",{
                    autoClose: 3000,
                }
            );
        }
    };

    return (
        <div className="password-reset-view view">
            <div className="card">
                <div className="card-header">
                    <h2 className="card-title center-title large-title">
                        Reset hasła
                    </h2>
                </div>
                <div className="card-body">
                    <form>
                        <div className="form-row">
                                <label className="input-label" htmlFor="enterEmail">
                                    E&#8209;mail
                                </label>
                            <div className={"form-validation-input"}>
                                <input
                                    id="enterEmail"
                                    className="form-control"
                                    name={"emailAddress"}
                                    type="email"
                                    placeholder="E-mail..."
                                    data-testid="emailinput"
                                    onChange={handleEmailChange}
                                    value={emailAddress}
                                    required
                                    pattern="[a-z0-9._%+-]+@[a-z0-9.-]+\.[a-z]{2,4}$"
                                />
                                <p
                                    className={isValidEmail ? "text-success" : "text-danger"}
                                    style={{
                                        visibility: emailAddress !== "" ? "visible" : "hidden",
                                    }}
                                >
                                    {isValidEmail ? "" : "Niepoprawny format e-mail"}
                                </p>
                            </div>
                        </div>
                        <div className="form-row">
                                <div className="btn-container float-right">
                                    <button
                                        className="btn submit-btn btn-success"
                                        onClick={sendEmailWithRecoveryToken}
                                    >
                                        Wyślij token resetu hasła
                                    </button>
                                </div>
                        </div>
                        {/*<div*/}
                        {/*    className="form-row"*/}
                        {/*    style={{ display: isTokenSent ? "flex" : "none" }}*/}
                        {/*>*/}
                        {/*        <label className="input-label" htmlFor="enterToken">*/}
                        {/*            Token*/}
                        {/*        </label>*/}
                        {/*        <input*/}
                        {/*            id="enterToken"*/}
                        {/*            className="form-control"*/}
                        {/*            type="text"*/}
                        {/*            placeholder="Token..."*/}
                        {/*            value={token}*/}
                        {/*            onChange={handleTokenChange}*/}
                        {/*        />*/}
                        {/*        <button*/}
                        {/*            className="btn confirm-token-btn btn-success"*/}
                        {/*            onClick={goToChangePasswordPage}*/}
                        {/*            style={{marginLeft: "4px"}}*/}
                        {/*        >*/}
                        {/*            Potwierdź*/}
                        {/*        </button>*/}
                        {/*</div>*/}
                        <div className="form-row">
                            <hr />
                        </div>
                        <div className="form-row">
                            {/*<div className="col-md-6">*/}
                            {/*    <div className="link-holder">*/}
                            {/*        <a className="login-link" href="/register">*/}
                            {/*            <label>Nie masz konta? Zarejestruj się!</label>*/}
                            {/*        </a>*/}
                            {/*    </div>*/}
                            {/*</div>*/}
                                <div className="link-holder">
                                    <a className="login-link" href="/login">
                                        <label>Wróć do strony logowania</label>
                                    </a>
                                </div>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    );
}
