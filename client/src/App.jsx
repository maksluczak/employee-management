import { useEffect, useState } from "react";
import { getSession, setSession, whenSessionEnds } from "./api.js";
import LoginPage from "./pages/LoginPage.jsx";
import AdminPage from "./pages/AdminPage.jsx";
import EmployeePage from "./pages/EmployeePage.jsx";

export default function App() {
    const [session, setSessionState] = useState(getSession());

    useEffect(() => whenSessionEnds(() => setSessionState(null)), []);

    const logout = () => {
        setSession(null);
        setSessionState(null);
    };

    if (!session) return <LoginPage onLogin={setSessionState} />;

    return (
        <>
            <header className="topbar">
                <strong className="brand">Employee Management</strong>
                <span className="tag">{session.role}</span>
                <button className="btn btn-pink" onClick={logout}>Log out</button>
            </header>
            <main className="container">
                {session.role === "ADMIN" ? <AdminPage /> : <EmployeePage />}
            </main>
        </>
    );
}
