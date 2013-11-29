package com.blumenthal.listey;
import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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

    	String passedSerializedHash = req.getParameter("content");
    	Long passedLastUpdateTS = null;
    	if (req.getParameter("lastUpdate") != null && req.getParameter("lastUpdate").length() > 0) {
    		passedLastUpdateTS = Long.parseLong(req.getParameter("lastUpdate"));
    	}
        if (passedSerializedHash == null || passedSerializedHash.length() == 0) {
        	log.info("doPost: Nothing passed in content, using default");
        	passedSerializedHash = "{}";
        }
        if (user != null) {
	        // We have one entity group per user
        	Key userKey = KeyFactory.createKey("user", user.getEmail());
        	log.info("doPost: User=" + user.getEmail());
        	String currentSerializedHash = "{}";
        	Long currentLastUpdateTS = null;
        	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        	ListeyDataMultipleUsers currentData = new ListeyDataMultipleUsers();
        	
        	//Load the current user data from the datastore by the user's email address
        	ListeyDataOneUser currentUserData = new ListeyDataOneUser(datastore, user.getEmail());
        	currentData.userData.put(user.getEmail(), currentUserData);
        	
        	//Load other user's lists that this user should have access to
        	//TODO

        	//Parse the passed data
        	Gson gson = new GsonBuilder().registerTypeAdapter(ListInfoDeserializer.class, new ListInfoDeserializer()).create();
    		ListeyDataMultipleUsers passedData = gson.fromJson(passedSerializedHash, ListeyDataMultipleUsers.class);
    		
    		//Compare the stored and the passed data.
        	//If the passed data element is newer than the current data element, replace
        	//the current data
    		
        	if (passedLastUpdateTS != null
        			&& (currentLastUpdateTS == null || currentLastUpdateTS < passedLastUpdateTS)) {
        		Entity userData = new Entity(userKey);
        		userData.setProperty("content", new Text(passedSerializedHash));
        		userData.setProperty("lastUpdate", passedLastUpdateTS.toString());
        		log.info("doPost: updating datastore.  lastUpdateTS=" + passedLastUpdateTS + ", serializedHash=" + passedSerializedHash);
        		datastore.put(userData);
        	}//if should replace
        	else {
        		passedSerializedHash = currentSerializedHash;
        	}
        }//if user
        else {
        	resp.setStatus(403);//unauthorized
        	log.info("doPost: no user defined");
        }
        resp.setContentType("text/plain");
        resp.getWriter().print(passedSerializedHash);
        
	}//doPost
	
	
	//Get the existing data
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		UserService userService = UserServiceFactory.getUserService();
        User user = userService.getCurrentUser();

		resp.setContentType("text/plain");
		Text listJSON = new Text("{}");
        if (user == null) {
        	resp.setStatus(403);//unauthorized
        } else {
        	Key userKey = KeyFactory.createKey("user", user.getEmail());
        	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        	try {
        		Entity userData = datastore.get(userKey);
        		listJSON = (Text) userData.getProperty("content");
        	} catch (EntityNotFoundException e) {
        	    //No data exists yet for this user, use default
        	}
        }
        resp.getWriter().print(listJSON);
	}
}
