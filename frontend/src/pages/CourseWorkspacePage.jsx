import { useEffect, useMemo, useState } from "react";
import { useParams } from "react-router-dom";
import { api } from "../api/client";
import { useAuth } from "../context/AuthContext";

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
const TAB_ITEMS = [
  { id: "overview", label: "Overview" },
  { id: "content", label: "Course Content" },
  { id: "community", label: "Forum + Wiki" },
  { id: "assessment", label: "Quiz + Assignment" },
  { id: "projects", label: "Group Projects" },
  { id: "reflection", label: "Blog + Journal" },
];

export default function CourseWorkspacePage() {
  const { courseId } = useParams();
  const { user } = useAuth();
  const [activeTab, setActiveTab] = useState("overview");
  const [course, setCourse] = useState(null);
  const [steps, setSteps] = useState([]);
  const [threads, setThreads] = useState([]);
  const [posts, setPosts] = useState([]);
  const [wikiPages, setWikiPages] = useState([]);
  const [quizzes, setQuizzes] = useState([]);
  const [quizQuestions, setQuizQuestions] = useState([]);
  const [assignments, setAssignments] = useState([]);
  const [submissionsByAssignment, setSubmissionsByAssignment] = useState({});
  const [projects, setProjects] = useState([]);
  const [reflections, setReflections] = useState([]);
  const [bloom, setBloom] = useState(null);
  const [selectedThreadId, setSelectedThreadId] = useState("");
  const [selectedQuizId, setSelectedQuizId] = useState("");
  const [quizAnswers, setQuizAnswers] = useState({});
  const [progressInput, setProgressInput] = useState(0);
  const [status, setStatus] = useState("");
  const [error, setError] = useState("");

  const [stepForm, setStepForm] = useState({
    title: "",
    description: "",
    bloomLevel: "REMEMBER",
    activityType: "RESOURCE",
    stepOrder: 1,
    resourceUrl: "",
  });
  const [threadForm, setThreadForm] = useState({ title: "", prompt: "" });
  const [postForm, setPostForm] = useState({ content: "" });
  const [wikiForm, setWikiForm] = useState({ title: "", content: "" });
  const [reflectionForm, setReflectionForm] = useState({ type: "BLOG", title: "", content: "" });
  const [quizForm, setQuizForm] = useState({ title: "", instructions: "" });
  const [questionForm, setQuestionForm] = useState({
    prompt: "",
    optionA: "",
    optionB: "",
    optionC: "",
    optionD: "",
    correctOption: "A",
  });
  const [assignmentForm, setAssignmentForm] = useState({
    title: "",
    instructions: "",
    dueDate: "",
  });
  const [assignmentSubmission, setAssignmentSubmission] = useState({});
  const [projectForm, setProjectForm] = useState({
    name: "",
    description: "",
    dueDate: "",
  });

  const canManageCourse = useMemo(() => {
    if (!course || !user) return false;
    if (user.role === "ADMIN") return true;
    return user.role === "INSTRUCTOR" && Number(user.userId) === Number(course.instructor?.id);
  }, [course, user]);

  const getError = (err) => err?.response?.data?.error || "Something went wrong. Please retry.";

  const load = async () => {
    setError("");
    try {
      const [c, s, t, w, q, a, p, r, b] = await Promise.all([
        api.get(`/courses/${courseId}`),
        api.get(`/courses/${courseId}/steps`),
        api.get(`/courses/${courseId}/forum/threads`),
        api.get(`/courses/${courseId}/wiki`),
        api.get(`/courses/${courseId}/quizzes`),
        api.get(`/courses/${courseId}/assignments`),
        api.get(`/courses/${courseId}/projects`),
        api.get(`/courses/${courseId}/reflections`),
        api.get(`/courses/${courseId}/bloom-coverage`),
      ]);

      setCourse(c.data);
      setSteps(s.data);
      setThreads(t.data);
      setWikiPages(w.data);
      setQuizzes(q.data);
      setAssignments(a.data);
      setProjects(p.data);
      setReflections(r.data);
      setBloom(b.data);
      setStepForm((prev) => ({
        ...prev,
        stepOrder: (s.data?.length || 0) + 1,
      }));
      setProgressInput(c.data?.enrollment?.progressPercent || 0);
    } catch (err) {
      setError(getError(err));
    }
  };

  useEffect(() => {
    load();
  }, [courseId]);

  useEffect(() => {
    if (!selectedThreadId) {
      setPosts([]);
      return;
    }
    api
      .get(`/forum/threads/${selectedThreadId}/posts`)
      .then((res) => setPosts(res.data))
      .catch(() => setError("Could not load thread posts."));
  }, [selectedThreadId]);

  useEffect(() => {
    if (!selectedQuizId) {
      setQuizQuestions([]);
      setQuizAnswers({});
      return;
    }
    api
      .get(`/quizzes/${selectedQuizId}/questions`)
      .then((res) => {
        setQuizQuestions(res.data);
        setQuizAnswers({});
      })
      .catch(() => setError("Could not load quiz questions."));
  }, [selectedQuizId]);

  useEffect(() => {
    if (!canManageCourse || assignments.length === 0) {
      setSubmissionsByAssignment({});
      return;
    }
    Promise.all(
      assignments.map((assignment) =>
        api
          .get(`/assignments/${assignment.id}/submissions`)
          .then((res) => [assignment.id, res.data])
          .catch(() => [assignment.id, []])
      )
    ).then((rows) => {
      setSubmissionsByAssignment(Object.fromEntries(rows));
    });
  }, [canManageCourse, assignments]);

  const createStep = async (e) => {
    e.preventDefault();
    setError("");
    setStatus("");
    try {
      await api.post(`/courses/${courseId}/steps`, {
        ...stepForm,
        stepOrder: Number(stepForm.stepOrder),
      });
      setStepForm({
        title: "",
        description: "",
        bloomLevel: "REMEMBER",
        activityType: "RESOURCE",
        stepOrder: Number(stepForm.stepOrder) + 1,
        resourceUrl: "",
      });
      await load();
      setStatus("Course content module added.");
    } catch (err) {
      setError(getError(err));
    }
  };

  const createThread = async (e) => {
    e.preventDefault();
    setError("");
    setStatus("");
    try {
      await api.post(`/courses/${courseId}/forum/threads`, threadForm);
      setThreadForm({ title: "", prompt: "" });
      const res = await api.get(`/courses/${courseId}/forum/threads`);
      setThreads(res.data);
      setStatus("Forum thread created.");
    } catch (err) {
      setError(getError(err));
    }
  };

  const createPost = async (e) => {
    e.preventDefault();
    if (!selectedThreadId) return;
    setError("");
    setStatus("");
    try {
      await api.post(`/forum/threads/${selectedThreadId}/posts`, postForm);
      setPostForm({ content: "" });
      const res = await api.get(`/forum/threads/${selectedThreadId}/posts`);
      setPosts(res.data);
      setStatus("Forum post added.");
    } catch (err) {
      setError(getError(err));
    }
  };

  const createWiki = async (e) => {
    e.preventDefault();
    setError("");
    setStatus("");
    try {
      await api.post(`/courses/${courseId}/wiki`, wikiForm);
      setWikiForm({ title: "", content: "" });
      const res = await api.get(`/courses/${courseId}/wiki`);
      setWikiPages(res.data);
      setStatus("Wiki page saved.");
    } catch (err) {
      setError(getError(err));
    }
  };

  const createReflection = async (e) => {
    e.preventDefault();
    setError("");
    setStatus("");
    try {
      await api.post(`/courses/${courseId}/reflections`, reflectionForm);
      setReflectionForm({ type: "BLOG", title: "", content: "" });
      const res = await api.get(`/courses/${courseId}/reflections`);
      setReflections(res.data);
      setStatus("Reflection published.");
    } catch (err) {
      setError(getError(err));
    }
  };

  const createQuiz = async (e) => {
    e.preventDefault();
    setError("");
    setStatus("");
    try {
      await api.post(`/courses/${courseId}/quizzes`, quizForm);
      setQuizForm({ title: "", instructions: "" });
      const res = await api.get(`/courses/${courseId}/quizzes`);
      setQuizzes(res.data);
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
      await api.post(`/quizzes/${selectedQuizId}/questions`, questionForm);
      setQuestionForm({
        prompt: "",
        optionA: "",
        optionB: "",
        optionC: "",
        optionD: "",
        correctOption: "A",
      });
      const [questionsRes, quizzesRes] = await Promise.all([
        api.get(`/quizzes/${selectedQuizId}/questions`),
        api.get(`/courses/${courseId}/quizzes`),
      ]);
      setQuizQuestions(questionsRes.data);
      setQuizzes(quizzesRes.data);
      setStatus("Quiz question added.");
    } catch (err) {
      setError(getError(err));
    }
  };

  const submitQuiz = async (e) => {
    e.preventDefault();
    if (!selectedQuizId) return;
    setError("");
    setStatus("");
    try {
      const payload = {
        answers: Object.fromEntries(
          Object.entries(quizAnswers).map(([key, val]) => [Number(key), val])
        ),
      };
      const { data } = await api.post(`/quizzes/${selectedQuizId}/attempts`, payload);
      setStatus(`Quiz submitted. Score ${data.score}/${data.total} (${data.percentage}%).`);
    } catch (err) {
      setError(getError(err));
    }
  };

  const createAssignment = async (e) => {
    e.preventDefault();
    setError("");
    setStatus("");
    try {
      await api.post(`/courses/${courseId}/assignments`, {
        title: assignmentForm.title,
        instructions: assignmentForm.instructions,
        dueDate: assignmentForm.dueDate ? new Date(assignmentForm.dueDate).toISOString() : null,
      });
      setAssignmentForm({ title: "", instructions: "", dueDate: "" });
      const res = await api.get(`/courses/${courseId}/assignments`);
      setAssignments(res.data);
      setStatus("Assignment created.");
    } catch (err) {
      setError(getError(err));
    }
  };

  const submitAssignment = async (assignmentId) => {
    const content = assignmentSubmission[assignmentId];
    if (!content) return;
    setError("");
    setStatus("");
    try {
      await api.post(`/assignments/${assignmentId}/submissions`, { content });
      setAssignmentSubmission((prev) => ({ ...prev, [assignmentId]: "" }));
      setStatus("Assignment submitted.");
      if (canManageCourse) {
        const res = await api.get(`/assignments/${assignmentId}/submissions`);
        setSubmissionsByAssignment((prev) => ({ ...prev, [assignmentId]: res.data }));
      }
    } catch (err) {
      setError(getError(err));
    }
  };

  const createProject = async (e) => {
    e.preventDefault();
    setError("");
    setStatus("");
    try {
      await api.post(`/courses/${courseId}/projects`, {
        name: projectForm.name,
        description: projectForm.description,
        dueDate: projectForm.dueDate ? new Date(projectForm.dueDate).toISOString() : null,
      });
      setProjectForm({ name: "", description: "", dueDate: "" });
      const res = await api.get(`/courses/${courseId}/projects`);
      setProjects(res.data);
      setStatus("Group project created.");
    } catch (err) {
      setError(getError(err));
    }
  };

  const enroll = async () => {
    setError("");
    setStatus("");
    try {
      await api.post(`/courses/${courseId}/enroll`);
      await load();
      setStatus("Enrolled successfully.");
    } catch (err) {
      setError(getError(err));
    }
  };

  const updateProgress = async () => {
    setError("");
    setStatus("");
    try {
      await api.post(`/courses/${courseId}/progress?completedSteps=${Number(progressInput)}`);
      await load();
      setStatus("Progress updated.");
    } catch (err) {
      setError(getError(err));
    }
  };

  const sortedSteps = useMemo(
    () => [...steps].sort((a, b) => (a.stepOrder || 0) - (b.stepOrder || 0)),
    [steps]
  );

  if (!course) return <div className="page">Loading...</div>;

  return (
    <div className="page">
      <section className="card">
        <h1>{course.title}</h1>
        <p>{course.description}</p>
        <small>Instructor: {course.instructor?.name}</small>
        <div className="row-actions">
          {user?.role === "STUDENT" && !course.enrollment && (
            <button onClick={enroll}>Enroll in Course</button>
          )}
          {course.enrollment && <div className="pill">Progress: {course.enrollment.progressPercent}%</div>}
          {user?.role === "STUDENT" && course.enrollment && (
            <>
              <input
                type="number"
                min="0"
                max={sortedSteps.length || 100}
                value={progressInput}
                onChange={(e) => setProgressInput(e.target.value)}
              />
              <button onClick={updateProgress}>Update Progress</button>
            </>
          )}
        </div>
        {error && <div className="error">{error}</div>}
        {status && <div className="success">{status}</div>}
      </section>

      <section className="card">
        <div className="tab-row">
          {TAB_ITEMS.map((tab) => (
            <button
              key={tab.id}
              type="button"
              className={`tab-btn ${activeTab === tab.id ? "active" : ""}`}
              onClick={() => setActiveTab(tab.id)}
            >
              {tab.label}
            </button>
          ))}
        </div>
      </section>

      {activeTab === "overview" && (
        <section className="card">
          <h2>Course Snapshot</h2>
          <div className="stats-grid">
            <article>
              <span>Modules</span>
              <strong>{steps.length}</strong>
            </article>
            <article>
              <span>Quizzes</span>
              <strong>{quizzes.length}</strong>
            </article>
            <article>
              <span>Assignments</span>
              <strong>{assignments.length}</strong>
            </article>
            <article>
              <span>Wiki Pages</span>
              <strong>{wikiPages.length}</strong>
            </article>
            <article>
              <span>Forum Threads</span>
              <strong>{threads.length}</strong>
            </article>
            <article>
              <span>Group Projects</span>
              <strong>{projects.length}</strong>
            </article>
          </div>
          {bloom && <p>{bloom.guidance}</p>}
        </section>
      )}

      {activeTab === "content" && (
        <section className="card">
          <h2>Course Content and Video Links</h2>
          {canManageCourse && (
            <form onSubmit={createStep} className="stack-form">
              <input
                placeholder="Module title"
                value={stepForm.title}
                onChange={(e) => setStepForm({ ...stepForm, title: e.target.value })}
                required
              />
              <textarea
                placeholder="Module description"
                value={stepForm.description}
                onChange={(e) => setStepForm({ ...stepForm, description: e.target.value })}
              />
              <div className="inline-grid">
                <select
                  value={stepForm.bloomLevel}
                  onChange={(e) => setStepForm({ ...stepForm, bloomLevel: e.target.value })}
                >
                  {BLOOM_LEVELS.map((level) => (
                    <option key={level}>{level}</option>
                  ))}
                </select>
                <select
                  value={stepForm.activityType}
                  onChange={(e) => setStepForm({ ...stepForm, activityType: e.target.value })}
                >
                  {ACTIVITY_TYPES.map((type) => (
                    <option key={type}>{type}</option>
                  ))}
                </select>
                <input
                  type="number"
                  min="1"
                  value={stepForm.stepOrder}
                  onChange={(e) => setStepForm({ ...stepForm, stepOrder: e.target.value })}
                />
              </div>
              <input
                placeholder="Resource or video URL"
                value={stepForm.resourceUrl}
                onChange={(e) => setStepForm({ ...stepForm, resourceUrl: e.target.value })}
              />
              <button type="submit">Add Module</button>
            </form>
          )}
          <div className="timeline">
            {sortedSteps.map((step) => (
              <article key={step.id}>
                <div>
                  <strong>
                    {step.stepOrder}. {step.title}
                  </strong>
                  <p>{step.description}</p>
                  {step.resourceUrl && (
                    <a href={step.resourceUrl} target="_blank" rel="noreferrer">
                      Open Link
                    </a>
                  )}
                </div>
                <div className="tags">
                  <span>{step.bloomLevel}</span>
                  <span>{step.activityType}</span>
                </div>
              </article>
            ))}
          </div>
        </section>
      )}

      {activeTab === "community" && (
        <section className="card two-col">
          <div>
            <h2>Forum</h2>
            <form onSubmit={createThread} className="stack-form">
              <input
                placeholder="Thread title"
                value={threadForm.title}
                onChange={(e) => setThreadForm({ ...threadForm, title: e.target.value })}
                required
              />
              <input
                placeholder="Prompt"
                value={threadForm.prompt}
                onChange={(e) => setThreadForm({ ...threadForm, prompt: e.target.value })}
              />
              <button type="submit">Create Thread</button>
            </form>
            <select value={selectedThreadId} onChange={(e) => setSelectedThreadId(e.target.value)}>
              <option value="">Choose thread...</option>
              {threads.map((thread) => (
                <option key={thread.id} value={thread.id}>
                  {thread.title}
                </option>
              ))}
            </select>
            <form onSubmit={createPost} className="stack-form">
              <textarea
                placeholder="Write a forum reply"
                value={postForm.content}
                onChange={(e) => setPostForm({ content: e.target.value })}
                required
              />
              <button type="submit" disabled={!selectedThreadId}>
                Post Reply
              </button>
            </form>
            <div className="list">
              {posts.map((post) => (
                <article key={post.id}>
                  <strong>{post.author?.name}</strong>
                  <p>{post.content}</p>
                </article>
              ))}
            </div>
          </div>

          <div>
            <h2>Wiki</h2>
            <form onSubmit={createWiki} className="stack-form">
              <input
                placeholder="Wiki page title"
                value={wikiForm.title}
                onChange={(e) => setWikiForm({ ...wikiForm, title: e.target.value })}
                required
              />
              <textarea
                placeholder="Wiki content"
                value={wikiForm.content}
                onChange={(e) => setWikiForm({ ...wikiForm, content: e.target.value })}
                required
              />
              <button type="submit">Save Wiki Page</button>
            </form>
            <div className="list">
              {wikiPages.map((page) => (
                <article key={page.id}>
                  <strong>{page.title}</strong>
                  <p>{page.content}</p>
                  <small>Updated by {page.updatedBy?.name}</small>
                </article>
              ))}
            </div>
          </div>
        </section>
      )}

      {activeTab === "assessment" && (
        <section className="card two-col">
          <div>
            <h2>Quizzes</h2>
            {canManageCourse && (
              <form onSubmit={createQuiz} className="stack-form">
                <input
                  placeholder="Quiz title"
                  value={quizForm.title}
                  onChange={(e) => setQuizForm({ ...quizForm, title: e.target.value })}
                  required
                />
                <textarea
                  placeholder="Quiz instructions"
                  value={quizForm.instructions}
                  onChange={(e) => setQuizForm({ ...quizForm, instructions: e.target.value })}
                />
                <button type="submit">Create Quiz</button>
              </form>
            )}

            <select value={selectedQuizId} onChange={(e) => setSelectedQuizId(e.target.value)}>
              <option value="">Select quiz...</option>
              {quizzes.map((quiz) => (
                <option key={quiz.id} value={quiz.id}>
                  {quiz.title} ({quiz.questionCount} questions)
                </option>
              ))}
            </select>

            {canManageCourse && (
              <form onSubmit={addQuestion} className="stack-form">
                <textarea
                  placeholder="Question prompt"
                  value={questionForm.prompt}
                  onChange={(e) => setQuestionForm({ ...questionForm, prompt: e.target.value })}
                  required
                />
                <input
                  placeholder="Option A"
                  value={questionForm.optionA}
                  onChange={(e) => setQuestionForm({ ...questionForm, optionA: e.target.value })}
                  required
                />
                <input
                  placeholder="Option B"
                  value={questionForm.optionB}
                  onChange={(e) => setQuestionForm({ ...questionForm, optionB: e.target.value })}
                  required
                />
                <input
                  placeholder="Option C"
                  value={questionForm.optionC}
                  onChange={(e) => setQuestionForm({ ...questionForm, optionC: e.target.value })}
                  required
                />
                <input
                  placeholder="Option D"
                  value={questionForm.optionD}
                  onChange={(e) => setQuestionForm({ ...questionForm, optionD: e.target.value })}
                  required
                />
                <select
                  value={questionForm.correctOption}
                  onChange={(e) => setQuestionForm({ ...questionForm, correctOption: e.target.value })}
                >
                  <option value="A">Correct Answer: A</option>
                  <option value="B">Correct Answer: B</option>
                  <option value="C">Correct Answer: C</option>
                  <option value="D">Correct Answer: D</option>
                </select>
                <button type="submit" disabled={!selectedQuizId}>
                  Add Question
                </button>
              </form>
            )}

            {quizQuestions.length > 0 && (
              <form onSubmit={submitQuiz} className="stack-form">
                {quizQuestions.map((question) => (
                  <article key={question.id} className="quiz-question">
                    <strong>{question.prompt}</strong>
                    {[
                      ["A", question.optionA],
                      ["B", question.optionB],
                      ["C", question.optionC],
                      ["D", question.optionD],
                    ].map(([opt, text]) => (
                      <label key={opt}>
                        <input
                          type="radio"
                          name={`q-${question.id}`}
                          value={opt}
                          checked={quizAnswers[question.id] === opt}
                          onChange={() =>
                            setQuizAnswers((prev) => ({
                              ...prev,
                              [question.id]: opt,
                            }))
                          }
                        />
                        {opt}: {text}
                      </label>
                    ))}
                  </article>
                ))}
                {!canManageCourse && <button type="submit">Submit Quiz</button>}
              </form>
            )}
          </div>

          <div>
            <h2>Assignments</h2>
            {canManageCourse && (
              <form onSubmit={createAssignment} className="stack-form">
                <input
                  placeholder="Assignment title"
                  value={assignmentForm.title}
                  onChange={(e) => setAssignmentForm({ ...assignmentForm, title: e.target.value })}
                  required
                />
                <textarea
                  placeholder="Instructions"
                  value={assignmentForm.instructions}
                  onChange={(e) => setAssignmentForm({ ...assignmentForm, instructions: e.target.value })}
                />
                <input
                  type="datetime-local"
                  value={assignmentForm.dueDate}
                  onChange={(e) => setAssignmentForm({ ...assignmentForm, dueDate: e.target.value })}
                />
                <button type="submit">Create Assignment</button>
              </form>
            )}

            {assignments.map((assignment) => (
              <article key={assignment.id} className="assignment-item">
                <strong>{assignment.title}</strong>
                <p>{assignment.instructions}</p>
                {assignment.dueDate && (
                  <small>Due: {new Date(assignment.dueDate).toLocaleString()}</small>
                )}

                {!canManageCourse && (
                  <>
                    <textarea
                      placeholder="Your solution"
                      value={assignmentSubmission[assignment.id] || ""}
                      onChange={(e) =>
                        setAssignmentSubmission((prev) => ({ ...prev, [assignment.id]: e.target.value }))
                      }
                    />
                    <button onClick={() => submitAssignment(assignment.id)}>Submit Assignment</button>
                  </>
                )}

                {canManageCourse && (
                  <div className="list compact-list">
                    {(submissionsByAssignment[assignment.id] || []).map((submission) => (
                      <article key={submission.id}>
                        <strong>{submission.student?.name}</strong>
                        <p>{submission.content}</p>
                      </article>
                    ))}
                    {(submissionsByAssignment[assignment.id] || []).length === 0 && (
                      <article>No submissions yet.</article>
                    )}
                  </div>
                )}
              </article>
            ))}
          </div>
        </section>
      )}

      {activeTab === "projects" && (
        <section className="card">
          <h2>Group Projects</h2>
          {canManageCourse && (
            <form onSubmit={createProject} className="stack-form">
              <input
                placeholder="Project name"
                value={projectForm.name}
                onChange={(e) => setProjectForm({ ...projectForm, name: e.target.value })}
                required
              />
              <textarea
                placeholder="Project description"
                value={projectForm.description}
                onChange={(e) => setProjectForm({ ...projectForm, description: e.target.value })}
              />
              <input
                type="datetime-local"
                value={projectForm.dueDate}
                onChange={(e) => setProjectForm({ ...projectForm, dueDate: e.target.value })}
              />
              <button type="submit">Create Group Project</button>
            </form>
          )}
          <div className="list">
            {projects.map((project) => (
              <article key={project.id}>
                <strong>{project.name}</strong>
                <p>{project.description}</p>
                {project.dueDate && <small>Due: {new Date(project.dueDate).toLocaleString()}</small>}
              </article>
            ))}
            {projects.length === 0 && <article>No group projects yet.</article>}
          </div>
        </section>
      )}

      {activeTab === "reflection" && (
        <section className="card">
          <h2>Reflection (Blog, Journal, Discussion Summary)</h2>
          <form onSubmit={createReflection} className="stack-form">
            <div className="inline-grid">
              <select
                value={reflectionForm.type}
                onChange={(e) => setReflectionForm({ ...reflectionForm, type: e.target.value })}
              >
                <option value="BLOG">BLOG</option>
                <option value="JOURNAL">JOURNAL</option>
                <option value="DISCUSSION_SUMMARY">DISCUSSION_SUMMARY</option>
              </select>
              <input
                placeholder="Reflection title"
                value={reflectionForm.title}
                onChange={(e) => setReflectionForm({ ...reflectionForm, title: e.target.value })}
              />
            </div>
            <textarea
              placeholder="Write your reflection"
              value={reflectionForm.content}
              onChange={(e) => setReflectionForm({ ...reflectionForm, content: e.target.value })}
              required
            />
            <button type="submit">Publish Reflection</button>
          </form>
          <div className="list">
            {reflections.map((entry) => (
              <article key={entry.id}>
                <strong>{entry.title}</strong>
                <small>
                  {entry.type} by {entry.student?.name}
                </small>
                <p>{entry.content}</p>
              </article>
            ))}
          </div>
        </section>
      )}
    </div>
  );
}
