import { useEffect, useState } from "react";
import { api } from "../api/client";
import { useAuth } from "../context/AuthContext";

export default function AnalyticsPage() {
  const { user } = useAuth();
  const [my, setMy] = useState(null);
  const [courses, setCourses] = useState([]);
  const [selectedCourseId, setSelectedCourseId] = useState("");
  const [courseAnalytics, setCourseAnalytics] = useState(null);

  useEffect(() => {
    api.get("/analytics/me").then((res) => setMy(res.data));
    api.get("/courses").then((res) => setCourses(res.data));
  }, []);

  useEffect(() => {
    if (!selectedCourseId) {
      setCourseAnalytics(null);
      return;
    }
    api
      .get(`/analytics/courses/${selectedCourseId}/overview`)
      .then((res) => setCourseAnalytics(res.data))
      .catch(() => setCourseAnalytics(null));
  }, [selectedCourseId]);

  return (
    <div className="page two-col">
      <section className="card">
        <h2>My Analytics</h2>
        {my && (
          <div className="stats-grid">
            <article>
              <span>Enrollments</span>
              <strong>{my.enrollments}</strong>
            </article>
            <article>
              <span>Average Progress</span>
              <strong>{Math.round(my.averageProgress || 0)}%</strong>
            </article>
            <article>
              <span>Quiz Avg</span>
              <strong>{Math.round(my.averageQuizScore || 0)}%</strong>
            </article>
            <article>
              <span>Submissions</span>
              <strong>{my.submissionCount}</strong>
            </article>
            <article>
              <span>Reflections</span>
              <strong>{my.reflectionCount}</strong>
            </article>
          </div>
        )}
      </section>

      {(user?.role === "INSTRUCTOR" || user?.role === "ADMIN") && (
        <section className="card">
          <h2>Course Analytics</h2>
          <select value={selectedCourseId} onChange={(e) => setSelectedCourseId(e.target.value)}>
            <option value="">Select course...</option>
            {courses.map((c) => (
              <option key={c.id} value={c.id}>{c.title}</option>
            ))}
          </select>

          {courseAnalytics && (
            <>
              <div className="stats-grid">
                <article>
                  <span>Enrollments</span>
                  <strong>{courseAnalytics.enrollmentCount}</strong>
                </article>
                <article>
                  <span>Avg Progress</span>
                  <strong>{Math.round(courseAnalytics.avgProgress || 0)}%</strong>
                </article>
                <article>
                  <span>Active Participants</span>
                  <strong>{courseAnalytics.activeParticipants}</strong>
                </article>
                <article>
                  <span>Bloom Balanced</span>
                  <strong>{courseAnalytics.bloomBalanced ? "Yes" : "No"}</strong>
                </article>
              </div>

              <h4>Bloom Coverage</h4>
              <div className="stats-grid">
                {Object.entries(courseAnalytics.bloomCoverage || {}).map(([k, v]) => (
                  <article key={k}>
                    <span>{k}</span>
                    <strong>{v}</strong>
                  </article>
                ))}
              </div>

              <h4>Recent Activity</h4>
              <div className="list">
                {(courseAnalytics.recentActivity || []).slice(0, 10).map((item) => (
                  <article key={item.id}>
                    <strong>{item.actionType}</strong>
                    <small>{item.userName}</small>
                    <p>{item.metadata}</p>
                  </article>
                ))}
              </div>
            </>
          )}
        </section>
      )}
    </div>
  );
}
