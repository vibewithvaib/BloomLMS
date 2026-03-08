import { Navigate, Route, Routes } from "react-router-dom";
import AppShell from "./layouts/AppShell";
import { useAuth } from "./context/AuthContext";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import DashboardPage from "./pages/DashboardPage";
import CourseCatalogPage from "./pages/CourseCatalogPage";
import CourseWorkspacePage from "./pages/CourseWorkspacePage";
import CourseDesignerPage from "./pages/CourseDesignerPage";
import AnalyticsPage from "./pages/AnalyticsPage";

function ProtectedRoute({ children }) {
  const { isAuthenticated } = useAuth();
  if (!isAuthenticated) return <Navigate to="/login" replace />;
  return children;
}

function RoleRoute({ roles, children }) {
  const { user } = useAuth();
  if (!roles.includes(user?.role)) return <Navigate to="/dashboard" replace />;
  return children;
}

function ShellRoute({ children }) {
  return (
    <ProtectedRoute>
      <AppShell>{children}</AppShell>
    </ProtectedRoute>
  );
}

export default function App() {
  const { isAuthenticated } = useAuth();

  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route
        path="/dashboard"
        element={
          <ShellRoute>
            <DashboardPage />
          </ShellRoute>
        }
      />
      <Route
        path="/courses"
        element={
          <ShellRoute>
            <CourseCatalogPage />
          </ShellRoute>
        }
      />
      <Route
        path="/courses/:courseId"
        element={
          <ShellRoute>
            <CourseWorkspacePage />
          </ShellRoute>
        }
      />
      <Route
        path="/designer"
        element={
          <ShellRoute>
            <RoleRoute roles={["INSTRUCTOR", "ADMIN"]}>
              <CourseDesignerPage />
            </RoleRoute>
          </ShellRoute>
        }
      />
      <Route
        path="/analytics"
        element={
          <ShellRoute>
            <AnalyticsPage />
          </ShellRoute>
        }
      />
      <Route path="*" element={<Navigate to={isAuthenticated ? "/dashboard" : "/login"} replace />} />
    </Routes>
  );
}
