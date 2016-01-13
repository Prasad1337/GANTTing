package net.sourceforge.ganttproject.test.task;

import junit.framework.TestCase;
import net.sourceforge.ganttproject.TestSetupHelper;
import net.sourceforge.ganttproject.task.TaskManager;
import net.sourceforge.ganttproject.task.Task;
import net.sourceforge.ganttproject.task.ResourceAssignment;
import net.sourceforge.ganttproject.task.TaskManagerConfig;
import net.sourceforge.ganttproject.task.dependency.TaskDependency;
import net.sourceforge.ganttproject.task.dependency.TaskDependencyException;
import net.sourceforge.ganttproject.TestSetupHelper.TaskManagerBuilder;
import net.sourceforge.ganttproject.gui.NotificationManager;
import net.sourceforge.ganttproject.resource.HumanResourceManager;
import net.sourceforge.ganttproject.resource.HumanResource;
import net.sourceforge.ganttproject.roles.RoleManager;

import java.awt.Color;
import java.net.URL;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

import biz.ganttproject.core.calendar.AlwaysWorkingTimeCalendarImpl;
import biz.ganttproject.core.calendar.GPCalendar;
import biz.ganttproject.core.time.TimeUnitStack;
import biz.ganttproject.core.time.impl.GPTimeUnitStack;

public class TestAutoLevel extends TestCase {
    private TaskManager myTaskManager;

    private HumanResourceManager myHumanResourceManager;

    public TestAutoLevel(String s) {
        super(s);
    }

    public void testAutoLevelShiftsTasks () {
    	/* Test Scenario : with two resources, autolevel should put tasks like so
    	 * test that start dates of tasks get shifted
    	 * t0
    	 * t1
    	 *		t2
    	 *		t3
    	 *			t4
    	 *			t5
    	 *				t6
    	 *				t7
    	 * */
        TaskManagerBuilder builder = TestSetupHelper.newTaskManagerBuilder();
        TaskManager taskManager = builder.build();
        HumanResourceManager resourceManager = builder.getResourceManager();

        //create some 8 tasks
        for(int i = 0; i <8; i++)
        	taskManager.createTask();
        //create some resources
        resourceManager.create("test resource#1", 0);
        resourceManager.create("test resource#2", 1);

        //all tasks should have the same start date
        long startDate = taskManager.getTask(0).getStart().getTimeInMillis();
        for(Task t: taskManager.getTasks())
        	assertEquals(startDate,t.getStart().getTimeInMillis());

        //run autolevel
        taskManager.processAutoLevel(taskManager.getRootTask());

		//tasks start dates should now be shifted
        long thisDate = startDate;
        int i = 0;
        for(Task t: taskManager.getTasks()){
        	assertEquals(thisDate,t.getStart().getTimeInMillis());
        	i++;
        	if(i%2 == 0)
        		thisDate = t.getEnd().getTimeInMillis();
        }

    }

    public void testAutoResourceAssignment() {
    	/*test that autolevel auto assigns one resources to each task
    	 *
    	 */
        TaskManagerBuilder builder = TestSetupHelper.newTaskManagerBuilder();
        TaskManager taskManager = builder.build();
        HumanResourceManager resourceManager = builder.getResourceManager();

        //create some tasks
        Task task1 = taskManager.createTask();
        Task task2 = taskManager.createTask();
        Task task3 = taskManager.createTask();
        Task task4 = taskManager.createTask();
        Task task5 = taskManager.createTask();
        Task task6 = taskManager.createTask();
        Task task7 = taskManager.createTask();

        createDependency(task3, task2);
        //create some resources
        resourceManager.create("test resource#1", 0);
        resourceManager.create("test resource#2", 1);
        //check resources are not assigned by default
        //loop through all tasks, check that they have 0 assigned resources
        for(Task t: taskManager.getTasks())
        	assertEquals(0,t.getAssignments().length);
        taskManager.processAutoLevel(taskManager.getRootTask());
        //check resource have now been assigned
        //assert all tasks now have one resource assigned
        for(Task t: taskManager.getTasks())
        	assertEquals(1,t.getAssignments().length);
    }

