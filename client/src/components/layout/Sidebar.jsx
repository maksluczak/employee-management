import {useAuth} from "../../context/useAuth.js";
import "./Sidebar.scss"

export default function Sidebar() {
    const { logout } = useAuth();

    return (
        <nav className="sidebar-nav">
            <h2>Dashboard</h2>
            <div className="navbar-brand">
                <img src="/favicon.svg" alt="logo" className="logo-img" />
            </div>

            <div className="navbar-menu">
                <ul>
                    <li>
                        <button className="insert-btn">
                            <span>Create employee</span>
                            <svg
                                width="20" height="20"
                                viewBox="0 0 24 24" fill="currentColor"
                            >
                                <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
                            </svg>
                        </button>
                    </li>
                </ul>
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
        </nav>
    );
}