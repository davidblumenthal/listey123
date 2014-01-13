package com.blumenthal.listey;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

/**
 * This allows the client to load/sync data from the server info.
 * 
 * The server stores information in the following format:
 * 
 * user is entity
 *   - each list is a separate entity with:
 *      - a property of their owner.
 *      - a property list of other users allowed to view it
 *      - last_update time
 *
 * Given a user:
 *   - find their lists by looking up lists by the owner property.
 *   - find other user's lists they should be able to edit by looking up lists by other_user property (does this work with multiple values??)
 *   - Find who/what they granted privs to by looking up their lists and looking at the other_user property values
 *   
 * @author David
 *
 */

@SuppressWarnings("serial")
public class ListeyServlet extends HttpServlet {
	private static final Logger log = Logger.getLogger(ListeyServlet.class.getName());
	
	/** Overwrite the existing data with an update from the client
	    Note, this does nothing if the user is not currently logged in
	*/
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException{
		UserService userService = UserServiceFactory.getUserService();
        User user = userService.getCurrentUser();

    	String jsonString = req.getParameter("content");
        if (jsonString == null || jsonString.length() == 0) {
        	log.info("doPost: Nothing passed in content, using default");
        	jsonString = "{}";
        }
        if (user != null) {
        	log.info("doPost: User=" + user.getEmail());

        	//Load the current data from datastore
        	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        	ListeyDataMultipleUsers currentData = new ListeyDataMultipleUsers(datastore, user.getEmail());

        	//Parse the passed data
    		ListeyDataMultipleUsers passedData = ListeyDataMultipleUsers.fromJson(user.getEmail(), jsonString);
    		
    		//Compare the stored and the passed data.
    		DataStoreUniqueId uniqueIdCreator = new DataStoreUniqueId();
    		List<Entity> updateEntities = new ArrayList<Entity>();
    		List<Key> deleteKeys = new ArrayList<Key>();
    		ListeyDataMultipleUsers updatedData = ListeyDataMultipleUsers.compareAndUpdate(uniqueIdCreator, currentData, passedData, updateEntities, deleteKeys);
    		log.info("doPost: passedData = " + jsonString + ", currentData=" + currentData.toJson() + ", updatedData = " + updatedData.toJson());
    		
    		log.info("doPost: updating " + updateEntities.size() + " entities, deleting " + deleteKeys.size() + " entities");
    		datastore.put(updateEntities);
    		datastore.delete(deleteKeys);
    		
    		jsonString = updatedData.toJson();
        }//if user
        else {
        	resp.setStatus(403);//unauthorized
        	log.info("doPost: no user defined");
        }
        resp.setContentType("text/plain");
        resp.getWriter().print(jsonString);
        
	}//doPost
	
	
	//Get the existing data
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		UserService userService = UserServiceFactory.getUserService();
        User user = userService.getCurrentUser();

		resp.setContentType("text/plain");
		String listJSON = "{}";
        if (user == null) {
        	resp.setStatus(403);//unauthorized
        } else {
        	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        	ListeyDataMultipleUsers currentData = new ListeyDataMultipleUsers(datastore, user.getEmail());
        	listJSON = currentData.toJson();
        }
        resp.getWriter().print(listJSON);
	}
}
