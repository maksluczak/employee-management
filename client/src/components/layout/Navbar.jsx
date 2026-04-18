import "./Navbar.scss";
import {useAuth} from "../../context/useAuth.js";

export default function Navbar() {
    const { user, logout } = useAuth();

    return (
        <nav className="navbar">
            <div className="navbar-brand">
                <img src="../../../public/favicon.svg" alt="logo" className="logo-img" />
                <span className="app-name">EMS</span>
            </div>

            {user && (
                <div className="navbar-nav">
                    <button className="logout-btn" onClick={logout}>
                        <span>Logout</span>
                        <svg
                            width="20" height="20"
                            viewBox="0 0 24 24" fill="none"
                            stroke="currentColor" strokeWidth="2"
                        >
                            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
                            <polyline points="16 17 21 12 16 7" />
                            <line x1="21" y1="12" x2="9" y2="12" />
                        </svg>
                    </button>
                </div>
            )}
        </nav>
    )
}