import "./Card.scss";
import {useEffect, useState} from "react";
import {apiClient} from "../../api/apiClient.js";

export default function Card({ employeeId }) {
    const [employee, setEmployee] = useState(null);
    const [imgSrc, setImgSrc] = useState(null);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        const fetchEmployeeData = async () => {
            setLoading(true);
            try {
                const data = await apiClient.get(`/employees/${employeeId}`);
                setEmployee(data);

                const response = await fetch(`http://localhost:8080/api/v1/employees/${employeeId}/profile-image`, {
                    headers: {
                        'Authorization': `Bearer ${localStorage.getItem('token')}` // Zakładając, że tam trzymasz token
                    }
                });

                if (response.ok) {
                    const blob = await response.blob();
                    const url = URL.createObjectURL(blob);
                    setImgSrc(url);
                }
            } catch (error) {
                console.error("Error fetching employee card data:", error);
            } finally {
                setLoading(false);
            }
        }

        fetchEmployeeData();
        return () => {
            if (imgSrc) URL.revokeObjectURL(imgSrc);
        };
    }, [employeeId, imgSrc]);

    if (loading) return <div className="card-loader">Loading...</div>;
    if (!employee) return <div className="card-error">Employee not found</div>;

    return (
        <div className="card">
            <div className="employee-image">
                <img
                    src={imgSrc || 'https://via.placeholder.com/150'}
                    alt={`${employee.firstName} ${employee.lastName}`}
                />
            </div>
            <div className="employee-info">
                <h2 className="employee-info-title">
                    {employee.firstName} {employee.lastName}
                </h2>
                <p className="employee-info-text position">
                    <strong>Position:</strong> {employee.position}
                </p>
                <p className="employee-info-text email">
                    <strong>Email:</strong> {employee.email}
                </p>
                <p className="employee-info-text id-badge">
                    <small>ID: #{employee.id}</small>
                </p>
            </div>
        </div>
    );
}