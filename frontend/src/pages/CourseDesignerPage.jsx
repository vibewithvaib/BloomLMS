import { useEffect, useMemo, useState } from "react";
import { api } from "../api/client";

const BLOOM_LEVELS = ["REMEMBER", "UNDERSTAND", "APPLY", "ANALYZE", "EVALUATE", "CREATE"];
const ACTIVITY_TYPES = [
  "RESOURCE",
  "QUIZ",
  "ASSIGNMENT",
  "FORUM",
  "BLOG",
  "WIKI",
  "GROUP_PROJECT",
  "SELF_ASSESSMENT",
  "PEER_REVIEW",
];
const QUIZ_OPTIONS = ["A", "B", "C", "D"];

export default function CourseDesignerPage() {
  const [courses, setCourses] = useState([]);
  const [selectedCourseId, setSelectedCourseId] = useState("");
  const [newCourse, setNewCourse] = useState({ title: "", description: "" });
  const [newStep, setNewStep] = useState({
    title: "",
    description: "",
    bloomLevel: "REMEMBER",
    activityType: "RESOURCE",
    stepOrder: 1,
    resourceUrl: "",
  });
  const [coverage, setCoverage] = useState(null);
  const [quizzes, setQuizzes] = useState([]);
  const [assignments, setAssignments] = useState([]);
  const [projects, setProjects] = useState([]);
  const [selectedQuizId, setSelectedQuizId] = useState("");
  const [quizQuestions, setQuizQuestions] = useState([]);
  const [newQuiz, setNewQuiz] = useState({ title: "", instructions: "" });
  const [newQuestion, setNewQuestion] = useState({
    prompt: "",
    optionA: "",
    optionB: "",
    optionC: "",
    optionD: "",
    correctOption: "A",
  });
  const [newAssignment, setNewAssignment] = useState({
    title: "",
    instructions: "",
    dueDate: "",
  });
  const [newProject, setNewProject] = useState({
    name: "",
    description: "",
    dueDate: "",
  });
  const [status, setStatus] = useState("");
  const [error, setError] = useState("");

  const loadCourses = () => api.get("/courses").then((res) => setCourses(res.data));
  const selectedCourse = useMemo(
    () => courses.find((course) => String(course.id) === String(selectedCourseId)),
    [courses, selectedCourseId]
  );

  useEffect(() => {
    loadCourses();
  }, []);

  const getError = (err) => err?.response?.data?.error || "Something went wrong. Please retry.";

  const refreshCoverage = async (courseId) => {
    if (!courseId) {
      setCoverage(null);
      return;
    }
    const { data } = await api.get(`/courses/${courseId}/bloom-coverage`);
    setCoverage(data);
  };

  const refreshAuthoringData = async (courseId) => {
    if (!courseId) {
      setQuizzes([]);
      setAssignments([]);
      setProjects([]);
      return;
    }

    const [quizRes, assignmentRes, projectRes] = await Promise.all([
      api.get(`/courses/${courseId}/quizzes`),
      api.get(`/courses/${courseId}/assignments`),
      api.get(`/courses/${courseId}/projects`),
    ]);
    setQuizzes(quizRes.data);
    setAssignments(assignmentRes.data);
    setProjects(projectRes.data);
  };

  useEffect(() => {
    if (selectedCourseId) {
      Promise.all([refreshCoverage(selectedCourseId), refreshAuthoringData(selectedCourseId)]).catch(() => {
        setError("Could not load selected course data.");
      });
    } else {
      setCoverage(null);
      setQuizzes([]);
      setAssignments([]);
      setProjects([]);
      setSelectedQuizId("");
      setQuizQuestions([]);
    }
  }, [selectedCourseId]);

  useEffect(() => {
    if (!selectedQuizId) {
      setQuizQuestions([]);
      return;
    }

    api
      .get(`/quizzes/${selectedQuizId}/questions`)
      .then((res) => setQuizQuestions(res.data))
      .catch(() => setError("Could not load quiz questions."));
  }, [selectedQuizId]);

  useEffect(() => {
    if (!selectedQuizId) return;
    const quizStillExists = quizzes.some((quiz) => String(quiz.id) === String(selectedQuizId));
    if (!quizStillExists) {
      setSelectedQuizId("");
      setQuizQuestions([]);
    }
  }, [quizzes, selectedQuizId]);

  const createCourse = async (e) => {
    e.preventDefault();
    setError("");
    setStatus("");
    try {
      const { data } = await api.post("/courses", newCourse);
      setNewCourse({ title: "", description: "" });
      await loadCourses();
      setSelectedCourseId(String(data.id));
      setStatus("Course created. Continue with module and activity setup.");
    } catch (err) {
      setError(getError(err));
    }
  };

  const addStep = async (e) => {
    e.preventDefault();
    if (!selectedCourseId) return;
    setError("");
    setStatus("");
    try {
      await api.post(`/courses/${selectedCourseId}/steps`, {
        ...newStep,
        stepOrder: Number(newStep.stepOrder),
      });
      setNewStep({
        title: "",
        description: "",
        bloomLevel: "REMEMBER",
        activityType: "RESOURCE",
        stepOrder: Number(newStep.stepOrder) + 1,
        resourceUrl: "",
      });
      await refreshCoverage(selectedCourseId);
      setStatus("Course content block added to learning path.");
    } catch (err) {
      setError(getError(err));
    }
  };

  const publish = async () => {
    if (!selectedCourseId) return;
    setError("");
    setStatus("");
    try {
      await api.patch(`/courses/${selectedCourseId}/publish?published=true`);
      await loadCourses();
      setStatus("Course published. Students can now enroll.");
    } catch (err) {
      setError(getError(err));
    }
  };

  const createQuiz = async (e) => {
    e.preventDefault();
    if (!selectedCourseId) return;
    setError("");
    setStatus("");
    try {
      await api.post(`/courses/${selectedCourseId}/quizzes`, newQuiz);
      setNewQuiz({ title: "", instructions: "" });
      await refreshAuthoringData(selectedCourseId);
      setStatus("Quiz created.");
    } catch (err) {
      setError(getError(err));
    }
  };

  const addQuestion = async (e) => {
    e.preventDefault();
    if (!selectedQuizId) return;
    setError("");
    setStatus("");
    try {
      await api.post(`/quizzes/${selectedQuizId}/questions`, newQuestion);
      setNewQuestion({
        prompt: "",
        optionA: "",
        optionB: "",
        optionC: "",
        optionD: "",
        correctOption: "A",
      });
      const { data } = await api.get(`/quizzes/${selectedQuizId}/questions`);
      setQuizQuestions(data);
      if (selectedCourseId) {
        await refreshAuthoringData(selectedCourseId);
      }
      setStatus("Quiz question added.");
    } catch (err) {
      setError(getError(err));
    }
  };

  const createAssignment = async (e) => {
    e.preventDefault();
    if (!selectedCourseId) return;
    setError("");
    setStatus("");
    try {
      await api.post(`/courses/${selectedCourseId}/assignments`, {
        title: newAssignment.title,
        instructions: newAssignment.instructions,
        dueDate: newAssignment.dueDate ? new Date(newAssignment.dueDate).toISOString() : null,
      });
      setNewAssignment({ title: "", instructions: "", dueDate: "" });
      await refreshAuthoringData(selectedCourseId);
      setStatus("Assignment created.");
    } catch (err) {
      setError(getError(err));
    }
  };

  const createProject = async (e) => {
    e.preventDefault();
    if (!selectedCourseId) return;
    setError("");
    setStatus("");
    try {
      await api.post(`/courses/${selectedCourseId}/projects`, {
        name: newProject.name,
        description: newProject.description,
        dueDate: newProject.dueDate ? new Date(newProject.dueDate).toISOString() : null,
      });
      setNewProject({ name: "", description: "", dueDate: "" });
      await refreshAuthoringData(selectedCourseId);
      setStatus("Group project created.");
    } catch (err) {
      setError(getError(err));
    }
  };

  return (
    <div className="page two-col">
      <section className="card">
        <h2>1. Create Course</h2>
        <form onSubmit={createCourse} className="stack-form">
          <input
            value={newCourse.title}
            placeholder="Course title (example: Database Foundations)"
            onChange={(e) => setNewCourse({ ...newCourse, title: e.target.value })}
            required
          />
          <textarea
            value={newCourse.description}
            placeholder="Simple summary of what students will learn"
            onChange={(e) => setNewCourse({ ...newCourse, description: e.target.value })}
          />
          <button type="submit">Create Course</button>
        </form>

        <hr />

        <h3>2. Select Course</h3>
        <select value={selectedCourseId} onChange={(e) => setSelectedCourseId(e.target.value)}>
          <option value="">Choose...</option>
          {courses.map((c) => (
            <option key={c.id} value={c.id}>
              {c.title} {c.published ? "(Published)" : "(Draft)"}
            </option>
          ))}
        </select>
        {selectedCourse && (
          <div className="hint-card">
            <strong>{selectedCourse.title}</strong>
            <p>{selectedCourse.description}</p>
          </div>
        )}
        {error && <div className="error">{error}</div>}
        {status && <div className="success">{status}</div>}
      </section>

      <section className="card">
        <h2>3. Add Course Modules (Resource, Quiz, Forum, Wiki...)</h2>
        <form onSubmit={addStep} className="stack-form">
          <input
            value={newStep.title}
            placeholder="Module title"
            onChange={(e) => setNewStep({ ...newStep, title: e.target.value })}
            required
          />
          <textarea
            value={newStep.description}
            placeholder="What should learners do in this module?"
            onChange={(e) => setNewStep({ ...newStep, description: e.target.value })}
          />
          <div className="inline-grid">
            <select
              value={newStep.bloomLevel}
              onChange={(e) => setNewStep({ ...newStep, bloomLevel: e.target.value })}
            >
              {BLOOM_LEVELS.map((level) => (
                <option key={level}>{level}</option>
              ))}
            </select>
            <select
              value={newStep.activityType}
              onChange={(e) => setNewStep({ ...newStep, activityType: e.target.value })}
            >
              {ACTIVITY_TYPES.map((t) => (
                <option key={t}>{t}</option>
              ))}
            </select>
            <input
              type="number"
              min="1"
              value={newStep.stepOrder}
              onChange={(e) => setNewStep({ ...newStep, stepOrder: e.target.value })}
            />
          </div>
          <input
            value={newStep.resourceUrl}
            placeholder="Resource or video URL (YouTube/Drive/docs link)"
            onChange={(e) => setNewStep({ ...newStep, resourceUrl: e.target.value })}
          />
          <button type="submit" disabled={!selectedCourseId}>
            Add Module
          </button>
        </form>
        <button className="btn-primary" disabled={!selectedCourseId} onClick={publish}>
          Publish Course
        </button>

        {coverage && (
          <div className="coverage-box">
            <h4>Bloom Coverage</h4>
            <p>{coverage.guidance}</p>
            <div className="stats-grid">
              {Object.entries(coverage.coverage || {}).map(([k, v]) => (
                <article key={k}>
                  <span>{k}</span>
                  <strong>{v}</strong>
                </article>
              ))}
            </div>
          </div>
        )}
      </section>

      <section className="card">
        <h2>4. Build Quizzes</h2>
        <form onSubmit={createQuiz} className="stack-form">
          <input
            value={newQuiz.title}
            placeholder="Quiz title"
            onChange={(e) => setNewQuiz({ ...newQuiz, title: e.target.value })}
            required
          />
          <textarea
            value={newQuiz.instructions}
            placeholder="Quiz instructions"
            onChange={(e) => setNewQuiz({ ...newQuiz, instructions: e.target.value })}
          />
          <button type="submit" disabled={!selectedCourseId}>
            Create Quiz
          </button>
        </form>

        <select value={selectedQuizId} onChange={(e) => setSelectedQuizId(e.target.value)}>
          <option value="">Select quiz to add questions...</option>
          {quizzes.map((quiz) => (
            <option key={quiz.id} value={quiz.id}>
              {quiz.title} ({quiz.questionCount} questions)
            </option>
          ))}
        </select>

        <form onSubmit={addQuestion} className="stack-form">
          <textarea
            value={newQuestion.prompt}
            placeholder="Question prompt"
            onChange={(e) => setNewQuestion({ ...newQuestion, prompt: e.target.value })}
            required
          />
          <input
            value={newQuestion.optionA}
            placeholder="Option A"
            onChange={(e) => setNewQuestion({ ...newQuestion, optionA: e.target.value })}
            required
          />
          <input
            value={newQuestion.optionB}
            placeholder="Option B"
            onChange={(e) => setNewQuestion({ ...newQuestion, optionB: e.target.value })}
            required
          />
          <input
            value={newQuestion.optionC}
            placeholder="Option C"
            onChange={(e) => setNewQuestion({ ...newQuestion, optionC: e.target.value })}
            required
          />
          <input
            value={newQuestion.optionD}
            placeholder="Option D"
            onChange={(e) => setNewQuestion({ ...newQuestion, optionD: e.target.value })}
            required
          />
          <select
            value={newQuestion.correctOption}
            onChange={(e) => setNewQuestion({ ...newQuestion, correctOption: e.target.value })}
          >
            {QUIZ_OPTIONS.map((option) => (
              <option key={option} value={option}>
                Correct Answer: {option}
              </option>
            ))}
          </select>
          <button type="submit" disabled={!selectedQuizId}>
            Add Question
          </button>
        </form>

        {quizQuestions.length > 0 && (
          <div className="list">
            {quizQuestions.map((question, index) => (
              <article key={question.id}>
                <strong>
                  Q{index + 1}. {question.prompt}
                </strong>
                <p>
                  A: {question.optionA} | B: {question.optionB}
                </p>
                <p>
                  C: {question.optionC} | D: {question.optionD}
                </p>
              </article>
            ))}
          </div>
        )}
      </section>

      <section className="card">
        <h2>5. Add Assignments and Group Projects</h2>
        <form onSubmit={createAssignment} className="stack-form">
          <input
            value={newAssignment.title}
            placeholder="Assignment title"
            onChange={(e) => setNewAssignment({ ...newAssignment, title: e.target.value })}
            required
          />
          <textarea
            value={newAssignment.instructions}
            placeholder="Assignment instructions"
            onChange={(e) => setNewAssignment({ ...newAssignment, instructions: e.target.value })}
          />
          <input
            type="datetime-local"
            value={newAssignment.dueDate}
            onChange={(e) => setNewAssignment({ ...newAssignment, dueDate: e.target.value })}
          />
          <button type="submit" disabled={!selectedCourseId}>
            Create Assignment
          </button>
        </form>

        <form onSubmit={createProject} className="stack-form">
          <input
            value={newProject.name}
            placeholder="Project name"
            onChange={(e) => setNewProject({ ...newProject, name: e.target.value })}
            required
          />
          <textarea
            value={newProject.description}
            placeholder="Project description"
            onChange={(e) => setNewProject({ ...newProject, description: e.target.value })}
          />
          <input
            type="datetime-local"
            value={newProject.dueDate}
            onChange={(e) => setNewProject({ ...newProject, dueDate: e.target.value })}
          />
          <button type="submit" disabled={!selectedCourseId}>
            Create Group Project
          </button>
        </form>

        <div className="stats-grid">
          <article>
            <span>Quizzes</span>
            <strong>{quizzes.length}</strong>
          </article>
          <article>
            <span>Assignments</span>
            <strong>{assignments.length}</strong>
          </article>
          <article>
            <span>Group Projects</span>
            <strong>{projects.length}</strong>
          </article>
        </div>
      </section>
    </div>
  );
}
