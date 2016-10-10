/***
 * JAVANAISE Implementation
 * JvnServerImpl class
 * Contact: 
 *
 * Authors: 
 */

package jvn;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.*;

public class JvnServerImpl extends UnicastRemoteObject implements JvnLocalServer, JvnRemoteServer {

	private static final Logger LOGGER = Logger.getLogger(JvnServerImpl.class.getName());

	// A JVN server is managed as a singleton
	private static JvnServerImpl js = null;
	private JvnRemoteCoord coord;
	private HashMap<Integer, JvnObject> objects = new HashMap<Integer, JvnObject>();

	/**
	 * Default constructor
	 * 
	 * @throws JvnException
	 **/
	private JvnServerImpl() throws Exception {
		super();
		Registry r = LocateRegistry.getRegistry(1069);
		coord = (JvnRemoteCoord) r.lookup("coord");

	}

	/**
	 * Static method allowing an application to get a reference to a JVN server
	 * instance
	 * 
	 * @throws JvnException
	 **/
	public static JvnServerImpl jvnGetServer() {
		if (js == null) {
			try {
				js = new JvnServerImpl();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		return js;
	}

	/**
	 * The JVN service is not used anymore
	 * 
	 * @throws JvnException
	 **/
	public void jvnTerminate() throws jvn.JvnException {
		try {
			coord.jvnTerminate(js);
		} catch (RemoteException e) {
			LOGGER.log(Level.WARNING, "Error while notifying coordinator of our termination.", e);
			e.printStackTrace();
		}
	}

	/**
	 * creation of a JVN object
	 * 
	 * @param o
	 *            : the JVN object state
	 * @throws JvnException
	 **/
	public JvnObject jvnCreateObject(Serializable o) throws jvn.JvnException {
		LOGGER.info("Creating new object");
		JvnObject jo;
		try {
			jo = new JvnObjectImpl(coord.jvnGetObjectId(), o);

			return jo;
		} catch (RemoteException e) {
			LOGGER.log(Level.WARNING, "Error while creating object.", e);
			throw new JvnException("Cannot create object.");
		}

	}

	/**
	 * Associate a symbolic name with a JVN object
	 * 
	 * @param jon
	 *            : the JVN object name
	 * @param jo
	 *            : the JVN object
	 * @throws JvnException
	 **/
	public void jvnRegisterObject(String jon, JvnObject jo) throws jvn.JvnException {
		LOGGER.info("Registering object " + jon);
		try {
			coord.jvnRegisterObject(jon, jo, this);
			objects.put(jo.jvnGetObjectId(), jo);
		} catch (Exception e) {
			throw new JvnException("Cannot register object.");
		}
	}

	/**
	 * Provide the reference of a JVN object beeing given its symbolic name
	 * 
	 * @param jon
	 *            : the JVN object name
	 * @return the JVN object
	 * @throws JvnException
	 **/
	public JvnObject jvnLookupObject(String jon) throws jvn.JvnException {
		LOGGER.info("Looking up for object " + jon);
		JvnObject jo = null;
		try {
			jo = coord.jvnLookupObject(jon, this);
		} catch (RemoteException e) {
			throw new JvnException("Error while looking up for object " + jon);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		return jo;
	}

	/**
	 * Get a Read lock on a JVN object
	 * 
	 * @param joi
	 *            : the JVN object identification
	 * @return the current JVN object state
	 * @throws JvnException
	 **/
	public Serializable jvnLockRead(int joi) throws JvnException {
		try {
			return coord.jvnLockRead(joi, this);
		} catch (RemoteException e) {
			throw new JvnException("Cannot take read lock");
		}
	}

	/**
	 * Get a Write lock on a JVN object
	 * 
	 * @param joi
	 *            : the JVN object identification
	 * @return the current JVN object state
	 * @throws JvnException
	 **/
	public Serializable jvnLockWrite(int joi) throws JvnException {
		
		try {
			return coord.jvnLockWrite(joi, this);
		} catch (RemoteException e) {
			throw new JvnException("Cannot take write lock");
		}
	}

	/**
	 * Invalidate the Read lock of the JVN object identified by id called by the
	 * JvnCoord
	 * 
	 * @param joi
	 *            : the JVN object id
	 * @return void
	 * @throws java.rmi.RemoteException,JvnException
	 **/
	public void jvnInvalidateReader(int joi) throws java.rmi.RemoteException, jvn.JvnException {
		objects.get(joi).jvnInvalidateReader();
	};

	/**
	 * Invalidate the Write lock of the JVN object identified by id
	 * 
	 * @param joi
	 *            : the JVN object id
	 * @return the current JVN object state
	 * @throws java.rmi.RemoteException,JvnException
	 **/
	public Serializable jvnInvalidateWriter(int joi) throws java.rmi.RemoteException, jvn.JvnException {
		return objects.get(joi).jvnInvalidateWriter();
	};

	/**
	 * Reduce the Write lock of the JVN object identified by id
	 * 
	 * @param joi
	 *            : the JVN object id
	 * @return the current JVN object state
	 * @throws java.rmi.RemoteException,JvnException
	 **/
	public Serializable jvnInvalidateWriterForReader(int joi) throws java.rmi.RemoteException, jvn.JvnException {
		return objects.get(joi).jvnInvalidateWriterForReader();
	};

}
