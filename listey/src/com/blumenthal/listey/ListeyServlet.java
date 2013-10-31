package com.blumenthal.listey;
import java.io.IOException;
import java.util.logging.Logger;
import javax.servlet.http.*;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;



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
        	try {
        		Entity userData = datastore.get(userKey);
        		currentSerializedHash = ((Text) userData.getProperty("content")).getValue();
        		if (userData.hasProperty("lastUpdate")) {
        			currentLastUpdateTS = Long.parseLong((String) (userData).getProperty("lastUpdate"));
        		}
        		log.info("doPost: currentLastUpdateTS=" + currentLastUpdateTS + ", currentSerializedHash=" + currentSerializedHash);
        	} catch (EntityNotFoundException e) {
        	    //No data exists yet for this user, use default
        		log.info("doPost: no data exists yet for this user, using default");
        	}
        	//Parse the current data and the passed data and compare dates
        	//If the passed data is newer than the current data, replace
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
