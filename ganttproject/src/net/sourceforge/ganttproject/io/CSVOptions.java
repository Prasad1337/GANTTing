/*
Copyright 2003-2012 GanttProject Team

This file is part of GanttProject, an opensource project management tool.

GanttProject is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

GanttProject is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with GanttProject.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.sourceforge.ganttproject.io;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import biz.ganttproject.core.model.task.TaskDefaultColumn;
import biz.ganttproject.core.option.BooleanOption;
import biz.ganttproject.core.option.DefaultBooleanOption;

public class CSVOptions {
//  public boolean bExportTaskID = true;
//
//  public boolean bExportTaskName = true;
//
//  public boolean bExportTaskStartDate = true;
//
//  public boolean bExportTaskEndDate = true;
//
//  public boolean bExportTaskPercent = true;
//
//  public boolean bExportTaskDuration = true;
//
//  public boolean bExportTaskWebLink = true;
//
//  public boolean bExportTaskResources = true;
//
//  public boolean bExportTaskNotes = true;

  private static final Set<TaskDefaultColumn> ourIgnoredTaskColumns = ImmutableSet.of(
      TaskDefaultColumn.TYPE, TaskDefaultColumn.PRIORITY, TaskDefaultColumn.INFO);
  private final Map<String, BooleanOption> myTaskOptions = Maps.newLinkedHashMap();

  public CSVOptions() {
    List<TaskDefaultColumn> orderedColumns = ImmutableList.of(
        TaskDefaultColumn.ID, TaskDefaultColumn.NAME, TaskDefaultColumn.BEGIN_DATE, TaskDefaultColumn.END_DATE, TaskDefaultColumn.DURATION, TaskDefaultColumn.COMPLETION);
    LinkedHashSet<TaskDefaultColumn> columns = Sets.newLinkedHashSet(Arrays.asList(TaskDefaultColumn.values()));
    columns.removeAll(orderedColumns);
    for (TaskDefaultColumn taskColumn : orderedColumns) {
      createTaskExportOption(taskColumn);
    }
    for (TaskDefaultColumn taskColumn : columns) {
      if (!ourIgnoredTaskColumns.contains(taskColumn)) {
        createTaskExportOption(taskColumn);
      }
    }
    createTaskExportOption("webLink");
    createTaskExportOption("resources");
    createTaskExportOption("notes");
  }

  public BooleanOption createTaskExportOption(TaskDefaultColumn taskColumn) {
    DefaultBooleanOption result = new DefaultBooleanOption(taskColumn.getStub().getID(), true);
    myTaskOptions.put(taskColumn.getStub().getID(), result);
    return result;
  }

  public BooleanOption createTaskExportOption(String id) {
    DefaultBooleanOption result = new DefaultBooleanOption(id, true);
    myTaskOptions.put(id, result);
    return result;
  }

  public Map<String, BooleanOption> getTaskOptions() {
    return myTaskOptions;
  }

  public boolean bExportResourceID = true;

  public boolean bExportResourceName = true;

  public boolean bExportResourceMail = true;

  public boolean bExportResourcePhone = true;

  public boolean bExportResourceRole = true;

  public boolean bFixedSize = false;

  public String sSeparatedChar = ",";

  public String sSeparatedTextChar = "\"";

  /** @return a list of the possible separated char. */
  public String[] getSeparatedTextChars() {
    String[] charText = { "   \'   ", "   \"   " };
    return charText;
  }
}
