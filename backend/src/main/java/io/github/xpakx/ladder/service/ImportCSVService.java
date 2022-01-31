package io.github.xpakx.ladder.service;

import io.github.xpakx.ladder.entity.Project;
import io.github.xpakx.ladder.repository.ProjectRepository;
import io.github.xpakx.ladder.repository.TaskRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class ImportCSVService implements ImportServiceInterface {
    private ProjectRepository projectRepository;
    private TaskRepository taskRepository;
    private static final String DELIMITER = ",";

    @Override
    public void importProjectList(Integer userId, String csv) {
        List<Project> projects = CSVtoProjectList(csv);
    }

    @Override
    public void importTasksFromProjectById(Integer userId, Integer projectId, String csv) {

    }

    @Override
    public void importTasks(Integer userId, String csv) {

    }

    private List<Project> CSVtoProjectList(String list) {
        String[] charArr = list.split("");
        int i = 0;
        while(!charArr[i].equals("\n")) {
            i++;
        }
        List<Project> result = new ArrayList<>();
        i++;
        while(i<charArr.length) {
            int quoteCount = 0;
            int start = i;
            int fieldNum = 0;
            Project newProj = new Project();
            while (!(charArr[i].equals("\n") && quoteCount % 2 == 0)) {
                if (quoteCount % 2 == 0 && charArr[i].equals(",")) {
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

    private void setField(int fieldNum, Project newProj, String field) {
        if(fieldNum == 0) {
            newProj.setId(Integer.valueOf(field));
        } else if(fieldNum == 1) {
            newProj.setName(field);
        } else if(fieldNum == 2) {
            newProj.setColor(field);
        } else if(fieldNum == 3) {
            newProj.setFavorite(field.equals("true"));
        } else if(fieldNum == 4) {
            newProj.setArchived(field.equals("true"));
        } else if(fieldNum == 5) {
            newProj.setParent(projectRepository.getById(Integer.valueOf(field)));
        } else if(fieldNum == 6) {
            newProj.setGeneralOrder(Integer.valueOf(field));
        }
    }
}
