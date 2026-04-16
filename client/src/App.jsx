import {BrowserRouter as Router, Routes, Route} from "react-router-dom";
import LoginPage from "./pages/LoginPage.jsx";
import AdminDashboardPage from "./pages/AdminDashboardPage.jsx";
import EmployeeDashboardPage from "./pages/EmployeeDashboardPage.jsx";
import Navbar from "./components/layout/Navbar.jsx";
import {AuthProvider} from "./context/AuthContext.jsx";

function App() {
    return (
        <Router>
            <AuthProvider>
                <div className='app'>
                    <Navbar />
                    <main className='main'>
                        <Routes>
                            <Route index element={<LoginPage />} />
                            <Route path='/admin-dashboard' element={<AdminDashboardPage />} />
                            <Route path='/employee-dashboard' element={<EmployeeDashboardPage />} />
                        </Routes>
                    </main>
                </div>
            </AuthProvider>
        </Router>
    )
}

export default App
