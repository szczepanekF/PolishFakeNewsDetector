import {useNavigate} from "react-router-dom";
import {useEffect} from "react";

export default function Logout(setUser, setGuest){
    setUser(null);
    setGuest(true);
    const navigate = useNavigate();
    useEffect(() => {
        navigate("/login");
    }, [navigate]);
}