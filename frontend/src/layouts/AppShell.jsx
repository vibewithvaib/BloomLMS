import { NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function AppShell({ children }) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const onLogout = () => {
    logout();
    navigate("/login");
  };

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">Bloom LMS</div>
        <div className="tag">Simple Course Builder</div>
        <nav>
          <NavLink to="/dashboard">Dashboard</NavLink>
          <NavLink to="/courses">Courses</NavLink>
          {(user?.role === "INSTRUCTOR" || user?.role === "ADMIN") && (
            <NavLink to="/designer">Instructor Studio</NavLink>
          )}
          <NavLink to="/analytics">Analytics</NavLink>
        </nav>
        <div className="profile-card">
          <p>{user?.fullName}</p>
          <small>{user?.role}</small>
          <button onClick={onLogout}>Logout</button>
        </div>
      </aside>
      <main className="main-content">{children}</main>
    </div>
  );
}
