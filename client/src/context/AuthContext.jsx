import {useCallback, useEffect, useMemo, useState} from "react"
import {useNavigate} from "react-router-dom";
import {AuthContext} from "./useAuth.js"
import {jwtDecode} from "jwt-decode";
import {apiClient} from "../api/apiClient";

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [token, setToken] = useState(null);
    const navigate = useNavigate();

    const setUserFromToken = useCallback((t) => {
        try {
            const decoded = jwtDecode(t);
            setUser({ username: decoded.username });
        } catch {
            setUser(null);
        }
    }, []);

    const logout = useCallback(() => {
        setToken(null);
        setUser(null);
        navigate("/");
    }, [navigate]);

    const refreshToken = useCallback(async () => {
        try {
            const data = await apiClient.get("/auth/refresh");
            if (data?.token) {
                setToken(data.token);
                setUserFromToken(data.token);
                return data.token;
            }
        } catch (err) {
            console.error("Session expired", err);
            logout();
            return null;
        }
    }, [logout, setUserFromToken]);

    useEffect(() => {
        apiClient.setToken(token);
        apiClient.onTokenExpired(refreshToken);
    }, [token, refreshToken]);

    const login = useCallback((token, role, shouldRedirect = true) => {
        setToken(token);
        setUserFromToken(token);
        if (shouldRedirect) {
            navigate(role === "ADMIN" ? "/admin-dashboard" : "/employee-dashboard");
        }
    }, [navigate, setUserFromToken]);

    const value = useMemo(() => ({
        user,
        token,
        login,
        logout,
        refreshToken,
    }), [user, token, login, logout, refreshToken]);

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );
};