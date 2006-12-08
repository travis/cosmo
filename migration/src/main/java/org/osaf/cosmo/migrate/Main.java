package org.osaf.cosmo.migrate;
/*
 * Copyright 2005-2006 Open Source Applications Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


/**
 * Main class for invoking the MigrationManager.
 *
 */
public class Main {

    private static final Class[] parameters = new Class[]{URL.class};
    
    public static void addFile(String s) throws IOException {
        File f = new File(s);
        addURL(f.toURL());
    }
 
    public static void addURL(URL u) throws IOException {
 
        URLClassLoader sysloader = (URLClassLoader)ClassLoader.getSystemClassLoader();
        Class sysclass = URLClassLoader.class;
        
        try {
            Method method = sysclass.getDeclaredMethod("addURL",parameters);
            method.setAccessible(true);
            method.invoke(sysloader,new Object[]{ u });
        } catch (Throwable t) {
            t.printStackTrace();
            throw new IOException("Error, could not add URL to system classloader");
        }
    }
    
    public static void main(String[] args) {
        try {
            
            // check correct number of args
            if(args.length>2 || args.length==0)
                outputUsage();
            
            // verify correct args
            if ((args.length == 2 && !"-v".equals(args[0]))
                    || (args.length == 1 && "-v".equals(args[0])))
                outputUsage();
            
            String resourceDir = null;
            
            // get args
            if(args.length==2) {
                resourceDir = args[1];
                // set verbose mode
                Logger.getLogger("org.osaf.cosmo.migrate").setLevel(Level.DEBUG);
            }
            else {
                resourceDir = args[0];
            }
            
            // add directory containing migration.properties to classpath
            addFile(resourceDir);
            
            // verify we can load migration.properties
            if (ClassLoader.getSystemClassLoader().getResource(
                    "migration.properties") == null) {
                System.out.println("unable to load migration.properties!");
                outputUsage();
            }
            
        } catch (IOException e1) {
            e1.printStackTrace();
            System.exit(1);
        }
        
        // Run the MigrationManager
        ApplicationContext context = new ClassPathXmlApplicationContext(
                new String[] {"applicationContext-migrate.xml"});
        
        MigrationManager migrationManager = 
            (MigrationManager) context.getBean("migrationManager");
        try {
            migrationManager.migrate();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
    
    private static void outputUsage() {
        System.out.println("usage: java -jar cosmo-migration.jar [-v] [dir containing migration.propeties]");
        System.exit(1);
    }
    

}
