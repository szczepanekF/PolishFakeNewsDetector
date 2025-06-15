import * as Icons from "react-icons/fi";
import * as IconsFa from "react-icons/fa6";
import "../css/forms.css";
import "../css/guest.css";
import React, { useEffect, useState } from "react";
import axios from "axios";
import { useLocation, useNavigate } from "react-router-dom";
import { toast } from "react-toastify";
import {validatePassword, validateEmail} from "./validation";

export default function Login({setGuest, setUserToken}) {
    const location = useLocation();
    const navigate = useNavigate();
    const [loginForm, setLoginForm] = useState({
        email: "",
        password: "",
    });
    const [isPasswordVisible, setIsPasswordVisible] = useState(false);
    const [isRememberMeChecked, setIsRememberMeChecked] = useState(false);
    const [isPasswordValid, setIsPasswordValid] = useState(false);
    const [isEmailValid, setIsEmailValid] = useState(false);

    const handleChange = (e) => {
        if (e.target.name === "password") {
            setIsPasswordValid(validatePassword(e.target.value));
            console.log(isPasswordValid);
        }
        if (e.target.name === "email") {
            setIsEmailValid(validateEmail(e.target.value));
            console.log(isEmailValid);
        }
        setLoginForm({ ...loginForm, [e.target.name]: e.target.value });
    };

    // const decodeJWToken = async (tokenParam) => {
    //     try {
    //         // Make a POST request to decodeJWT endpoint with the token
    //         const response = await axios.post(
    //             "http://localhost:8080/app/user/login",
    //             tokenParam
    //         );
    //         localStorage.setItem(
    //             "springUserId",
    //             response.data.containedObject.SpringUserId
    //         );
    //     } catch (error) {
    //         console.error("Error decoding JWT:", error);
    //         toast.error(
    //             "Błąd odczytu JWT. Proszę spróbować później.", {
    //                 autoClose: 3000,
    //             }
    //         );
    //     }
    // };

    useEffect(() => {
        const isGoogleLogin = JSON.parse(localStorage.getItem("isGoogleLogin"));
        if (isGoogleLogin) {
            const queryParams = new URLSearchParams(location.search);
            const tokenParam = queryParams.get("token");
            if (tokenParam) {
                localStorage.setItem("token", tokenParam);
                setUserToken(tokenParam);
                localStorage.setItem("isGoogleLogin", JSON.stringify(false));
                location.pathname = "/check";
                navigate("/check");
                window.location.reload();
            }
        }
    }, [location, navigate, setUserToken]);

    useEffect(() => {
        if (localStorage.getItem("userLoginData")) {
            const userLoginData = JSON.parse(localStorage.getItem("userLoginData"));
            setLoginForm({
                email: userLoginData.email,
                password: userLoginData.password,
            });
            setIsRememberMeChecked(userLoginData.isRememberMeChecked);
            setIsPasswordValid(true);
        }
    }, []);

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (isRememberMeChecked) {
            const userLoginData = {
                email: loginForm.email,
                password: loginForm.password,
                isRememberMeChecked,
            };
            localStorage.setItem("userLoginData", JSON.stringify(userLoginData));
        } else {
            localStorage.removeItem("userLoginData");
        }
        try {
            const response = await axios.post(
                `${process.env.REACT_APP_AUTH_API}/login`,
                loginForm
            );
            await toast.success(
                "Zalogowano pomyślnie.",{
                    autoClose: 3000,
                }
            );

            // Retrieve the token from the response's containedObject
            const token = response.data.containedObject;
            // Save the token to local storage
            localStorage.setItem("token", token);
            setUserToken(token);
            setGuest(false);

            // try {
            //     // Make a POST request to decodeJWT endpoint with the token
            //     const response = await axios.post(
            //         "http://localhost:8080/app/auth/decodeJWT",
            //         token
            //     );
            //     localStorage.setItem(
            //         "springUserId",
            //         response.data.containedObject.SpringUserId
            //     );
            // } catch (error) {
            //     console.error("Error decoding JWT:", error);
            //     toast.error(
            //         "Błąd odczytu JWT. Proszę spróbować później",{
            //             autoClose: 3000,
            //         }
            //     );
            // }

            // Reset form data after successful login
            setLoginForm({
                email: "",
                password: "",
            });
            location.pathname = "/check";
            navigate("/check");
            window.location.reload();
        } catch (error) {
            console.error("Error logging in:", error);
            await toast.error(
                error.response
                    ? error.response.data.message
                        ? error.response.data.message
                        : "Wystąpił błąd przy logowaniu."
                    : "Wystąpił błąd przy logowaniu",{
                    autoClose: 3000,
                }
            );
        }
    };

    const loginWithGoogle = async () => {
        localStorage.setItem("isGoogleLogin", JSON.stringify(true));
        window.location.href = `${process.env.REACT_APP_AUTH_API}/oauth2/authorization/google`;
    };

    return (
        <div className="login-view view">
            <div className="card">
                <div className="card-header">
                    <h2 className="card-title center-title large-title">Zaloguj się</h2>
                </div>
                <div className="card-body">
                    <form onSubmit={handleSubmit}>
                        <div className="form-row">
                                <label
                                    className="input-label"
                                    htmlFor="enterUsername"
                                    data-testid="usernameLabelTest"
                                >
                                    E-mail
                                </label>
                            <div className={"form-validation-input"}>
                                <input
                                    id="enterUsername"
                                    name="email" // Add name attribute
                                    className="form-control"
                                    type="email"
                                    value={loginForm.email}
                                    onChange={handleChange}
                                    placeholder="E-mail..."
                                    required
                                />
                                <p
                                    className={isEmailValid ? "text-success" : "text-danger"}
                                    style={{
                                        visibility:
                                            loginForm.email !== "" ? "visible" : "hidden",
                                    }}
                                >
                                    {isEmailValid
                                        ? ""
                                        : "Niepoprawny format e-mail"}
                                </p>
                            </div>
                        </div>
                        <div className="form-row password-input-container">
                            <label className="input-label" htmlFor="enterPassword">
                                Hasło
                            </label>
                            <div className={"form-validation-input"}>
                                <input
                                    id="enterPassword"
                                    name="password" // Add name attribute
                                    className="form-control password-input"
                                    type={isPasswordVisible ? "text" : "password"}
                                    value={loginForm.password}
                                    onChange={handleChange}
                                    placeholder="Hasło..."
                                    required
                                />
                                <p
                                    className={isPasswordValid ? "text-success" : "text-danger"}
                                    style={{
                                        visibility:
                                            loginForm.password !== "" ? "visible" : "hidden",
                                    }}
                                >
                                    {isPasswordValid
                                        ? ""
                                        : (loginForm.password ==='' ?
                                            "Hasło jest wymagane"
                                            : "Nieprawidłowe hasło, zasady: 8-20 znaków, jedna wielka litera, jedna mała litera, dodatkowy znak i liczba")}
                                </p>
                            </div>
                            <div
                                    className="show-password-btn"
                                    onClick={() => setIsPasswordVisible(!isPasswordVisible)}
                                    data-testid="togglePasswordVisibility"
                                >
                                    {isPasswordVisible ? (
                                        <Icons.FiEye />
                                    ) : (
                                        <Icons.FiEyeOff />
                                    )}
                            </div>
                        </div>
                        <div className="form-row">
                                <input
                                    type="checkbox"
                                    id="stay-logged-in"
                                    checked={isRememberMeChecked}
                                    onChange={() => {
                                        setIsRememberMeChecked(!isRememberMeChecked);
                                    }}
                                />
                                <label htmlFor="stay-logged-in"> Zapamiętaj mnie</label>
                        </div>
                        <div className="form-row">
                            <div
                                    className="btn-container float-right"
                                    style={{ display: "flex", flexDirection: "row" }}
                                >
                                <button
                                    className="btn submit-btn btn-primary"
                                    onClick={loginWithGoogle}
                                >
                                    Kontynuuj przez Google &nbsp;
                                    <IconsFa.FaGoogle></IconsFa.FaGoogle>
                                </button>
                                <button type="submit" className="btn submit-btn btn-success">
                                        Login
                                    </button>
                                </div>
                        </div>
                        <div className="form-row">
                            <hr />
                        </div>
                        <div className="form-row">
                            <div className={"link-holder"}>
                            {/*    <a className="login-link" href="/register">*/}
                            {/*        <label>Don't have an account? Register!</label>*/}
                            {/*    </a>*/}
                                <a className="login-link" href="/reset-password">
                                    <label>Nie pamiętasz hasła?</label>
                                </a></div>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    );
}
