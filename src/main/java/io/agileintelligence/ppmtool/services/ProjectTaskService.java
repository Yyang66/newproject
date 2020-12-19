package io.agileintelligence.ppmtool.services;

import io.agileintelligence.ppmtool.domain.Backlog;
import io.agileintelligence.ppmtool.domain.Project;
import io.agileintelligence.ppmtool.domain.ProjectTask;
import io.agileintelligence.ppmtool.exceptions.ProjectNotFoundException;
import io.agileintelligence.ppmtool.repositories.BacklogRepository;
import io.agileintelligence.ppmtool.repositories.ProjectRepository;
import io.agileintelligence.ppmtool.repositories.ProjectTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectTaskService{
    @Autowired
    private BacklogRepository backlogRepository;

    @Autowired
    private ProjectTaskRepository projectTaskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectServices projectServices;

    public ProjectTask addProjectTask(String projectIdentifier, ProjectTask projectTask,String username){
        //exceptions: project not found

            //PTs to be added to a specific project, !=null,backlog exists
            Backlog backlog = projectServices.findProjectByIdentifier(projectIdentifier,username).getBacklog();//backlogRepository.findByProjectIdentifier(projectIdentifier);
            //set the backlog to pt
            projectTask.setBacklog(backlog);
            //project sequence to be like :IDPRO-1, IDPRO-2
            Integer BacklogSequence = backlog.getPTSequence();
            //update BL sequence
            BacklogSequence++;
            backlog.setPTSequence(BacklogSequence);
            //Add to Task
            projectTask.setProjectSequence(projectIdentifier+"-"+BacklogSequence);
            projectTask.setProjectIdentifier(projectIdentifier);
            //initial priority when priority null
            //initial status when status null
            if(projectTask.getStatus()==""|| projectTask.getStatus()==null){
                projectTask.setStatus("TO_DO");
            }
            //fix bug with priority in spring boot server, need to check null first
            if(projectTask.getPriority()==null||projectTask.getPriority()==0){
                //in the future wee need projectTask.getPriority()==0 to handle form
                projectTask.setPriority(3);
            }
            return projectTaskRepository.save(projectTask);
        }

    public Iterable<ProjectTask>findBacklogById(String id, String username) {
        projectServices.findProjectByIdentifier(id,username);
        return projectTaskRepository.findByProjectIdentifierOrderByPriority(id);
    }

    public ProjectTask findPTByProjectSequence(String backlog_id, String pt_id,String username){
        //make sure we are searching on an existing backlog
        projectServices.findProjectByIdentifier(backlog_id,username);

        //make sure task exist
        ProjectTask projectTask = projectTaskRepository.findByProjectSequence(pt_id);
        if(projectTask==null){
            throw new ProjectNotFoundException("Project Task '"+pt_id+"' not found");
        }

        //make sure backlog/pt id return to right project
        if(!projectTask.getProjectIdentifier().equals(backlog_id)){
            throw new ProjectNotFoundException("Project Task '"+pt_id+"' " +
                    "does not exist in project: '"+backlog_id);
        }
        return projectTask;
    }

    public ProjectTask updateByProjectSequence(ProjectTask updatedTask, String backlog_id,
                                               String pt_id,String username){
        ProjectTask projectTask = findPTByProjectSequence(backlog_id, pt_id,username);
        projectTask = updatedTask;
        return projectTaskRepository.save(projectTask);
    }

    public void deletePTByProjectSequence(String backlog_id, String pt_id,String username){
        ProjectTask projectTask = findPTByProjectSequence(backlog_id, pt_id,username);
        projectTaskRepository.delete(projectTask);
    }
}
