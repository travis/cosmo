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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;


/**
 * Main class for invoking the MigrationManager.
 *
 */
public class Main {

    public static void main(String[] args) {
       
        // check correct number of args
        if (args.length > 2 || args.length == 0)
            outputUsage();

        // verify correct args
        if ((args.length == 2 && !"-v".equals(args[0]))
                || (args.length == 1 && "-v".equals(args[0])))
            outputUsage();

        File resourceFile = null;

        // get args
        if (args.length == 2) {
            resourceFile = new File(args[1]);
            // set verbose mode
            Logger.getLogger("org.osaf.cosmo.migrate").setLevel(Level.DEBUG);
        } else {
            resourceFile = new File(args[0]);
        }

        // verify properties file exists
        if (!resourceFile.exists()) {
            System.out.println("unable to load migration.properties!");
            outputUsage();
        }
        
        // Load properties
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(resourceFile));
        } catch (FileNotFoundException e1) {
            System.out.println("unable to load migration.properties!");
            outputUsage();
        } catch (IOException e1) {
            System.out.println("error loading migration.properties!");
            outputUsage();
        }
        
        // Set system properties for use by PropertyPlaceholderConfigurer
        Iterator keys = props.keySet().iterator();
        while(keys.hasNext()) {
            String key = (String) keys.next();
            String val = props.getProperty(key);
            System.setProperty(key, val);
        }
        
        // Run the MigrationManager
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
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
        System.out.println("usage: java -jar cosmo-migration.jar [-v] [migration properties file]");
        System.exit(1);
    }
    

}
