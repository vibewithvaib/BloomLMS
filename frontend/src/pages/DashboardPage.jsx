import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api } from "../api/client";
import { useAuth } from "../context/AuthContext";
import BloomLegend from "../components/BloomLegend";

export default function DashboardPage() {
  const { user } = useAuth();
  const [myAnalytics, setMyAnalytics] = useState(null);

  useEffect(() => {
    api.get("/analytics/me").then((res) => setMyAnalytics(res.data)).catch(() => setMyAnalytics(null));
  }, []);

  return (
    <div className="page">
      <div className="hero card">
        <h1>{user?.role} Dashboard</h1>
        <p>
          Build simple courses with resources, quizzes, assignments, forum, wiki, blogs, and group projects.
        </p>
        <div className="hero-actions">
          <Link to="/courses" className="btn-primary">
            Open Courses
          </Link>
          {(user?.role === "INSTRUCTOR" || user?.role === "ADMIN") && (
            <Link to="/designer" className="btn-secondary">
              Open Instructor Studio
            </Link>
          )}
        </div>
      </div>

      <BloomLegend />

      {myAnalytics && (
        <div className="card">
          <h3>My Learning Analytics</h3>
          <div className="stats-grid">
            <article>
              <span>Enrollments</span>
              <strong>{myAnalytics.enrollments}</strong>
            </article>
            <article>
              <span>Average Progress</span>
              <strong>{Math.round(myAnalytics.averageProgress || 0)}%</strong>
            </article>
            <article>
              <span>Average Quiz Score</span>
              <strong>{Math.round(myAnalytics.averageQuizScore || 0)}%</strong>
            </article>
            <article>
              <span>Reflections</span>
              <strong>{myAnalytics.reflectionCount}</strong>
            </article>
          </div>
        </div>
      )}
    </div>
  );
}
