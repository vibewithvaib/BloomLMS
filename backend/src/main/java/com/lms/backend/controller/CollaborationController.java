package com.lms.backend.controller;

import com.lms.backend.dto.ForumPostRequest;
import com.lms.backend.dto.ForumThreadRequest;
import com.lms.backend.dto.GroupProjectRequest;
import com.lms.backend.dto.MemberAssignRequest;
import com.lms.backend.dto.WikiPageRequest;
import com.lms.backend.model.Course;
import com.lms.backend.model.ForumPost;
import com.lms.backend.model.ForumThread;
import com.lms.backend.model.GroupProject;
import com.lms.backend.model.ProjectMembership;
import com.lms.backend.model.Role;
import com.lms.backend.model.User;
import com.lms.backend.model.WikiPage;
import com.lms.backend.repository.CourseRepository;
import com.lms.backend.repository.ForumPostRepository;
import com.lms.backend.repository.ForumThreadRepository;
import com.lms.backend.repository.GroupProjectRepository;
import com.lms.backend.repository.ProjectMembershipRepository;
import com.lms.backend.repository.UserRepository;
import com.lms.backend.repository.WikiPageRepository;
import com.lms.backend.service.AccessControlService;
import com.lms.backend.service.ActivityLogService;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CollaborationController {

    private final CourseRepository courseRepository;
    private final ForumThreadRepository forumThreadRepository;
    private final ForumPostRepository forumPostRepository;
    private final WikiPageRepository wikiPageRepository;
    private final GroupProjectRepository groupProjectRepository;
    private final ProjectMembershipRepository projectMembershipRepository;
    private final UserRepository userRepository;
    private final AccessControlService accessControlService;
    private final ActivityLogService activityLogService;

    @GetMapping("/courses/{courseId}/forum/threads")
    public List<Map<String, Object>> listThreads(@PathVariable Long courseId) {
        Course course = getCourseOrThrow(courseId);
        return forumThreadRepository.findByCourse(course).stream().map(this::toThreadMap).toList();
    }

    @PostMapping("/courses/{courseId}/forum/threads")
    public Map<String, Object> createThread(@PathVariable Long courseId,
                                            @Valid @RequestBody ForumThreadRequest request,
                                            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Course course = getCourseOrThrow(courseId);

        ForumThread thread = new ForumThread();
        thread.setCourse(course);
        thread.setCreatedBy(user);
        thread.setTitle(request.title());
        thread.setPrompt(request.prompt());
        ForumThread saved = forumThreadRepository.save(thread);

        activityLogService.log(user, course, "FORUM_THREAD_CREATED", Map.of("threadId", saved.getId()));
        return toThreadMap(saved);
    }

    @GetMapping("/forum/threads/{threadId}/posts")
    public List<Map<String, Object>> listPosts(@PathVariable Long threadId) {
        ForumThread thread = getThreadOrThrow(threadId);
        return forumPostRepository.findByThreadOrderByCreatedAtAsc(thread).stream().map(this::toPostMap).toList();
    }

    @PostMapping("/forum/threads/{threadId}/posts")
    public Map<String, Object> createPost(@PathVariable Long threadId,
                                          @Valid @RequestBody ForumPostRequest request,
                                          Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        ForumThread thread = getThreadOrThrow(threadId);

        ForumPost post = new ForumPost();
        post.setThread(thread);
        post.setAuthor(user);
        post.setContent(request.content());

        if (request.parentPostId() != null) {
            ForumPost parent = forumPostRepository.findById(request.parentPostId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parent post not found"));
            post.setParentPost(parent);
        }

        ForumPost saved = forumPostRepository.save(post);
        activityLogService.log(user, thread.getCourse(), "FORUM_POST_CREATED", Map.of("postId", saved.getId()));
        return toPostMap(saved);
    }

    @GetMapping("/courses/{courseId}/wiki")
    public List<Map<String, Object>> listWikiPages(@PathVariable Long courseId) {
        Course course = getCourseOrThrow(courseId);
        return wikiPageRepository.findByCourse(course).stream().map(this::toWikiMap).toList();
    }

    @PostMapping("/courses/{courseId}/wiki")
    public Map<String, Object> createWikiPage(@PathVariable Long courseId,
                                              @Valid @RequestBody WikiPageRequest request,
                                              Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Course course = getCourseOrThrow(courseId);

        WikiPage page = new WikiPage();
        page.setCourse(course);
        page.setUpdatedBy(user);
        page.setTitle(request.title());
        page.setContent(request.content());

        WikiPage saved = wikiPageRepository.save(page);
        activityLogService.log(user, course, "WIKI_PAGE_CREATED", Map.of("pageId", saved.getId()));
        return toWikiMap(saved);
    }

    @PutMapping("/wiki/{pageId}")
    public Map<String, Object> updateWikiPage(@PathVariable Long pageId,
                                              @Valid @RequestBody WikiPageRequest request,
                                              Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        WikiPage page = wikiPageRepository.findById(pageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wiki page not found"));

        page.setTitle(request.title());
        page.setContent(request.content());
        page.setUpdatedBy(user);
        WikiPage saved = wikiPageRepository.save(page);

        activityLogService.log(user, page.getCourse(), "WIKI_PAGE_UPDATED", Map.of("pageId", saved.getId()));
        return toWikiMap(saved);
    }

    @GetMapping("/courses/{courseId}/projects")
    public List<Map<String, Object>> listProjects(@PathVariable Long courseId) {
        Course course = getCourseOrThrow(courseId);
        return groupProjectRepository.findByCourse(course).stream().map(this::toProjectMap).toList();
    }

    @PostMapping("/courses/{courseId}/projects")
    public Map<String, Object> createProject(@PathVariable Long courseId,
                                             @Valid @RequestBody GroupProjectRequest request,
                                             Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Course course = getCourseOrThrow(courseId);
        if (!accessControlService.canManageCourse(user, course)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only instructors/admins can create projects");
        }

        GroupProject project = new GroupProject();
        project.setCourse(course);
        project.setName(request.name());
        project.setDescription(request.description());
        project.setDueDate(request.dueDate());
        GroupProject saved = groupProjectRepository.save(project);

        activityLogService.log(user, course, "GROUP_PROJECT_CREATED", Map.of("projectId", saved.getId()));
        return toProjectMap(saved);
    }

    @GetMapping("/projects/{projectId}/members")
    public List<Map<String, Object>> listMembers(@PathVariable Long projectId) {
        GroupProject project = getProjectOrThrow(projectId);
        return projectMembershipRepository.findByProject(project).stream().map(this::toMembershipMap).toList();
    }

    @PostMapping("/projects/{projectId}/members")
    public Map<String, Object> addMember(@PathVariable Long projectId,
                                         @Valid @RequestBody MemberAssignRequest request,
                                         Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        GroupProject project = getProjectOrThrow(projectId);
        if (!accessControlService.canManageCourse(user, project.getCourse())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only course owner or admin can assign members");
        }

        User student = userRepository.findById(request.studentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));
        if (student.getRole() != Role.STUDENT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only students can be project members");
        }

        ProjectMembership membership = new ProjectMembership();
        membership.setProject(project);
        membership.setStudent(student);
        ProjectMembership saved = projectMembershipRepository.save(membership);

        activityLogService.log(user, project.getCourse(), "GROUP_MEMBER_ASSIGNED", Map.of(
                "projectId", project.getId(),
                "studentId", student.getId()
        ));
        return toMembershipMap(saved);
    }

    private Course getCourseOrThrow(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
    }

    private ForumThread getThreadOrThrow(Long threadId) {
        return forumThreadRepository.findById(threadId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Thread not found"));
    }

    private GroupProject getProjectOrThrow(Long projectId) {
        return groupProjectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
    }

    private Map<String, Object> toThreadMap(ForumThread thread) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", thread.getId());
        map.put("courseId", thread.getCourse().getId());
        map.put("title", thread.getTitle());
        map.put("prompt", thread.getPrompt());
        map.put("createdBy", Map.of("id", thread.getCreatedBy().getId(), "name", thread.getCreatedBy().getFullName()));
        map.put("createdAt", thread.getCreatedAt());
        return map;
    }

    private Map<String, Object> toPostMap(ForumPost post) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", post.getId());
        map.put("threadId", post.getThread().getId());
        map.put("content", post.getContent());
        map.put("author", Map.of("id", post.getAuthor().getId(), "name", post.getAuthor().getFullName()));
        map.put("parentPostId", post.getParentPost() == null ? null : post.getParentPost().getId());
        map.put("createdAt", post.getCreatedAt());
        return map;
    }

    private Map<String, Object> toWikiMap(WikiPage page) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", page.getId());
        map.put("courseId", page.getCourse().getId());
        map.put("title", page.getTitle());
        map.put("content", page.getContent());
        map.put("updatedBy", Map.of("id", page.getUpdatedBy().getId(), "name", page.getUpdatedBy().getFullName()));
        map.put("updatedAt", page.getUpdatedAt());
        return map;
    }

    private Map<String, Object> toProjectMap(GroupProject project) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", project.getId());
        map.put("courseId", project.getCourse().getId());
        map.put("name", project.getName());
        map.put("description", project.getDescription());
        map.put("dueDate", project.getDueDate());
        map.put("createdAt", project.getCreatedAt());
        return map;
    }

    private Map<String, Object> toMembershipMap(ProjectMembership membership) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", membership.getId());
        map.put("projectId", membership.getProject().getId());
        map.put("student", Map.of(
                "id", membership.getStudent().getId(),
                "name", membership.getStudent().getFullName(),
                "email", membership.getStudent().getEmail()
        ));
        return map;
    }
}
