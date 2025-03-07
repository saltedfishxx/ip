package helpers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import exceptions.InvalidTaskDescriptionException;
import tasks.Deadline;
import tasks.Events;
import tasks.Task;
import tasks.Todo;


/**
 * Represents Parser class that deals with processing user commands
 */
public class Parser {

    private static final DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    protected final String cmd;
    protected final TaskList taskList;
    protected String input;
    protected Storage storage;
    protected Message message;

    /**
     * Constructor for Helpers.Commands to initalise the relevant parameters
     *
     * @param cmd      The command entered
     * @param taskList The list of tasks
     * @param storage  The storage class to perform read/write
     */
    public Parser(String cmd, TaskList taskList, Storage storage) {
        this.cmd = cmd;
        this.taskList = taskList;
        this.message = new Message();
        this.storage = storage;
    }

    /**
     * Creates a task relating to ToDo.
     *
     * @param input String of user input
     * @return the String of task that was created
     */
    public String createToDo(String input) {
        String taskDesc = input.split(" ", 2)[1];
        if (input.split(" ").length < 2) {
            return new InvalidTaskDescriptionException("To-Do task").toString();
        } else {
            Todo td = new Todo(taskDesc, false);
            this.taskList.addTask(td);
            this.storage.write(this.taskList.getTaskList());
            return message.showAddTaskMessage(this.taskList, td);
        }
    }

    /**
     * Creates a task relating to Deadline.
     *
     * @param input String of user input
     * @return the String of task that was created
     */
    public String createDeadline(String input) {
        String[] parts = input.split(" /by ", 2);
        if (parts.length == 1 || parts[1].isEmpty() || parts[1].isBlank()) {
            return message.showArgumentErrorMessage(input.substring(9), "/by");
        } else {
            try {
                String taskDesc = parts[0].split(" ", 2)[1];
                LocalDateTime by = LocalDateTime.parse(parts[1], dateTimeFormat);
                Deadline dl = new Deadline(taskDesc, false, by);
                this.taskList.addTask(dl);
                this.storage.write(this.taskList.getTaskList());
                return message.showAddTaskMessage(this.taskList, dl);
            } catch (Exception e) {
                return message.showInvalidTimeFormatErrorMessage(parts[1]);
            }
        }
    }

    /**
     * Creates a task relating to Events.
     *
     * @param input String of user input
     * @return the String of task that was created
     */
    public String createEvent(String input) {
        String[] parts = input.split("\\s+/from\\s+|\\s+/to\\s+");
        if (parts.length < 3) {
            return message.showArgumentErrorMessage(input.substring(6), "/from and /to");
        } else {
            try {
                String taskDesc = parts[0].split(" ", 2)[1];
                LocalDateTime afterFrom = LocalDateTime.parse(parts[1], dateTimeFormat);
                LocalDateTime afterTo = LocalDateTime.parse(parts[2], dateTimeFormat);
                Events ev = new Events(taskDesc, false, afterFrom, afterTo);
                this.taskList.addTask(ev);
                this.storage.write(this.taskList.getTaskList());
                return message.showAddTaskMessage(this.taskList, ev);

            } catch (Exception e) {
                return message.showInvalidTimeFormatErrorMessage(parts[1] + " " + parts[2]);
            }
        }
    }

    /**
     * Marks a task as uncompleted.
     *
     * @param input String of user input
     * @return the String of task that was marked as undone
     */
    public String unmark(String input) {
        try {
            int index = Integer.parseInt(input.split(" ", 2)[1]);
            if (index < 0 || this.taskList.getListLength() == 0) {
                return message.showEmptyTasksError(input);
            } else {
                try {
                    Task task = this.taskList.markTaskAsUnDone(index - 1);
                    this.storage.write(this.taskList.getTaskList());
                    return message.showUnmarkDoneMessage(task);

                } catch (IndexOutOfBoundsException e) {
                    return message.showInvalidIndexErrorMessage(input);
                }

            }
        } catch (Exception e) {
            return message.showInvalidCommandErrorMessage(e.getMessage());

        }
    }

