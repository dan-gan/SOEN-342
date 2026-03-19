
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CSVConnector{

    public static boolean exportTasks(List<Task> tasks, String filePath){
        try{
            File f = new File(filePath+"/TaskCSV.csv");
            FileWriter fWriter = new FileWriter(f);
            if(!f.exists()){
                f.createNewFile();
            }
            BufferedWriter writer = new BufferedWriter(fWriter);
            for(Task task: tasks){
                String entry = task.getId() + ",&ID," + task.getTitle()+ ",&TITLE,"+task.getDescription()+",&DESCRIPTION,"+task.getCreationDate()+",&CREATIONDATE,"
                +task.getDueDate()+",&DUEDATE,"+task.getPriorityLevel()+",&PRIORITYLEVEL,"+task.getTaskStatus()
                +",&STATUS,";
                for(Subtask subtask: task.getSubtasks()){
                    entry = entry + subtask.getTitle() + ",&SUBTITLE,"+subtask.getId() + ",&SUBID,"+ subtask.getStatus() + ",&SUBSTATUS,";
                }
                for(Tag tag: task.getTags()){
                    entry = entry + tag.getName() + ",&TAGNAME,"+tag.getId()+",&TAGID,";
                }
                for(ActivityEntry activityEntry: task.getActivityEntry()){
                    entry = entry + activityEntry.getDescription()+ ",&ACTDESCRIPTION,"+activityEntry.getTimestamp()+",&ACTTIMESTAMP,";
                }
                entry+="&TASK,";
                writer.write(entry);
            }
            writer.close();
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            return false;
        }
        return false;
    }

    public static ArrayList<Task> importTasks(String filePath){
        ArrayList<Task> tasks = new ArrayList<>();
        File file = new File(filePath+"/TaskCSV.csv");
        String line;
        if(!file.exists()){
            return tasks;
        }
        try{
            BufferedReader reader = new BufferedReader(new FileReader(file));

            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(",");

                Task task = new Task();

                List<Subtask> subtasks = new ArrayList<>();
                List<Tag> tags = new ArrayList<>();
                List<ActivityEntry> activities = new ArrayList<>();

                Subtask currentSubtask = null;
                Tag currentTag = null;
                ActivityEntry currentActivity = null;

                for (int i = 0; i < tokens.length; i++) {

                    if(tokens[i].equalsIgnoreCase("&TASK")){
                        task.setSubtasks(subtasks);
                        task.setTags(tags);
                        task.setActivityEntry(activities);
                        tasks.add(task);

                        subtasks.clear();
                        tags.clear();
                        activities.clear();
                        task = new Task();
                        continue;
                    }

                    String value = tokens[i];
                    String marker = tokens[i + 1];

                    switch (marker) {
                        case "&ID":
                            task.setId(Integer.parseInt(value));
                            break;
                        case "&TITLE":
                            task.setTitle(value);
                            break;
                        case "&DESCRIPTION":
                            task.setDescription(value);
                            break;
                        case "&CREATIONDATE":
                            task.setCreationDate(new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy").parse(value));
                            break;
                        case "&DUEDATE":
                            task.setDueDate(new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy").parse(value));
                            break;
                        case "&PRIORITYLEVEL":
                            task.setPriorityLevel(PriorityLevel.valueOf(value));
                            break;
                        case "&STATUS":
                            task.setTaskStatus(TaskStatus.valueOf(value));
                            break;

                        // --- Subtask ---
                        case "&SUBTITLE":
                            currentSubtask = new Subtask();
                            currentSubtask.setTitle(value);
                            break;
                        case "&SUBID":
                            if (currentSubtask != null) {
                                currentSubtask.setId(Integer.parseInt(value));
                            }
                            break;
                        case "&SUBSTATUS":
                            if (currentSubtask != null) {
                                currentSubtask.setStatus(TaskStatus.valueOf(value));
                                subtasks.add(currentSubtask);
                                currentSubtask = null;
                            }
                            break;

                        case "&TAGNAME":
                            currentTag = new Tag();
                            currentTag.setName(value);
                            break;
                        case "&TAGID":
                            if (currentTag != null) {
                                currentTag.setId(Integer.parseInt(value));
                                tags.add(currentTag);
                                currentTag = null;
                            }
                            break;

                        case "&ACTDESCRIPTION":
                            currentActivity = new ActivityEntry();
                            currentActivity.setDescription(value);
                            break;
                        case "&ACTTIMESTAMP":
                            if (currentActivity != null) {
                                currentActivity.setTimestamp(new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy").parse(value));
                                activities.add(currentActivity);
                                currentActivity = null;
                            }
                            break;
                        default:
                            break;
                    }
                }
            }

            reader.close();
        }
        catch (IOException e){
            System.out.println("I/O Exception");
        } catch (ParseException e) {
            System.out.println("Couldn't parse a date");
        }

        return tasks;
    }

}