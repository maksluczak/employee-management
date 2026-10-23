import { useEffect, useState } from "react";
import { api } from "../api.js";
import Avatar from "../components/Avatar.jsx";

export default function EmployeePage() {
    const [me, setMe] = useState(null);
    const [error, setError] = useState(null);

    useEffect(() => {
        api.get("/employees/me").then(setMe).catch((err) => setError(err.message));
    }, []);

    if (error) return <p className="alert">{error}</p>;
    if (!me) return <p className="muted">Loading…</p>;

    return (
        <section className="card profile">
            <Avatar path="/employees/me/profile-image" size={96} />
            <div>
                <h1>{me.firstName} {me.lastName}</h1>
                <p>{me.position}</p>
                <p className="muted">{me.email} · #{me.id}</p>
            </div>
        </section>
    );
}
