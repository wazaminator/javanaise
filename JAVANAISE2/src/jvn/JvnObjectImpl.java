package jvn;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Logger;

import com.sun.corba.se.spi.orbutil.fsm.State;

public class JvnObjectImpl implements JvnObject {

	private static final Logger LOGGER = Logger.getLogger(JvnObjectImpl.class.getName());

	Serializable object;
	RWState state;
	int id;

	public JvnObjectImpl(int joId, Serializable o) throws RemoteException {
		this.id = joId;
		state = RWState.W;
		object = o;
	}

	public void jvnLockRead() throws JvnException {
		switch (state) {
		case NL:
			state = RWState.R;
			object = JvnServerImpl.jvnGetServer().jvnLockRead(id);
			break;
		case RC:
			state = RWState.R;
			break;
		case WC:
			state = RWState.RWC;
			break;
		default:
			throw new JvnException("Error while taking read lock ("+state+")");
		}
	}

	public void jvnLockWrite() throws JvnException {
		switch (state) {
		case NL:
			state = RWState.W;
			JvnServerImpl.jvnGetServer().jvnLockWrite(id);
			break;
		case RC:
			state = RWState.W;
			JvnServerImpl.jvnGetServer().jvnLockWrite(id);
			break;
		case WC:
			state = RWState.W;
			break;
		default:
			throw new JvnException("Error while taking write lock ("+state+")");
		}
	}

	synchronized public void jvnUnLock() throws JvnException {
		switch (state) {
		case R:
			state = RWState.RC;
			break;
		case RWC:
		case W:
			state = RWState.WC;
			break;
		default:
			throw new JvnException("erreur dans jvnunlock ("+state+")");
		}
		
		notify();
	}

	public int jvnGetObjectId() throws JvnException {
		return id;
	}

	public Serializable jvnGetObjectState() throws JvnException {
		return object;
	}

	public void jvnInvalidateReader() throws JvnException {
		switch (state) {
		case RC:
			state = RWState.NL;
			break;
		case R:
		case RWC:
			try {
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		default:
			throw new JvnException("erreur dans invalidatereader ("+state+")");
		}

	}

	public Serializable jvnInvalidateWriter() throws JvnException {
		switch (state) {
		case WC:
			state = RWState.NL;
			break;

		case W:
		case RWC:
			try {
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		default:
			throw new JvnException("erreur dans invalidatewriter ("+state+")");
		}
		return object;
	}

	public Serializable jvnInvalidateWriterForReader() throws JvnException {
		switch (state) {
		case RWC:
		case W:
			try {
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case WC:
			state = RWState.RC;
			break;
		default:
			throw new JvnException("erreur dans invalidatewriterforreader ("+state+")");
		}
		return object;
	}

	public void set(Serializable obj) throws JvnException {
		object = obj;
	}
	
	

}