    /**
     * Marks a task as completed.
     *
     * @param input String of user input
     * @return the String of task that was marked as done
     */
    public String mark(String input) {
        try {
            int index = Integer.parseInt(input.split(" ", 2)[1]);
            if (index < 0 || this.taskList.getListLength() == 0) {
                return message.showEmptyTasksError(input);

            } else {
                try {
                    Task task = this.taskList.markTaskAsDone(index - 1);
                    this.storage.write(this.taskList.getTaskList());
                    return message.showMarkDoneMessage(task);

                } catch (IndexOutOfBoundsException e) {
                    return message.showInvalidIndexErrorMessage(input);
                }
            }
        } catch (Exception e) {
            return message.showInvalidCommandErrorMessage(e.getMessage());
        }
    }

    /**
     * Deletes a task from the task list.
     *
     * @param input String of user input
     * @return the String representation of the task that was deleted
     */
    public String deleteTask(String input) {
        try {
            int index = Integer.parseInt(input.split(" ", 2)[1]) - 1;
            if (index < 0 || this.taskList.getListLength() == 0) {
                return message.showEmptyTasksError(input);
            } else {
                try {
                    Task task = this.taskList.removeTask(index);
                    this.storage.write(this.taskList.getTaskList());
                    return message.showDeletedTaskMessage(this.taskList, task);

                } catch (IndexOutOfBoundsException e) {
                    return message.showInvalidIndexErrorMessage(input);
                }
            }
        } catch (IndexOutOfBoundsException e) {
            return message.showInvalidIndexErrorMessage(input);
        }
    }

    /**
     * Method to filter tasks based on user input
     * @param input Regex input by user
     * @return String of filtered tasks list
     */
    public String findTasks(String input) {
        String regex = input.split(" ", 2)[1];
        if (input.split(" ").length < 2) {
            return message.showInvalidArgumentErrorMessage("find", regex);
        } else {
            return message.showFilteredTaskList(this.taskList, regex);
        }
    }

    /**
     * Method to sort tasks based on ascending or descending
     * @param input User input
     * @return String of tasks sorted
     */
    public String showSortedTask(String input) {
        try {
            String param = input.split(" ", 2)[1];
            if (param == null || param.isEmpty()) {
                return message.showInvalidArgumentErrorMessage("sort", input);
            } else if (param.equalsIgnoreCase("asc")) {
                return message.showSortedTask(this.taskList, true);
            } else if (param.equalsIgnoreCase("desc")) {
                return message.showSortedTask(this.taskList, false);
            } else {
                return message.showInvalidArgumentErrorMessage("sort", input);
            }
        } catch (IndexOutOfBoundsException e) {
            return message.showInvalidIndexErrorMessage(input);
        }
    }



    /**
     * Execute the controllers.Duke's functions to process user's input.
     * @param input User's text input
     * @return MachoDuke's response in String
     */
    public String execute(String input) {
        this.input = input;
        CommandEnum command = null;
        try {
            try {
                command = CommandEnum.valueOf(this.cmd.toUpperCase());
            } catch (Exception e) {
                return message.showInvalidCommandErrorMessage(this.cmd);
            }

            switch (command) {
            case LIST:
                return message.showTaskList(this.taskList);

            case SORT:
                return showSortedTask(this.input);

            case TODO:
                return createToDo(this.input);

            case DEADLINE:
                return createDeadline(this.input);

            case EVENT:
                return createEvent(this.input);

            case UNMARK:
                return unmark(this.input);

            case MARK:
                return mark(this.input);

            case DELETE:
                return deleteTask(this.input);

            case FIND:
                return findTasks(this.input);

            case BYE:
                return "\nBye! Hope to see you again soon, macho!";

            default:
                return "Invalid command macho! Please try again!";
            }

        } catch (NullPointerException e) {
            System.out.println(e.getMessage());
        }
        return "Macho there is an error processing your message, please try again!";
    }


}
