import "./Card.scss";
import { useEffect, useState } from "react";
import { apiClient } from "../../api/apiClient.js";

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

                try {
                    const imageBlobUrl = await apiClient.getEmployeeImage(employeeId);
                    setImgSrc(imageBlobUrl);
                } catch (imageError) {
                    console.warn("Employee image could not be found.");
                    console.error(imageError);
                    setImgSrc(null);
                }
            } catch (error) {
                console.error("Error fetching employee card data:", error);
            } finally {
                setLoading(false);
            }
        }

        fetchEmployeeData();

        return () => {
            if (imgSrc) {
                URL.revokeObjectURL(imgSrc);
            }
        };
    }, [employeeId]);

    if (loading) return <div className="card-loader">Loading...</div>;
    if (!employee) return <div className="card-error">Employee not found</div>;

    return (
        <div className="card">
            <div className="employee-image">
                <img
                    src={imgSrc || '/default-user.png'}
                    alt={`${employee.firstName} ${employee.lastName}`}
                />
            </div>
            <div className="employee-info">
                <div className="id-badge">
                    <small>#{employee.id}</small>
                </div>

                <h2 className="employee-info-title">
                    {employee.firstName} {employee.lastName}
                </h2>

                <p className="employee-info-text email">
                    {employee.email}
                </p>

                <p className="employee-info-text position">
                    {employee.position}
                </p>
            </div>

            <div className="employee-management">
                <button className="employee-update">
                    Update
                </button>
                <button className="employee-delete">
                    Delete
                </button>
            </div>
        </div>
    );
}