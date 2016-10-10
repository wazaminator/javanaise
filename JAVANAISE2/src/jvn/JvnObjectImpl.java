package jvn;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.logging.Logger;

@SuppressWarnings("serial")
public class JvnObjectImpl implements JvnObject {

	private static final Logger LOGGER = Logger.getLogger(JvnObjectImpl.class.getName());

	Serializable object;
	public transient RWState state;
	int id;

	public JvnObjectImpl(int joId, Serializable o) throws RemoteException {
		this.id = joId;
		state = RWState.NL;
		object = o;
	}
	public JvnObjectImpl(int i,Serializable o,boolean g)throws RemoteException {
		this.id = i;
		state = RWState.W;
		object = o;
	}

	public void jvnLockRead() throws JvnException {
		LOGGER.info("trying to lockread +("+state+")");
		switch (state) {
		case NL:
			object = JvnServerImpl.jvnGetServer().jvnLockRead(id);
			state = RWState.R;
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
		LOGGER.info("trying to lockwrite +("+state+")");
		switch (state) {
		case NL:
			JvnServerImpl.jvnGetServer().jvnLockWrite(id);
			state = RWState.W;
			break;
		case RC:		
			JvnServerImpl.jvnGetServer().jvnLockWrite(id);
			state = RWState.W;
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
			state = RWState.NL;
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
			state = RWState.NL;
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
				e.printStackTrace();
			}
			state = RWState.RC;
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
