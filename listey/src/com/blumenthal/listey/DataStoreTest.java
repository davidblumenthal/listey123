/**
 * 
 */
package com.blumenthal.listey;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.datastore.Transaction;

/**
 * @author David
 *
 */
@SuppressWarnings("serial")
public class DataStoreTest extends HttpServlet {
	//private static final Logger log = Logger.getLogger(ListeyServlet.class.getName());
	
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setContentType("text/plain");
		StringBuilder output = new StringBuilder();
		boolean batchMode = req.getParameter("batch") != null;
		output.append("Datastore Test: batchMode = " + batchMode + "\n");
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		long startTime = System.nanoTime();
		output.append((System.nanoTime() - startTime)/1_000_000+" Loaded datastore\n");
		output.append("Type="+req.getParameter("type")+"\n");
		List<Entity> entities = null;
		List<Key> keys=null;
		int numIter = req.getParameter("iter") != null ? Integer.parseInt(req.getParameter("iter")) : 2;
		for (int loopCount=1; loopCount<=numIter; loopCount++){
			startTime = System.nanoTime();
			entities = new ArrayList<Entity>();
			keys = new ArrayList<Key>();
			if ("unique".equals(req.getParameter("type"))) {
				DataStoreUniqueId uniqueIdObj = new DataStoreUniqueId();
				String uniqueId = uniqueIdObj.getUniqueId();
				output.append("*** uniqueId = " + uniqueId + "\n");
			}
			else if ("transaction".equals(req.getParameter("type"))) {
				Transaction txn = datastore.beginTransaction();
				try {
					output.append("****** Testing all one entity, with transaction - iter " + loopCount + "\n");
					Key userKey = KeyFactory.createKey("testuser", "test@blumenthal.com");
					keys.add(userKey);
					Entity userEntity = new Entity(userKey);
					userEntity.setProperty("content", new Text("Blah blah"));
					if (batchMode) {
						entities.add(userEntity);
					} else {
						datastore.put(userEntity);
					}
					output.append((System.nanoTime() - startTime)/1_000_000+" Wrote userEntity\n");
					for (int i=0; i<10; i++) {
						Key listKey = KeyFactory.createKey(userKey,  "list", "List"+i);
						keys.add(listKey);
						Entity listEntity = new Entity(listKey);
						listEntity.setProperty("listinfo",  new Text("List blah blah"));
						if (batchMode) {
							entities.add(listEntity);
						} else {
							datastore.put(listEntity);
						}
						output.append((System.nanoTime() - startTime)/1_000_000+" Wrote List"+i+" entity\n");
					}//for
					if (batchMode) {
						datastore.put(entities);
						output.append((System.nanoTime() - startTime)/1_000_000+" Wrote entities in batch mode\n");
					}
					txn.commit();
				} finally {
					if (txn.isActive()) {
						txn.rollback();
					}
				}
			}//if transaction

			else if ("separate".equals(req.getParameter("type"))) {
				output.append("Testing all with different entities, no transaction - iter " + loopCount + "\n");
				Key userKey = KeyFactory.createKey("testuser", "test@blumenthal.com");
				keys.add(userKey);
				Entity userEntity = new Entity(userKey);
				userEntity.setProperty("content", new Text("Blah blah"));
				if (batchMode) {
					entities.add(userEntity);
				} else {
					datastore.put(userEntity);
				}
				output.append((System.nanoTime() - startTime)/1_000_000+" Wrote userEntity\n");
				for (int i=0; i<10; i++) {
					Key listKey = KeyFactory.createKey("list", "test@blumenthal.com List"+i);
					keys.add(listKey);
					Entity listEntity = new Entity(listKey);
					listEntity.setProperty("listinfo",  new Text("List blah blah"));
					listEntity.setProperty("userEntityKey", userKey.getName());
					if (batchMode) {
						entities.add(listEntity);
					} else {
						datastore.put(listEntity);
					}
					output.append((System.nanoTime() - startTime)/1_000_000+" Wrote List"+i+" entity\n");
				}//for
				if (batchMode) {
					datastore.put(entities);
					output.append((System.nanoTime() - startTime)/1_000_000+" Wrote entities in batch mode\n");
				}
			}//if separate

			else {
				output.append("Testing all one entity, no transaction - iter " + loopCount + "\n");
				Key userKey = KeyFactory.createKey("testuser", "test@blumenthal.com");
				keys.add(userKey);
				Entity userEntity = new Entity(userKey);
				userEntity.setProperty("content", new Text("Blah blah"));
				if (batchMode) {
					entities.add(userEntity);
				} else {
					datastore.put(userEntity);
				}
				output.append((System.nanoTime() - startTime)/1_000_000+" Wrote userEntity\n");
				for (int i=0; i<10; i++) {
					Key listKey = KeyFactory.createKey(userKey,  "list", "List"+i);
					keys.add(listKey);
					Entity listEntity = new Entity(listKey);
					listEntity.setProperty("listinfo",  new Text("List blah blah"));
					if (batchMode) {
						entities.add(listEntity);
					} else {
						datastore.put(listEntity);
					}
					output.append((System.nanoTime() - startTime)/1_000_000+" Wrote List"+i+" entity\n");
				}//for
				if (batchMode) {
					datastore.put(entities);
					output.append((System.nanoTime() - startTime)/1_000_000+" Wrote entities in batch mode\n");
				}
			}//1 entity no transaction
		}//foreach iter
		
		if (req.getParameter("noDelete") == null) {
			startTime = System.nanoTime();
			datastore.delete(keys);
			output.append((System.nanoTime() - startTime)/1_000_000+" Deleted " + keys.size() +" keys\n");
		}
		
		resp.getWriter().print(output.toString());
	}
}
