import "./css/main.css";
import "./css/variables.css";
import 'bootstrap/dist/css/bootstrap.min.css';
import logo from './logo.png';

import { useState, useEffect } from "react";
import {
    BrowserRouter as Router,
    Route,
    Routes,
    useLocation,
    useNavigate,
} from "react-router-dom";
import * as Icons from "react-icons/fi";
import { ToastContainer, toast } from "react-toastify";
import Login from "./guest/Login";
import Error from "./Error";
import ResetPassword from "./guest/ResetPassword";
import ChangePassword from "./guest/ChangePassword";
import Sidebar from "./Sidebar";
import Logout from "./guest/Logout";
import Checker from "./detector/Checker";

export default function Base() {
  return (
      <Router>
        <App />
      </Router>
  );
}

function App() {
    const [guest, setGuest] = useState(true);
    // const [page, setPage] = useState("");
    const [user, setUser] = useState(null);

  return (
    <div className={guest ? "layout guest" : "layout"}>

        {guest ? null : <div className="sidebar-icon"><Icons.FiAlignJustify></Icons.FiAlignJustify></div>}
        {guest ? null : <Sidebar guest={guest}></Sidebar>}
        <div className={guest ? "main-content guest" : "main-content"}>
            {guest ?
                <img src={logo} className={"guest-logo"} alt={"PolishFakeNewsDetector"} />
                : null}
            <Routes>
                <Route path="/login" element={<Login setGuest={setGuest} />} />
                <Route path="/logout" element={<Logout setUser={setUser} setGuest={setGuest} />} />
                <Route path="/error" element={<Error />} />
                {/*<Route path="/register" element={<Register />} />*/}
                <Route path="/reset-password" element={<ResetPassword />} />
                <Route
                    path="/change-password/:userId"
                    element={<ChangePassword />}
                />
                <Route path="/check" element={<Checker user={user} setGuest={setGuest} />} />
                <Route path={"*"} element={<Error code={"404"}/>}/>
            </Routes>
        </div>
        <ToastContainer />
    </div>
  );
}

