import Card from "../components/employee/Card.jsx";
import Sidebar from "../components/layout/Sidebar.jsx";
import "./AdminDashboardPage.scss";

export default function AdminDashboardPage() {
    return (
        <section className="admin-dashboard-container">
            <Sidebar />
            <Card employeeId={1} />
        </section>
    )
}