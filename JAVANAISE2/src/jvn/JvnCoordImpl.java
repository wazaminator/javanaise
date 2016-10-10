/***
 * JAVANAISE Implementation
 * JvnServerImpl class
 * Contact: 
 *
 * Authors: 
 */

package jvn;

import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import java.io.Serializable;

public class JvnCoordImpl extends UnicastRemoteObject implements JvnRemoteCoord {

	private static final Logger LOGGER = Logger.getLogger(JvnCoordImpl.class.getName());

	/**
	 * Object names - Objects
	 */
	private HashMap<Integer, JvnObject> jvnObjects;

	/**
	 * Object ids - object names
	 */
	private HashMap<String, Integer> jvnObjectNames;
	
	/**
	 * Dist objects
	 */
	private HashMap<JvnRemoteServer, List<JvnObject>> distObjects;
	
	/**
	 * List of read-mode servers corresponding to objects
	 */
	private HashMap<Integer, List<JvnRemoteServer>> serversInReadMode;
	
	/**
	 * The server currently in write-mode on an object corresponding to its id
	 */
	private HashMap<Integer, JvnRemoteServer> serversInWriteMode;
	

	/**
	 * The object id for naming uniqueness purpose
	 */
	private int id;
	
	/**
	 * Default constructor
	 * 
	 * @throws JvnException
	 **/
	public JvnCoordImpl() throws Exception {
		jvnObjects = new HashMap<Integer, JvnObject>();
		jvnObjectNames = new HashMap<String, Integer>();
		distObjects = new HashMap<JvnRemoteServer, List<JvnObject>>();
		serversInReadMode = new HashMap<Integer, List<JvnRemoteServer>>();
		serversInWriteMode = new HashMap<Integer, JvnRemoteServer>();
		id = 0;
	}

	/**
	 * Allocate a NEW JVN object id (usually allocated to a newly created JVN
	 * object)
	 * 
	 * @throws java.rmi.RemoteException,JvnException
	 **/
	public int jvnGetObjectId() throws java.rmi.RemoteException, jvn.JvnException {
		return id++;
	}

	/**
	 * Associate a symbolic name with a JVN object
	 * 
	 * @param jon
	 *            : the JVN object name
	 * @param jo
	 *            : the JVN object
	 * @param joi
	 *            : the JVN object identification
	 * @param js
	 *            : the remote reference of the JVNServer
	 * @throws java.rmi.RemoteException,JvnException
	 **/
	public void jvnRegisterObject(String jon, JvnObject jo, JvnRemoteServer js)
			throws java.rmi.RemoteException, jvn.JvnException {
		LOGGER.info("Registering object "+jon);
		int tempIdentifier = jo.jvnGetObjectId();
		jvnObjects.put(tempIdentifier, jo);
		jvnObjectNames.put(jon, tempIdentifier);
		
		//Add the object to the list
		List<JvnObject> serverObjects = distObjects.get(js);
		if(serverObjects == null){ //First object on the server, we need to init the list 
			serverObjects = new ArrayList<JvnObject>();
		}
		serverObjects.add(jo);
		distObjects.put(js, serverObjects);
		
		//Init read mode list
		serversInReadMode.put(jo.jvnGetObjectId(), new ArrayList<JvnRemoteServer>());
		
		//Set remoteserver as writer
		serversInWriteMode.put(jo.jvnGetObjectId(), js);
		LOGGER.info("Registered object "+jon);
	}

	/**
	 * Get the reference of a JVN object managed by a given JVN server
	 * 
	 * @param jon
	 *            : the JVN object name
	 * @param js
	 *            : the remote reference of the JVNServer
	 * @throws java.rmi.RemoteException,JvnException
	 **/
	public JvnObject jvnLookupObject(String jon, JvnRemoteServer js) throws java.rmi.RemoteException, jvn.JvnException {
		LOGGER.info("Looking up for object "+jon);
		
		if(jvnObjects.get(jvnObjectNames.get(jon)) == null){ //Object not found
			LOGGER.info("Object "+jon+" not found.");
			return null;
		}
		
		if(distObjects.get(js) == null){ // This server is not registered
			LOGGER.info("Server not registered, registering it.");
			distObjects.put(js, new ArrayList<JvnObject>());
		}
		
		distObjects.get(js).add(jvnObjects.get(jon));
		
		return jvnObjects.get(jon);
	}

	/**
	 * Get a Read lock on a JVN object managed by a given JVN server
	 * 
	 * @param joi
	 *            : the JVN object identification
	 * @param js
	 *            : the remote reference of the server
	 * @return the current JVN object state
	 * @throws java.rmi.RemoteException,
	 *             JvnException
	 **/
	public Serializable jvnLockRead(int joi, JvnRemoteServer js) throws java.rmi.RemoteException, JvnException {
		LOGGER.info("Locking read on obj "+joi);
		JvnRemoteServer serverInWriteMode = serversInWriteMode.get(joi);
		Serializable tmpObj = null;
		if(serverInWriteMode == null){ //no one is in write mode
			tmpObj = jvnObjects.get(joi).jvnGetObjectState();
		}else{
			//Someone is in write mode on this object
			if(!js.equals(serverInWriteMode)){
				tmpObj = serverInWriteMode.jvnInvalidateWriterForReader(joi);
				serversInWriteMode.remove(joi);
				serversInReadMode.get(joi).add(serverInWriteMode);
			}
			jvnObjects.get(joi).set(tmpObj);
		}
		
		serversInReadMode.get(joi).add(js);
		return tmpObj;
	}

	/**
	 * Get a Write lock on a JVN object managed by a given JVN server
	 * 
	 * @param joi
	 *            : the JVN object identification
	 * @param js
	 *            : the remote reference of the server
	 * @return the current JVN object state
	 * @throws java.rmi.RemoteException,
	 *             JvnException
	 **/
	public Serializable jvnLockWrite(int joi, JvnRemoteServer js) throws java.rmi.RemoteException, JvnException {
		LOGGER.info("Locking write on object "+joi);
		JvnRemoteServer serverInWriteMode = serversInWriteMode.get(joi);
		Serializable tmpObj = null;
		if(serverInWriteMode == null || serverInWriteMode.equals(js)){ //no one or self is in write mode
			tmpObj = jvnObjects.get(joi).jvnGetObjectState();
		}else{
			tmpObj = serverInWriteMode.jvnInvalidateWriter(joi);
			serversInWriteMode.remove(joi);
			jvnObjects.get(joi).set(tmpObj);
		}
		
		//Now remove all readers
		for (JvnRemoteServer remoteServer : serversInReadMode.get(joi)) {
			if(!remoteServer.equals(js)){ //If its not me
				remoteServer.jvnInvalidateReader(joi);
			}
		}
		serversInReadMode.put(joi, new ArrayList<JvnRemoteServer>());
		serversInWriteMode.put(joi, js);
		return tmpObj;
	}

	/**
	 * A JVN server terminates
	 * 
	 * @param js
	 *            : the remote reference of the server
	 * @throws java.rmi.RemoteException,
	 *             JvnException
	 **/
	public void jvnTerminate(JvnRemoteServer js) throws java.rmi.RemoteException, JvnException {
		// to be completed
	}
}
