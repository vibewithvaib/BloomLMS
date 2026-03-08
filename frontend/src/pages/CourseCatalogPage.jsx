import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api } from "../api/client";
import { useAuth } from "../context/AuthContext";

export default function CourseCatalogPage() {
  const { user } = useAuth();
  const [courses, setCourses] = useState([]);

  const load = () => {
    api.get("/courses").then((res) => setCourses(res.data));
  };

  useEffect(() => {
    load();
  }, []);

  const enroll = async (courseId) => {
    await api.post(`/courses/${courseId}/enroll`);
    load();
  };

  return (
    <div className="page">
      <h1>Courses</h1>
      <div className="course-grid">
        {courses.map((course) => (
          <article key={course.id} className="card">
            <h3>{course.title}</h3>
            <p>{course.description}</p>
            <small>Instructor: {course.instructor?.name}</small>
            <div className="row-actions">
              <Link to={`/courses/${course.id}`} className="btn-primary">
                Open
              </Link>
              {user?.role === "STUDENT" && !course.enrollment && (
                <button onClick={() => enroll(course.id)}>Enroll</button>
              )}
            </div>
            {course.enrollment && (
              <div className="pill">Progress: {course.enrollment.progressPercent}%</div>
            )}
          </article>
        ))}
      </div>
    </div>
  );
}
