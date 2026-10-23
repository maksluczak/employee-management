import { useCallback, useEffect, useState } from "react";
import { api } from "../api.js";
import Avatar from "../components/Avatar.jsx";

const EMPTY_FORM = { firstName: "", lastName: "", position: "", role: "EMPLOYEE" };

const fetchData = () => Promise.all([api.get("/employees"), api.get("/employees/me")]);

export default function AdminPage() {
    const [employees, setEmployees] = useState([]);
    const [me, setMe] = useState(null);
    const [created, setCreated] = useState(null);
    const [error, setError] = useState(null);

    const run = useCallback(async (action) => {
        setError(null);
        try {
            await action();
            return true;
        } catch (err) {
            setError(err.message);
            return false;
        }
    }, []);

    const showData = ([list, profile]) => {
        setEmployees(list);
        setMe(profile);
    };

    const load = useCallback(() => run(async () => showData(await fetchData())), [run]);

    useEffect(() => {
        fetchData().then(showData).catch((err) => setError(err.message));
    }, []);

    const createEmployee = (form) => run(async () => {
        setCreated(await api.post("/employees", form));
        await load();
    });

    return (
        <>
            <h1>Employees</h1>
            {error && <p className="alert">{error}</p>}

            <CreateEmployeeForm onSubmit={createEmployee} />

            {created && (
                <div className="card success">
                    <strong>Account created. Pass these credentials to the employee, the password is shown only once:</strong>
                    <p>Username: <code>{created.username}</code> · Password: <code>{created.password}</code></p>
                    <button className="btn" onClick={() => setCreated(null)}>Got it</button>
                </div>
            )}

            <section className="list">
                {employees.map((employee) => (
                    <EmployeeRow
                        key={employee.id}
                        employee={employee}
                        isMe={employee.id === me?.id}
                        run={run}
                        onChange={load}
                    />
                ))}
            </section>
        </>
    );
}

function CreateEmployeeForm({ onSubmit }) {
    const [form, setForm] = useState(EMPTY_FORM);

    const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (await onSubmit(form)) setForm(EMPTY_FORM);
    };

    return (
        <form className="card row-form" onSubmit={handleSubmit}>
            <input name="firstName" placeholder="First name" value={form.firstName} onChange={handleChange} />
            <input name="lastName" placeholder="Last name" value={form.lastName} onChange={handleChange} />
            <input name="position" placeholder="Position" value={form.position} onChange={handleChange} />
            <select name="role" value={form.role} onChange={handleChange}>
                <option value="EMPLOYEE">Employee</option>
                <option value="ADMIN">Admin</option>
            </select>
            <button className="btn btn-green">Add employee</button>
        </form>
    );
}

function EmployeeRow({ employee, isMe, run, onChange }) {
    const [position, setPosition] = useState(employee.position);
    const [imageVersion, setImageVersion] = useState(0);

    const savePosition = () => run(async () => {
        await api.put(`/employees/${employee.id}`, { position });
        await onChange();
    });

    const uploadImage = (e) => {
        const file = e.target.files[0];
        e.target.value = "";
        if (!file) return;
        run(async () => {
            await api.upload(`/employees/${employee.id}/profile-image`, file);
            setImageVersion((v) => v + 1);
        });
    };

    const remove = () => {
        if (!confirm(`Delete ${employee.firstName} ${employee.lastName}?`)) return;
        run(async () => {
            await api.delete(`/employees/${employee.id}`);
            await onChange();
        });
    };

    return (
        <article className="card employee">
            <Avatar path={`/employees/${employee.id}/profile-image`} version={imageVersion} />
            <div className="grow">
                <strong>{employee.firstName} {employee.lastName}</strong> {isMe && <span className="tag">you</span>}
                <p className="muted">{employee.email} · #{employee.id}</p>
            </div>
            <input value={position} onChange={(e) => setPosition(e.target.value)} aria-label="Position" />
            <button className="btn" onClick={savePosition} disabled={position === employee.position}>Save</button>
            <label className="btn">
                Photo
                <input type="file" accept="image/*" hidden onChange={uploadImage} />
            </label>
            <button className="btn btn-pink" onClick={remove} disabled={isMe}>Delete</button>
        </article>
    );
}
