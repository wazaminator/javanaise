package jvn;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.UnicastRemoteObject;

public class JvnObjectImpl extends UnicastRemoteObject implements JvnObject {

	
	Serializable object;
	RWState state;
	public JvnObjectImpl() throws RemoteException {
		// TODO Auto-generated constructor stub
	}

	public JvnObjectImpl(int port) throws RemoteException {
		super(port);
		// TODO Auto-generated constructor stub
	}

	public JvnObjectImpl(int port, RMIClientSocketFactory csf, RMIServerSocketFactory ssf) throws RemoteException {
		super(port, csf, ssf);
		// TODO Auto-generated constructor stub
	}

	public void jvnLockRead() throws JvnException {
		// TODO Auto-generated method stub

	}

	public void jvnLockWrite() throws JvnException {
		// TODO Auto-generated method stub

	}

	public void jvnUnLock() throws JvnException {
		// TODO Auto-generated method stub

	}

	public int jvnGetObjectId() throws JvnException {
		// TODO Auto-generated method stub
		return 0;
	}

	public Serializable jvnGetObjectState() throws JvnException {
		// TODO Auto-generated method stub
		return null;
	}

	public void jvnInvalidateReader() throws JvnException {
		// TODO Auto-generated method stub

	}

	public Serializable jvnInvalidateWriter() throws JvnException {
		// TODO Auto-generated method stub
		return null;
	}

	public Serializable jvnInvalidateWriterForReader() throws JvnException {
		// TODO Auto-generated method stub
		return null;
	}

}
