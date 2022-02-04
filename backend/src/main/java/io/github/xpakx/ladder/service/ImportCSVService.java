package io.github.xpakx.ladder.service;

import io.github.xpakx.ladder.entity.Project;
import io.github.xpakx.ladder.entity.Task;
import io.github.xpakx.ladder.entity.dto.ProjectImport;
import io.github.xpakx.ladder.entity.dto.TaskImport;
import io.github.xpakx.ladder.repository.ProjectRepository;
import io.github.xpakx.ladder.repository.TaskRepository;
import io.github.xpakx.ladder.repository.UserAccountRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ImportCSVService implements ImportServiceInterface {
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final UserAccountRepository userRepository;
    private static final String DELIMITER = ",";
    private static final Pattern isInteger = Pattern.compile("\\d+");

    @Override
    public void importProjectList(Integer userId, String csv) {
        List<ProjectImport> projects = CSVtoProjectList(csv);
        List<Integer> ids = getProjectIdsFromImported(projects);
        List<Integer> parentIds = getParentIdsFromImported(projects);
        parentIds = projectRepository.findIdByOwnerIdAndIdIn(userId, parentIds);
        List<Project> projectsInDb = projectRepository.findByOwnerIdAndIdIn(userId, ids);
        ids = listToIds(projectsInDb);
        Map<Integer, Project> hashMap = new HashMap<>();
        Map<Project, Integer> parentMap = new HashMap<>();
        List<Project> toSave = transformToProjects(userId, projects, projectsInDb, hashMap, parentMap);
        appendParentsToProjects(ids, parentIds, toSave, hashMap, parentMap);
        projectRepository.saveAll(toSave);
    }

    private List<Project> transformToProjects(Integer userId, List<ProjectImport> projects, List<Project> projectsInDb, Map<Integer, Project> hashMap, Map<Project, Integer> parentMap) {
        List<Project> toSave = new ArrayList<>();
        for(ProjectImport project : projects) {
            Project projectToSave = getProjectFromDbOrNewProject(projectsInDb, project);
            copyFieldsToProject(projectToSave, project, userId);
            if(project.getId() != null) {
                hashMap.put(project.getId(), projectToSave);
            }
            parentMap.put(projectToSave, project.getParentId());
            toSave.add(projectToSave);
        }
        return toSave;
    }

    private void appendParentsToProjects(List<Integer> ids, List<Integer> parentIds, List<Project> toSave, Map<Integer, Project> hashMap, Map<Project, Integer> parentMap) {
        for(Project project : toSave) {
            Project parent = null;
            if(parentMap.containsKey(project)) {
                Integer parentId = parentMap.get(project);
                if(hashMap.containsKey(parentId)) {
                    parent = hashMap.get(parentId);
                } else if(ids.contains(parentId) || parentIds.contains(parentId)) {
                    parent = projectRepository.getById(parentId);
                }
            }
            project.setParent(parent);
        }
    }

    private List<Integer> listToIds(List<Project> projectsInDb) {
        return projectsInDb.stream()
                .map(Project::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Project getProjectFromDbOrNewProject(List<Project> projectsInDb, ProjectImport project) {
        return projectsInDb.stream()
                .filter((a) -> Objects.equals(a.getId(), project.getId()))
                .findAny()
                .orElse(new Project());
    }

    private void copyFieldsToProject(Project projectToSave, ProjectImport project, Integer userId) {
        projectToSave.setName(project.getName());
        projectToSave.setColor(project.getColor());
        projectToSave.setFavorite(project.isFavorite());
        projectToSave.setArchived(project.isArchived());
        projectToSave.setGeneralOrder(project.getGeneralOrder());
        projectToSave.setOwner(userRepository.getById(userId));
    }

    private List<Integer> getParentIdsFromImported(List<ProjectImport> projects) {
        return projects.stream()
                .map(ProjectImport::getParentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private List<Integer> getProjectIdsFromImported(List<ProjectImport> projects) {
        return projects.stream()
                .map(ProjectImport::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public void importTasksFromProjectById(Integer userId, Integer projectId, String csv) {
        List<TaskImport> tasks = CSVtoTaskList(csv);
        List<Integer> ids = tasks.stream()
                .map(TaskImport::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        List<Integer> parentIds = tasks.stream()
                .map(TaskImport::getParentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        parentIds = taskRepository.findIdByOwnerIdAndIdIn(userId, parentIds);
        List<Task> tasksInDb = taskRepository.findByOwnerIdAndIdIn(userId, ids);
        ids = tasksInDb.stream()
                .map(Task::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        List<Task> toSave = new ArrayList<>();
        Map<Integer, Task> hashMap = new HashMap<>();
        Map<Task, Integer> parentMap = new HashMap<>();
        for(TaskImport task : tasks) {
            Task taskToSave = tasksInDb.stream()
                    .filter((a) -> Objects.equals(a.getId(), task.getId()))
                    .findAny()
                    .orElse(new Task());
            taskToSave.setTitle(task.getTitle());
            taskToSave.setDescription(task.getDescription());
            taskToSave.setDue(task.getDue());
            taskToSave.setCompleted(task.isCompleted());
            taskToSave.setCollapsed(task.isCollapsed());
            taskToSave.setArchived(task.isArchived());
            taskToSave.setProjectOrder(task.getProjectOrder());
            taskToSave.setDailyViewOrder(task.getDailyOrder());
            taskToSave.setPriority(task.getPriority());
            taskToSave.setOwner(userRepository.getById(userId));
            //TODO labels
            taskToSave.setProject(projectRepository.getById(projectId));
            if(task.getId() != null) {
                hashMap.put(task.getId(), taskToSave);
            }
            parentMap.put(taskToSave, task.getParentId());
            toSave.add(taskToSave);
        }
        for(Task task : toSave) {
            Task parent = null;
            if(parentMap.containsKey(task)) {
                Integer parentId = parentMap.get(task);
                if(hashMap.containsKey(parentId)) {
                    parent = hashMap.get(parentId);
                } else if(ids.contains(parentId) || parentIds.contains(parentId)) {
                    parent = taskRepository.getById(parentId);
                }
            }
            task.setParent(parent);
        }

        taskRepository.saveAll(toSave);
    }

    @Override
    public void importTasks(Integer userId, String csv) {
        List<TaskImport> tasks = CSVtoTaskList(csv);
        List<Integer> ids = tasks.stream()
                .map(TaskImport::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        List<Integer> parentIds = tasks.stream()
                .map(TaskImport::getParentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        List<Integer> projectIds = tasks.stream()
                .map(TaskImport::getProjectId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        projectIds = projectRepository.findIdByOwnerIdAndIdIn(userId, projectIds);
        parentIds = taskRepository.findIdByOwnerIdAndIdIn(userId, parentIds);
        List<Task> tasksInDb = taskRepository.findByOwnerIdAndIdIn(userId, ids);
        ids = tasksInDb.stream()
                .map(Task::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        List<Task> toSave = new ArrayList<>();
        Map<Integer, Task> hashMap = new HashMap<>();
        Map<Task, Integer> parentMap = new HashMap<>();
        for(TaskImport task : tasks) {
            Task taskToSave = tasksInDb.stream()
                    .filter((a) -> Objects.equals(a.getId(), task.getId()))
                    .findAny()
                    .orElse(new Task());
            taskToSave.setTitle(task.getTitle());
            taskToSave.setDescription(task.getDescription());
            taskToSave.setDue(task.getDue());
            taskToSave.setCompleted(task.isCompleted());
            taskToSave.setCollapsed(task.isCollapsed());
            taskToSave.setArchived(task.isArchived());
            taskToSave.setProjectOrder(task.getProjectOrder());
            taskToSave.setDailyViewOrder(task.getDailyOrder());
            taskToSave.setPriority(task.getPriority());
            taskToSave.setOwner(userRepository.getById(userId));
            //TODO labels
            if(projectIds.contains(task.getProjectId())) {
                taskToSave.setProject(projectRepository.getById(task.getProjectId())); //TODO
            }
            if(task.getId() != null) {
                hashMap.put(task.getId(), taskToSave);
            }
            parentMap.put(taskToSave, task.getParentId());
            toSave.add(taskToSave);
        }
        for(Task task : toSave) {
            Task parent = null;
            if(parentMap.containsKey(task)) {
                Integer parentId = parentMap.get(task);
                if(hashMap.containsKey(parentId)) {
                    parent = hashMap.get(parentId);
                } else if(ids.contains(parentId) || parentIds.contains(parentId)) {
                    parent = taskRepository.getById(parentId);
                }
            }
            task.setParent(parent);
        }

        taskRepository.saveAll(toSave);
    }

    private List<ProjectImport> CSVtoProjectList(String list) {
        String[] charArr = list.split("");
        int i = 0;
        while(!charArr[i].equals("\n")) {
            i++;
        }
        List<ProjectImport> result = new ArrayList<>();
        i++;
        while(i<charArr.length) {
            int quoteCount = 0;
            int start = i;
            int fieldNum = 0;
            ProjectImport newProj = new ProjectImport();
            while (!(charArr[i].equals("\n") && quoteCount % 2 == 0)) {
                if (quoteCount % 2 == 0 && charArr[i].equals(DELIMITER)) {
                    String field = list.substring(start, i);
                    start = i + 1;
                    setField(fieldNum, newProj, field);
                    fieldNum++;
                } else if (charArr[i].equals("\"")) {
                    quoteCount++;
                }
                i++;
            }
            result.add(newProj);
            i++;
        }
        return result;
    }

    private void setField(int fieldNum, ProjectImport newProj, String field) {
        if(fieldNum == 0) {
            newProj.setId((toInteger(field)));
        } else if(fieldNum == 1) {
            newProj.setName(field);
        } else if(fieldNum == 2) {
            newProj.setColor(field);
        } else if(fieldNum == 3) {
            newProj.setFavorite(field.equals("true"));
        } else if(fieldNum == 4) {
            newProj.setArchived(field.equals("true"));
        } else if(fieldNum == 5) {
            newProj.setParentId(toInteger(field));
        } else if(fieldNum == 6) {
            newProj.setGeneralOrder(toInteger(field));
        }
    }

    private Integer toInteger(String s) {
        Matcher matcher = isInteger.matcher(s);
        if(matcher.matches()) {
            return Integer.valueOf(s);
        } else {
            return null;
        }
    }

    private List<TaskImport> CSVtoTaskList(String list) {
        String[] charArr = list.split("");
        int i = 0;
        while(!charArr[i].equals("\n")) {
            i++;
        }
        List<TaskImport> result = new ArrayList<>();
        i++;
        while(i<charArr.length) {
            int quoteCount = 0;
            int start = i;
            int fieldNum = 0;
            TaskImport newTask = new TaskImport();
            while (!(charArr[i].equals("\n") && quoteCount % 2 == 0)) {
                if (quoteCount % 2 == 0 && charArr[i].equals(DELIMITER)) {
                    String field = list.substring(start, i);
                    start = i + 1;
                    setField(fieldNum, newTask, field);
                    fieldNum++;
                } else if (charArr[i].equals("\"")) {
                    quoteCount++;
                }
                i++;
            }
            result.add(newTask);
            i++;
        }
        return result;
    }

    private void setField(int fieldNum, TaskImport newTask, String field) {
        if(fieldNum == 0) {
            newTask.setId((toInteger(field)));
        } else if(fieldNum == 1) {
            newTask.setTitle(field);
        } else if(fieldNum == 2) {
            newTask.setDescription(field);
        } else if(fieldNum == 3) {
            newTask.setParentId(toInteger(field));
        } else if(fieldNum == 4) {
            newTask.setDue(toDate(field));
        } else if(fieldNum == 5) {
            newTask.setCompleted(field.equals("true"));
        } else if(fieldNum == 6) {
            newTask.setCollapsed(field.equals("true"));
        } else if(fieldNum == 7) {
            newTask.setArchived(field.equals("true"));
        } else if(fieldNum == 8) {
            newTask.setProjectOrder(toInteger(field));
        } else if(fieldNum == 9) {
            newTask.setDailyOrder(toInteger(field));
        } else if(fieldNum == 10) {
            newTask.setPriority(toInteger(field));
        } else if(fieldNum == 11) {
            newTask.setLabels(new HashSet<>());
        } else if(fieldNum == 12) {
            newTask.setProjectId(toInteger(field));
        } else if(fieldNum == 13) {
            newTask.setProjectName(field);
        }
    }

    private LocalDateTime toDate(String s) {
        try {
            return LocalDateTime.parse(s);
        } catch(DateTimeParseException ex) {
            return null;
        }

    }
}
