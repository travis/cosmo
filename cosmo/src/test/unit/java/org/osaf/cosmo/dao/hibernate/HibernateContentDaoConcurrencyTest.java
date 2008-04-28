/*
 * Copyright 2007 Open Source Applications Foundation
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
package org.osaf.cosmo.dao.hibernate;

import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import junit.framework.Assert;

import org.hibernate.SessionFactory;
import org.osaf.cosmo.dao.UserDao;
import org.osaf.cosmo.model.CollectionItem;
import org.osaf.cosmo.model.ContentItem;
import org.osaf.cosmo.model.FileItem;
import org.osaf.cosmo.model.Item;
import org.osaf.cosmo.model.NoteItem;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.model.hibernate.HibFileItem;
import org.osaf.cosmo.model.hibernate.HibItem;
import org.osaf.cosmo.model.hibernate.HibNoteItem;
import org.osaf.cosmo.model.hibernate.HibQName;
import org.osaf.cosmo.model.hibernate.HibStringAttribute;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * Test concurrent modification of an item.  Since cosmo uses
 * optimistic locking, a concurrent modification will fail
 * when the second thread updates the item.  It goes something
 * like this:
 * 
 * 1. Thread 1 reads item version 1
 * 2. Thread 2 reads item version 1
 * 3. Thread 1 updates item version 1 to item version 2
 * 4. Thread 2 tries to update item version 1, sees that item is
 *    no longer version 1, and throws exception
 *
 */
public class HibernateContentDaoConcurrencyTest extends AbstractHibernateDaoTestCase {

    protected UserDaoImpl userDao = null;
    protected ContentDaoImpl contentDao = null;
    protected DataSource jdbcDataSource = null;
    
