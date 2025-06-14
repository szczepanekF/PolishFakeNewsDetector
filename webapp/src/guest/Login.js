import * as Icons from "react-icons/fi";
import * as IconsFa from "react-icons/fa6";
import React, { useEffect, useState } from "react";
import axios from "axios";
import { useLocation, useNavigate } from "react-router-dom";
import { toast } from "react-toastify";

export default function Login(setGuest) {
    const location = useLocation();
    const navigate = useNavigate();
    const [loginForm, setLoginForm] = useState({
        usernameOrEmail: "",
        password: "",
    });
    const [isPasswordVisible, setIsPasswordVisible] = useState(false);
    const [isRememberMeChecked, setIsRememberMeChecked] = useState(false);
    const [isPasswordValid, setIsPasswordValid] = useState(false);

    const handleChange = (e) => {
        if (e.target.name === "password") {
            setIsPasswordValid(e.target.checkValidity());
        }
        setLoginForm({ ...loginForm, [e.target.name]: e.target.value });
    };

    const decodeJWToken = async (tokenParam) => {
        try {
            // Make a POST request to decodeJWT endpoint with the token
            const response = await axios.post(
                "http://localhost:8080/app/auth/decodeJWT",
                tokenParam
            );
            localStorage.setItem(
                "springUserId",
                response.data.containedObject.SpringUserId
            );
        } catch (error) {
            console.error("Error decoding JWT:", error);
            toast.error(
                "Błąd odczytu JWT. Proszę spróbować później.", {
                    autoClose: 3000,
                }
            );
        }
    };

    useEffect(() => {
        const isGoogleLogin = JSON.parse(localStorage.getItem("isGoogleLogin"));
        if (isGoogleLogin) {
            const queryParams = new URLSearchParams(location.search);
            const tokenParam = queryParams.get("token");
            if (tokenParam) {
                localStorage.setItem("token", tokenParam);
                decodeJWToken(tokenParam);
                localStorage.setItem("isGoogleLogin", JSON.stringify(false));
                location.pathname = "/";
                navigate("/check");
                window.location.reload();
            }
        }
    }, []);

    useEffect(() => {
        if (localStorage.getItem("userLoginData")) {
            const userLoginData = JSON.parse(localStorage.getItem("userLoginData"));
            setLoginForm({
                usernameOrEmail: userLoginData.usernameOrEmail,
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
                usernameOrEmail: loginForm.usernameOrEmail,
                password: loginForm.password,
                isRememberMeChecked,
            };
            localStorage.setItem("userLoginData", JSON.stringify(userLoginData));
        } else {
            localStorage.removeItem("userLoginData");
        }
        try {
            const response = await axios.post(
                "http://localhost:8080/app/auth/login",
                loginForm
            );
            await toast.success(
                "Zalogowano pomyślnie.",{
                    autoClose: 3000,
                }
            );

            // Retrieve the token from the response's containedObject
            const token = response.data.containedObject.token;
            // Save the token to local storage
            localStorage.setItem("token", token);

            try {
                // Make a POST request to decodeJWT endpoint with the token
                const response = await axios.post(
                    "http://localhost:8080/app/auth/decodeJWT",
                    token
                );
                localStorage.setItem(
                    "springUserId",
                    response.data.containedObject.SpringUserId
                );
            } catch (error) {
                console.error("Error decoding JWT:", error);
                toast.error(
                    "Błąd odczytu JWT. Proszę spróbować później",{
                        autoClose: 3000,
                    }
                );
            }

            // Reset form data after successful login
            setLoginForm({
                usernameOrEmail: "",
                password: "",
            });
            location.pathname = "/";
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
        window.location.href = "http://localhost:8080/oauth2/authorization/google";
    };

    return (
        <div className="login-view view">
            <div className="card">
                <div className="card-header">
                    <h2 className="card-title center-title large-title">Zaloguj się</h2>
                </div>
                <div className="card-body">
                    <form onSubmit={handleSubmit}>
                        <div className="row">
                            <div className="col-md-2">
                                <label
                                    className="input-label"
                                    htmlFor="enterUsername"
                                    data-testid="usernameLabelTest"
                                >
                                    E-mail
                                </label>
                            </div>
                            <div className="col-md-10">
                                <input
                                    id="enterUsername"
                                    name="usernameOrEmail" // Add name attribute
                                    className="form-control"
                                    type="text"
                                    value={loginForm.usernameOrEmail}
                                    onChange={handleChange}
                                    placeholder="E-mail..."
                                    required
                                />
                            </div>
                        </div>
                        <div className="row">
                            <div className="col-md-2">
                                <label className="input-label" htmlFor="enterPassword">
                                    Hasło
                                </label>
                            </div>
                            <div className="col-md-9" style={{ paddingRight: 0 }}>
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
                                        ? "Password valid"
                                        : "Password invalid, rules: 8-20 signs, one uppercase letter, one lowercase letter, extra sign and number"}
                                </p>
                            </div>
                            <div className="col-md-1 show-password">
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
                        </div>
                        <div className="row">
                            <div className="col-md-2"></div>
                            <div className="col-md-10">
                                <input
                                    type="checkbox"
                                    id="stay-logged-in"
                                    checked={isRememberMeChecked}
                                    onChange={() => {
                                        setIsRememberMeChecked(!isRememberMeChecked);
                                    }}
                                />
                                <label htmlFor="stay-logged-in"> Remember me</label>
                            </div>
                        </div>
                        <div className="row">
                            <div className="col-md-12">
                                <div
                                    className="btn-container float-right"
                                    style={{ display: "flex", flexDirection: "row" }}
                                >
                                    <button type="submit" className="btn submit-btn btn-success">
                                        Login
                                    </button>
                                    <button
                                        className="btn submit-btn btn-success"
                                        onClick={loginWithGoogle}
                                    >
                                        Continue with Google
                                        <IconsFa.FaGoogle></IconsFa.FaGoogle>
                                    </button>
                                </div>
                            </div>
                        </div>
                        <div className="row">
                            <hr />
                        </div>
                        <div className="row">
                            {/*<div className="col-md-6">*/}
                            {/*    <a className="login-link" href="/register">*/}
                            {/*        <label>Don't have an account? Register!</label>*/}
                            {/*    </a>*/}
                            {/*</div>*/}
                            <div className="col-md-12">
                                <a className="login-link" href="/reset-password">
                                    <label>Nie pamiętasz hasła?</label>
                                </a>
                            </div>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    );
}
