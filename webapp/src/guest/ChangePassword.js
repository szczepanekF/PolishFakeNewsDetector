import "../css/user.css";
import * as Icons from "react-icons/fi";
import React, { useEffect, useState } from "react";
import axios from "axios";
import { useLocation, useNavigate } from "react-router-dom";
import { toast } from "react-toastify";

export default function ChangePassword({ userId = null, isSeparate = true }) {
    const navigate = useNavigate();
    const location = useLocation();
    if (isSeparate) {
        const parts = location.pathname.split("/");
        userId = parts[2];
    }

    const [changePasswordForm, setChangePasswordForm] = useState({
        newPassword: "",
        newPasswordRepeat: "",
        userId: userId,
    });
    const [isPasswordVisible, setIsPasswordVisible] = useState(false);
    const [isPasswordRepeatVisible, setIsPasswordRepeatVisible] = useState(false);
    const [isPasswordValid, setIsPasswordValid] = useState(false);
    const [isRepeatPasswordValid, setIsRepeatPasswordValid] = useState(false);
    const [showPassMessage, setShowPassMessage] = useState(false);
    const [showRepPassMessage, setShowRepPassMessage] = useState(false);

    const setNewPassword = async (e) => {
        e.preventDefault();
        if (
            changePasswordForm.newPassword !== changePasswordForm.newPasswordRepeat
        ) {
            toast.error(
                "Hasła muszą byc takie same",
                "Podane i powtórzone hasła nie są zgodne",
                3000
            );
            return;
        }
        try {
            const body = {
                userId: Number(changePasswordForm.userId),
                newPassword: changePasswordForm.newPassword,
            };
            const response = await axios.post(
                "http://localhost:8080/app/auth/changePassword",
                body
            );
            if (response.status === 200) {
                location.pathname = "/login";
                navigate("/login");
            }
        } catch (error) {
            console.error("Error while resetting password: ", error);
            toast.error(
                "Wystąpił bład przy zmianie hasła",
                "Błąd resetu hasła",
                3000
            );
        }
    };

    const handlePasswordChange = (e) => {
        setChangePasswordForm({
            ...changePasswordForm,
            [e.target.name]: e.target.value,
        });

    };

    const handleRepeatPasswordChange = (e) => {
        setChangePasswordForm({
            ...changePasswordForm,
            [e.target.name]: e.target.value,
        });

    };

    const passwordValidation = (e) => {
        setIsPasswordValid(e.target.checkValidity());
        if(e.target.checkValidity() && e.target.classList.contains('is-invalid')){
            e.target.classList.remove('is-invalid');
        } else if(!e.target.checkValidity() && !e.target.classList.contains('is-invalid')) {
            e.target.classList.add('is-invalid');
        }
        setShowPassMessage(true);
    }

    const repeatPasswordValidation = (e) => {
        setIsRepeatPasswordValid(e.target.checkValidity());
        if(e.target.checkValidity() && e.target.classList.contains('is-invalid')){
            e.target.classList.remove('is-invalid');
        } else if(!e.target.checkValidity() && !e.target.classList.contains('is-invalid')) {
            e.target.classList.add('is-invalid');
        }
        setShowRepPassMessage(true);
    }

    return (
        <div className="password-reset-view">
            <div className="card">
                <div className="card-header">
                    <h2 className="card-title center-title large-title">
                        Zmień hasło
                    </h2>
                </div>
                <div className="card-body">
                    <form id="change-password-form">
                        <div className="row">
                            <div className="col-md-3 label-col">
                                <label className="input-label" htmlFor="newPassword">
                                    Nowe hasło
                                </label>
                            </div>
                            <div className="col-md-8" style={{ paddingRight: 0 }} data-testid="newpassword">
                                <input
                                    id="newPassword"
                                    name="newPassword" // Add name attribute
                                    className="form-control password-input"
                                    type={isPasswordVisible ? "text" : "password"}
                                    value={changePasswordForm.newPassword}
                                    onChange={handlePasswordChange}
                                    onBlur={passwordValidation}
                                    placeholder="Wprowadź hasło..."
                                    pattern="^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[\W_]).{8,20}$"
                                    required
                                />
                                <p
                                    className={isPasswordValid ? "text-success" : "text-danger"}
                                    style={{visibility: showPassMessage ? "visible" : "hidden"}}
                                >
                                    {isPasswordValid
                                        ? "Prawidłowe hasło"
                                        : (changePasswordForm.newPassword ==='' ?
                                            "Hasło jest wymagane"
                                            : "Nieprawidłowe hasło, zasady: 8-20 znaków, jedna wielka litera, jedna mała litera, dodatkowy znak i liczba")}
                                </p>
                            </div>
                            <div className="col-md-1 show-password">
                                <div
                                    className="show-password-btn"
                                    onClick={() => setIsPasswordVisible(!isPasswordVisible)}
                                    data-testid="showpasswordbtn"
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
                            <div className="col-md-3 label-col">
                                <label className="input-label" htmlFor="newPasswordRepeat">
                                    Powtórz nowe hasło
                                </label>
                            </div>
                            <div className="col-md-8" style={{ paddingRight: 0 }}>
                                <input
                                    id="newPasswordRepeat"
                                    name="newPasswordRepeat" // Add name attribute
                                    className="form-control password-input"
                                    type={isPasswordRepeatVisible ? "text" : "password"}
                                    value={changePasswordForm.newPasswordRepeat}
                                    onChange={handleRepeatPasswordChange}
                                    onBlur={repeatPasswordValidation}
                                    placeholder="Powtórz hasło..."
                                    pattern="^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[\W_]).{8,20}$"
                                    required
                                />
                                <p
                                    className={isRepeatPasswordValid ? "text-success" : "text-danger"}
                                    style={{visibility: showRepPassMessage ? "visible" : "hidden"}}
                                >
                                    {isRepeatPasswordValid
                                        ? "Prawidłowe hasło"
                                        : (changePasswordForm.newPasswordRepeat ==='' ?
                                            "Hasło jest wymagane"
                                            : "Nieprawidłowe hasło, zasady: 8-20 znaków, jedna wielka litera, jedna mała litera, dodatkowy znak i liczba")}
                                </p>
                            </div>
                            <div className="col-md-1 show-password">
                                <div
                                    className="show-password-btn"
                                    onClick={() =>
                                        setIsPasswordRepeatVisible(!isPasswordRepeatVisible)
                                    }
                                >
                                    {isPasswordRepeatVisible ? (
                                            <Icons.FiEye />
                                        ) : (
                                            <Icons.FiEyeOff />
                                        )}
                                </div>
                            </div>
                        </div>
                        <div className="row">
                            <div className="col-md-12">
                                <div className="btn-container float-right">
                                    <button
                                        className="btn submit-btn btn-success"
                                        onClick={setNewPassword}
                                    >
                                        Zmień hasło
                                    </button>
                                </div>
                            </div>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    );
}
