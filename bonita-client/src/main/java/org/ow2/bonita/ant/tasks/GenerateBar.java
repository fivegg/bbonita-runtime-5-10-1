/**
 * Copyright (C) 2010  BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA  02110-1301, USA.
 **/
package org.ow2.bonita.ant.tasks;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.ow2.bonita.facade.def.element.BusinessArchive;
import org.ow2.bonita.util.BusinessArchiveFactory;
import org.ow2.bonita.util.ExceptionManager;
import org.ow2.bonita.util.Misc;

public class GenerateBar extends Task {

  private static final Logger LOG = Logger.getLogger(GenerateBar.class.getName());
  
  protected File destfile = null;
  protected Collection<FileSet> fileSets = new ArrayList<FileSet>();

  public void setDestfile(File destfile) {
    this.destfile = destfile;
  }

  public void addFileset(FileSet fileSet) {
    fileSets.add(fileSet);
  }

  @Override
  public void execute() {
    if (destfile == null) {
    	String message = ExceptionManager.getInstance().getFullMessage(
    			"bat_GB_1", this.getClass().getName());
      throw new BuildException(message);
    }
    try {
      if (!destfile.exists()) {
        destfile.createNewFile();
      }
      Map<String, byte[]> resources = new HashMap<String, byte[]>();
      
      for (FileSet fileSet : fileSets) {
        DirectoryScanner directoryScanner = fileSet.getDirectoryScanner(getProject()); 
        String[] files = directoryScanner.getIncludedFiles();
        for (String file : files) {
          File f = new File(directoryScanner.getBasedir().getAbsolutePath() + File.separatorChar + file);
          if (!f.exists()) {
            LOG.severe("File : " + f + " does not exist. Can't add it to the BAR.");
          } else {
            resources.put(file, Misc.getAllContentFrom(f));
          }
        }
      }
      if (resources.isEmpty()) {
      	String message = ExceptionManager.getInstance().getFullMessage(
      			"bat_GB_2", this.getClass().getName());
        throw new BuildException(message);
      }
      BusinessArchive businessArchive = BusinessArchiveFactory.getBusinessArchive(resources);
      BusinessArchiveFactory.generateBusinessArchiveFile(destfile, businessArchive);
    } catch (Exception e) {
      throw new BuildException(e);
    }
  }
}
