import * as Icons from "react-icons/fi"; //https://iconscout.com/unicons/free-line-icons/
import logo from "./logo.png";
import logo_mini from "./logo-mini.png";
import { useEffect, useState } from "react";
import axios from "axios";
import { useLocation, useNavigate } from "react-router-dom";

export default function Sidebar() {
    const location = useLocation();
    const navigate = useNavigate();
    const [classes, setClasses] = useState(["sidebar"]);
    // useEffect(() => {
    //     // if (guest && !classes.includes("hidden")) {
    //     //     setClasses([...classes, "hidden"]);
    //     // } else if (!guest) {
    //     //     const cls = classes.filter(c => c !== "hidden");
    //     //     setClasses(cls);
    //     // }
    // }, [classes]);
    useEffect(() => {
        const handleResize = () => {
            if (window.innerWidth < 1200 && window.innerWidth >= 600) {
                setClasses(prev => {
                    const cls = prev.filter(c => c !== "hidden");
                    if (!cls.includes("narrow")) cls.push("narrow");
                    return cls;
                });
            } else if (window.innerWidth < 600) {
                setClasses(prev => {
                    const cls = prev.filter(c => c !== "narrow");
                    if (!cls.includes("hidden")) cls.push("hidden");
                    return cls;
                });
            } else {
                setClasses(["sidebar"]);
            }
        };

        // Call once to initialize based on current width
        handleResize();

        // Add listener
        window.addEventListener("resize", handleResize);

        // Clean up on unmount
        return () => window.removeEventListener("resize", handleResize);
    }, []);


    return (
        <div className={classes.join(" ")}>
            <div className="logo">
                <img src={logo} alt={"PolishFakeNewsDetector"}/>
            </div>
            <div className="logo-mini">
                <img src={logo_mini} alt={"PolishFakeNewsDetector"}/>
            </div>

            <div className="sidebar-items scrollable-y">
                <a className={"item"} href={"/logout"}>
                    <div className={"icon"}><Icons.FiLogOut size={"24px"}
                                                                color={"currentColor"}></Icons.FiLogOut></div>
                    <div className={"label"}>Wyloguj się</div>
                </a>
                <a className={"item"} href={"/check"}>
                    <div className={"icon"}><Icons.FiCheckCircle size={"24px"}
                                                                color={"currentColor"}></Icons.FiCheckCircle></div>
                    <div className={"label"}>Wyloguj się</div>
                </a>
            </div>
        </div>
    );
}
