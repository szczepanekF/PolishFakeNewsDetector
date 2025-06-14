import { useEffect, useState } from "react";
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

    const goToChangePasswordPage = async (e) => {
        e.preventDefault();
        try {
            // Make a POST request to decodeJWT endpoint with the token
            const response = await axios.post(
                `http://localhost:8080/app/auth/checkEmailAndToken?userEmailAddress=${emailAddress}&token=${token}`
            );
            if (response.status === 200) {
                const springUserId = response.data.containedObject;
                location.pathname = `/change-password/${springUserId}`;
                navigate(`/change-password/${springUserId}`);
            }
        } catch (error) {
            console.error("Error while email and token pair validation:", error);
            toast.error(
                "Wystąpił błąd przy walidowaniu tokenu.",{
                    autoClose: 3000,
                }
            );
        }
    };

    const sendEmailWithRecoveryToken = async (e) => {
        e.preventDefault();
        if (isValidEmail) {
            try {
                const response = await axios.post(
                    `http://localhost:8080/app/auth/passwordRecovery?emailAddress=${emailAddress}`
                );
                if (response.status === 200) {
                    setIsTokenSent(true);
                }
            } catch (error) {
                console.error("Error while password recovery:", error);
                toast.error(
                    "Wystąpił błąd przy odzyskiwaniu hasła",{
                        autoClose: 3000,
                    }
                );
            }
        } else {
            toast.error(
                "Wprowadź poprawny adres e-mail",{
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
                        <div className="row">
                            <div className="col-md-2">
                                <label className="input-label" htmlFor="enterUsername">
                                    E-mail
                                </label>
                            </div>
                            <div className="col-md-10">
                                <input
                                    id="enterUsername"
                                    className="form-control"
                                    type="email"
                                    placeholder="Enter e-mail..."
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
                                    {isValidEmail ? "Email valid" : "Email invalid"}
                                </p>
                            </div>
                        </div>
                        <div className="row">
                            <div className="col-md-12">
                                <div className="btn-container float-right">
                                    <button
                                        className="btn submit-btn btn-success"
                                        onClick={sendEmailWithRecoveryToken}
                                    >
                                        Wyślij token resetu hasła
                                    </button>
                                </div>
                            </div>
                        </div>
                        <div
                            className="row"
                            style={{ display: isTokenSent ? "flex" : "none" }}
                        >
                            <div className="col-md-2">
                                <label className="input-label" htmlFor="enterToken">
                                    Token
                                </label>
                            </div>
                            <div className="col-md-8">
                                <input
                                    id="enterToken"
                                    className="form-control"
                                    type="text"
                                    placeholder="Enter token..."
                                    value={token}
                                    onChange={handleTokenChange}
                                />
                            </div>
                            <div className="col-md-2">
                                <button
                                    className="btn confirm-token-btn btn-success"
                                    onClick={goToChangePasswordPage}
                                >
                                    Potwierdź
                                </button>
                            </div>
                        </div>
                        <div className="row">
                            <hr />
                        </div>
                        <div className="row">
                            {/*<div className="col-md-6">*/}
                            {/*    <div className="link-holder">*/}
                            {/*        <a className="login-link" href="/register">*/}
                            {/*            <label>Nie masz konta? Zarejestruj się!</label>*/}
                            {/*        </a>*/}
                            {/*    </div>*/}
                            {/*</div>*/}
                            <div className="col-md-12">
                                <div className="link-holder">
                                    <a className="login-link" href="/login">
                                        <label>Wróć do strony logowania</label>
                                    </a>
                                </div>
                            </div>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    );
}
