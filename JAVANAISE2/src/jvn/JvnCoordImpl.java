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
	private HashMap<JvnRemoteServer, List<JvnObject>> distServersObjects;

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
		distServersObjects = new HashMap<JvnRemoteServer, List<JvnObject>>();
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
		LOGGER.info("Registering object " + jon);
		int tempIdentifier = jo.jvnGetObjectId();
		jvnObjects.put(tempIdentifier, jo);
		jvnObjectNames.put(jon, tempIdentifier);

		// Add the object to the list
		List<JvnObject> serverObjects = distServersObjects.get(js);
		if (serverObjects == null) { // First object on the server, we need to
										// init the list
			serverObjects = new ArrayList<JvnObject>();
		}
		serverObjects.add(jo);
		distServersObjects.put(js, serverObjects);

		// Init read mode list
		serversInReadMode.put(jo.jvnGetObjectId(), new ArrayList<JvnRemoteServer>());

		LOGGER.info("Registered object " + jon);
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
		LOGGER.info("Looking up for object " + jon);
		JvnObject jo = jvnObjects.get(jvnObjectNames.get(jon));
		if (jo == null) { // Object not found
			LOGGER.info("Object " + jon + " not found.");
			return null;
		} else {
			LOGGER.info("found, id = " + jo.jvnGetObjectId());

			if (distServersObjects.get(js) == null) { // This server is not
														// registered
				LOGGER.info("Server not registered, registering it.");
				distServersObjects.put(js, new ArrayList<JvnObject>());
			}

			distServersObjects.get(js).add(jvnObjects.get(jon));

			return jvnObjects.get(jvnObjectNames.get(jon));
		}
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
		LOGGER.info("Locking read on obj " + joi);
		JvnRemoteServer serverInWriteMode = serversInWriteMode.get(joi);
		Serializable tmpObj = null;
		if (serverInWriteMode == null) { // no one is in write mode
			LOGGER.info("no one is writing, passing on");
			tmpObj = jvnObjects.get(joi).jvnGetObjectState();
		} else {
			// Someone is in write mode on this object
			LOGGER.info("someone has write lock " + serverInWriteMode.toString());
			tmpObj = serverInWriteMode.jvnInvalidateWriterForReader(joi);
			serversInWriteMode.remove(joi);
			serversInReadMode.get(joi).add(js);

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
		LOGGER.info("Locking write on object " + joi);
		JvnRemoteServer serverInWriteMode = serversInWriteMode.get(joi);
		Serializable tmpObj = null;
		if (serverInWriteMode == null) {
			JvnObject jo = jvnObjects.get(joi);
			if (jo != null) {
				tmpObj = jvnObjects.get(joi).jvnGetObjectState();
			}
		} else {
			tmpObj = serverInWriteMode.jvnInvalidateWriter(joi);
			serversInWriteMode.remove(joi);
			jvnObjects.get(joi).set(tmpObj);
		}

		// Now remove all readers
		List<JvnRemoteServer> l = serversInReadMode.get(joi);
		if (l == null) {
			l = new ArrayList<JvnRemoteServer>();
		} else {
			LOGGER.info("someone is reading ");
			for (JvnRemoteServer remoteServer : l) {
				if (!remoteServer.equals(js)) {
					remoteServer.jvnInvalidateReader(joi);
				}
			}
		}
		ArrayList<JvnRemoteServer> tmp = new ArrayList<JvnRemoteServer>();
		tmp.add(js);
		serversInReadMode.put(joi, tmp);
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
