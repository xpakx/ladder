package io.github.xpakx.ladder.service;

import io.github.xpakx.ladder.entity.Project;
import io.github.xpakx.ladder.entity.dto.ProjectImport;
import io.github.xpakx.ladder.entity.dto.TaskImport;
import io.github.xpakx.ladder.repository.ProjectRepository;
import io.github.xpakx.ladder.repository.TaskRepository;
import io.github.xpakx.ladder.repository.UserAccountRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
        List<Integer> ids = projects.stream()
                .map(ProjectImport::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        List<Integer> parentIds = projects.stream()
                .map(ProjectImport::getParentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        parentIds = projectRepository.findIdByOwnerIdAndIdIn(userId, parentIds);
        List<Project> projectsInDb = projectRepository.findByOwnerIdAndIdIn(userId, ids);
        ids = projectsInDb.stream()
                .map(Project::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        List<Project> toSave = new ArrayList<>();
        Map<Integer, Project> hashMap = new HashMap<>();
        Map<Project, Integer> parentMap = new HashMap<>();
        for(ProjectImport project : projects) {
            Project projectToSave = projectsInDb.stream()
                    .filter((a) -> Objects.equals(a.getId(), project.getId()))
                    .findAny()
                    .orElse(new Project());
            projectToSave.setName(project.getName());
            projectToSave.setColor(project.getColor());
            projectToSave.setFavorite(project.isFavorite());
            projectToSave.setArchived(project.isArchived());
            projectToSave.setGeneralOrder(project.getGeneralOrder());
            projectToSave.setOwner(userRepository.getById(userId));
            if(project.getId() != null) {
                hashMap.put(project.getId(), projectToSave);
            }
            parentMap.put(projectToSave, project.getParentId());
            toSave.add(projectToSave);
        }
        for(Project project : toSave) {
            Project parent = null;
            if(parentMap.containsKey(project)) {
                Integer parentId = parentMap.get(project);
                if(hashMap.containsKey(parentId)) {
                    parent = hashMap.get(parentId);
                } else if(ids.contains(parentId)) {
                    parent = projectRepository.getById(parentId);
                }
            }
            project.setParent(parent);
        }

        projectRepository.saveAll(toSave);
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
            newTask.setDue(null);
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
}