    public void testPrioritization()throws Exception{
    	/* Test Scenario :-
    	 * t0
    	 * t2 -> t1
    	 *
    	 * */
        TaskManager mgr = getTaskManager();
        Task t0 = mgr.createTask();
        Task t1 = mgr.createTask();
        Task t2 = mgr.createTask();
        createDependency(t1, t2);
        mgr.processCriticalPath(mgr.getRootTask());
        // get all tasks starting at the first day of the project
        ArrayList<Task> conf_tasks = mgr.getConflictingTasks(t0.getStart().getTimeInMillis() + 1);
        // before calling prioritize t0 is on the top of the list, since it was created before t1
        assertEquals(conf_tasks.get(0), t0);
        assertEquals(conf_tasks.get(1), t2);
        ArrayList<Task> prioritized = mgr.prioritize(conf_tasks);
        // after calling prioritize t0 is swapped with t2; t2 is more critical than t0, because it
        // belongs to the critical path
        assertEquals(prioritized.get(0), t2);
        assertEquals(prioritized.get(1), t0);
    }

    public void testOverloadResolver()throws Exception{
    	/* Test Scenario :-
    	 * r0: default
    	 *
    	 * [t0: default]
    	 * [t1: default]
    	 * [t2: default]
    	 * format:  [task name: task type]
    	 * */
        TaskManager taskManager = getTaskManager();
        Task t0 = taskManager.createTask();
        Task t1 = taskManager.createTask();
        Task t2 = taskManager.createTask();
        // create developer
        TaskManagerBuilder builder = TestSetupHelper.newTaskManagerBuilder();
        	///TaskManager taskManager = builder.build();
        HumanResourceManager resourceManager = builder.getResourceManager();
        resourceManager.create("test resource#0", 0);

        taskManager.processCriticalPath(taskManager.getRootTask());
        // get all tasks starting at the first day of the project TODO add loop
        ArrayList<Task> conf_tasks = taskManager.getConflictingTasks(t0.getStart().getTimeInMillis() + 1);
        // before calling prioritize t0 is on the top of the list, since it was created before t1
        assertEquals(conf_tasks.size(), 3);
        taskManager.resolveOverloadedResource(conf_tasks, 1);
        conf_tasks = taskManager.getConflictingTasks(t0.getStart().getTimeInMillis() + 1);
        assertEquals(conf_tasks.size(), 1);

    }


    private Set<HumanResource> extractResources(Task task) {
        Set<HumanResource> result = new HashSet<HumanResource>();
        ResourceAssignment[] assignments = task.getAssignments();
        for (int i = 0; i < assignments.length; i++) {
            ResourceAssignment next = assignments[i];
            result.add(next.getResource());
            assertEquals("Unexpected task is owning resource assignment="
                    + next, task, next.getTask());
        }
        return result;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        myHumanResourceManager = new HumanResourceManager(RoleManager.Access
                .getInstance().getDefaultRole(), null);
        getResourceManager().create("test resource#1", 1);
        getResourceManager().create("test resource#2", 2);
        myTaskManager = newTaskManager();
    }

    private TaskManager newTaskManager() {
        return TaskManager.Access.newInstance(null, new TaskManagerConfig() {

            @Override
            public Color getDefaultColor() {
                return null;
            }

            @Override
            public GPCalendar getCalendar() {
                return new AlwaysWorkingTimeCalendarImpl();
            }

            @Override
            public TimeUnitStack getTimeUnitStack() {
                return new GPTimeUnitStack();
            }

            @Override
            public HumanResourceManager getResourceManager() {
                return null;
            }

            @Override
            public URL getProjectDocumentURL() {
                return null;
            }

            @Override
            public NotificationManager getNotificationManager() {
              return null;
            }
        });
    }

    private TaskManager getTaskManager() {
        return myTaskManager;
    }

    private HumanResourceManager getResourceManager() {
        return myHumanResourceManager;
    }

    protected TaskDependency createDependency(Task dependant, Task dependee) throws TaskDependencyException {
        return getTaskManager().getDependencyCollection().createDependency(dependant, dependee);
    }
}