    public HibernateContentDaoConcurrencyTest() {
        super();
    }
    
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();
    }

    public void testConcurrentContentDaoUpdateContent() throws Exception {
        
        TransactionThread txThread1 = new TransactionThread(transactionManager,sessionFactory);
        TransactionThread txThread2 = new TransactionThread(transactionManager,sessionFactory);
        TransactionThread txThread3 = new TransactionThread(transactionManager,sessionFactory);
        
        cleanupDb();
        
        // create item to be updated concurrently
        txThread1.addRunnable("1", new TxRunnable() {
            public Object run() {
                User user = getUser(userDao, "testuser");
                CollectionItem root = (CollectionItem) contentDao.getRootItem(user);

                ContentItem item = generateTestContent();
                item.setUid("test");

                ContentItem newItem = contentDao.createContent(root, item);
                return newItem;
            }
        });
        
        // read item by thread 2
        txThread2.addRunnable("1", new TxRunnable() {
            public Object run() {
                
                ContentItem item = (ContentItem) contentDao.findItemByUid("test");
                return item;
            }
        });
        
        // read item by thread 3
        txThread3.addRunnable("1", new TxRunnable() {
            public Object run() {
                
                ContentItem item = (ContentItem) contentDao.findItemByUid("test");
                return item;
            }
        });
        
        
        // create item
        txThread1.start();
        txThread1.commit();
        txThread1.join();
        
        // read item at the same time
        txThread2.start();
        txThread3.start();
        
        // wait till reads are done
        while(txThread2.getRunnableResults("1")==null)
            Thread.sleep(50);
        while(txThread3.getRunnableResults("1")==null)
            Thread.sleep(50);
        
        // results of the read (should be same item)
        final ContentItem item1 = (ContentItem) txThread2.getRunnableResults("1");
        final ContentItem item2 = (ContentItem) txThread3.getRunnableResults("1");
        
        // write item by thread 2
        txThread2.addRunnable("2", new TxRunnable() {
            public Object run() {
                
                contentDao.updateContent(item1);
                return item1;
            }
        });
        
        // wait for write to complete
        while(txThread2.getRunnableResults("2")==null)
            Thread.sleep(50);
        
        // thread 2 wins with the commit
        txThread2.commit();
        txThread2.join();
        
        // now try to write item by thread 3, should fail
        txThread3.addRunnable("2", new TxRunnable() {
            public Object run() {
                
                contentDao.updateContent(item2);
                return item2;
            }
        });
        
        txThread3.commit();
        txThread3.join();

        // results should be OptimisticLockingFailureException
        Assert.assertTrue(txThread3.getRunnableResults("2") instanceof OptimisticLockingFailureException);
       
        cleanupDb();
    }
    
    public void testConcurrentContentDaoDeleteContent() throws Exception {
        
        TransactionThread txThread1 = new TransactionThread(transactionManager,sessionFactory);
        TransactionThread txThread2 = new TransactionThread(transactionManager,sessionFactory);
        TransactionThread txThread3 = new TransactionThread(transactionManager,sessionFactory);
        
        cleanupDb();
        
        // create item to be updated concurrently
        txThread1.addRunnable("1", new TxRunnable() {
            public Object run() {
                User user = getUser(userDao, "testuser");
                CollectionItem root = (CollectionItem) contentDao.getRootItem(user);

                ContentItem item = generateTestContent();
                item.setUid("test");

                ContentItem newItem = contentDao.createContent(root, item);
                return newItem;
            }
        });
        
        // read item by thread 2
        txThread2.addRunnable("1", new TxRunnable() {
            public Object run() {
                
                ContentItem item = (ContentItem) contentDao.findItemByUid("test");
                return item;
            }
        });
        
        // read item by thread 3
        txThread3.addRunnable("1", new TxRunnable() {
            public Object run() {
                
                ContentItem item = (ContentItem) contentDao.findItemByUid("test");
                return item;
            }
        });
        
        
        // create item
        txThread1.start();
        txThread1.commit();
        txThread1.join();
        
        // read item at the same time
        txThread2.start();
        txThread3.start();
        
        // wait till reads are done
        while(txThread2.getRunnableResults("1")==null)
            Thread.sleep(50);
        while(txThread3.getRunnableResults("1")==null)
            Thread.sleep(50);
        
        // results of the read (should be same item)
        final ContentItem item1 = (ContentItem) txThread2.getRunnableResults("1");
        final ContentItem item2 = (ContentItem) txThread3.getRunnableResults("1");
        
        // delete item by thread 2
        txThread2.addRunnable("2", new TxRunnable() {
            public Object run() {
                
                contentDao.removeContent(item1);
                return item1;
            }
        });
        
        // wait for delete to complete
        while(txThread2.getRunnableResults("2")==null)
            Thread.sleep(50);
        
        // thread 2 wins with the commit
        txThread2.commit();
        txThread2.join();
        
        // now try to delete item by thread 3, should fail
        txThread3.addRunnable("2", new TxRunnable() {
            public Object run() {
                
                contentDao.removeContent(item2);
                return item2;
            }
        });
        
        txThread3.commit();
        txThread3.join();

        // results should be DataRetrievalFailureException
        Assert.assertTrue(txThread3.getRunnableResults("2") instanceof DataRetrievalFailureException);
       
        cleanupDb();
    }

    private User getUser(UserDao userDao, String username) {
        return helper.getUser(userDao, contentDao, username);
    }

    private FileItem generateTestContent() {
        return generateTestContent("test", "testuser");
    }

    private FileItem generateTestContent(String name, String owner)
             {
        FileItem content = new HibFileItem();
        content.setName(name);
        content.setDisplayName(name);
        try {
            content.setContent(helper.getBytes("testdata1.txt"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        content.setContentLanguage("en");
        content.setContentEncoding("UTF8");
        content.setContentType("text/text");
        content.setOwner(getUser(userDao, owner));
        content.addAttribute(new HibStringAttribute(new HibQName("customattribute"),
                "customattributevalue"));
        return content;
    }
    
    private NoteItem generateTestNote(String name, String owner)
            throws Exception {
        NoteItem content = new HibNoteItem();
        content.setName(name);
        content.setDisplayName(name);
        content.setOwner(getUser(userDao, owner));
        return content;
    }
    
    private HibItem getHibItem(Item item) {
        return (HibItem) item;
    }
    
    protected void cleanupDb () throws Exception {
        Connection conn = jdbcDataSource.getConnection();
        
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("delete from event_stamp");
        stmt.executeUpdate("delete from stamp");
        stmt.executeUpdate("delete from attribute");
        stmt.executeUpdate("delete from collection_item");
        stmt.executeUpdate("delete from tombstones");
        stmt.executeUpdate("delete from item");
        stmt.executeUpdate("delete from content_data");
        stmt.executeUpdate("delete from users");
        
        conn.commit();
    }
    
    static class TransactionThread extends Thread {
        
        private List<RunContext> toRun = new ArrayList<RunContext>();
        private Map<String, Object> doneSet = Collections.synchronizedMap(new HashMap<String, Object>());
        private boolean commit = false;
        HibernateTransactionHelper txHelper = null;
        
        TransactionThread(PlatformTransactionManager ptm, SessionFactory sf) {
            txHelper = new HibernateTransactionHelper(ptm, sf);
        }
        
        /**
         * Add code to be run inside transaction.
         * @param key identifier to use when checkign results of run
         * @param runnable code to run
         */
        public void addRunnable(String key, TxRunnable runnable) {
            RunContext rc = new RunContext();
            rc.key = key;
            rc.runnable = runnable;

            toRun.add(rc);
        }
        
        /**
         * Return results from runnable
         * @param key identifier
         * @return return value from runnable
         */
        public Object getRunnableResults(String key) {
            return doneSet.get(key);
        }
      
        public void run() {
            TransactionStatus ts = txHelper.startNewTransaction();
            
            while(!commit || toRun.size()>0) {
                RunContext rc = null;
                
                if(toRun.size()>0)
                    rc = toRun.remove(0); 
                
                
                if(rc==null) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    continue;
                }
                
                Object returnValue = null;
                
                try {
                    returnValue = rc.runnable.run();
                } catch (Throwable e) {
                    doneSet.put(rc.key, e);
                    txHelper.endTransaction(ts, true);
                    return;
                }
                
               
                doneSet.put(rc.key, returnValue);
            }
            
           
            txHelper.endTransaction(ts, false);
        }

        public void commit() {
            commit = true;
        }
        
        class RunContext {
            String key;
            TxRunnable runnable;
        }
        
    }
    
    interface TxRunnable {
        public Object run();
    }
}
