package jvn;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.UnicastRemoteObject;

import com.sun.corba.se.spi.orbutil.fsm.State;

public class JvnObjectImpl implements JvnObject {

	JvnServerImpl js;
	Serializable object;
	RWState state;
	Invalidate invalidate;
	int id;

	public JvnObjectImpl() throws RemoteException {
		// TODO Auto-generated constructor stub
		state = RWState.NL;
	}

	public JvnObjectImpl(Serializable o, JvnServerImpl jvnServerImpl, int joi) throws RemoteException {
		this.js = jvnServerImpl;
		this.id = joi;
		state = RWState.NL;
		object = o;
	}

	public void jvnLockRead() throws JvnException {
		switch (state) {
		case RC:
			state = RWState.R;
			break;
		case NL:
			js.jvnLockRead(id);
			state = RWState.R;
			break;
		case WC:
			state = RWState.RWC;
		default:
			System.out.println("erreur dans lockread : " + state.toString());
		}
	}

	public void jvnLockWrite() throws JvnException {
		switch (state) {

		case WC:
			state = RWState.W;
		case RC:
		case NL:
			js.jvnLockWrite(id);
			state = RWState.W;
			break;
		default:
			System.out.println("erreur dans lockread : " + state.toString());
		}

	}

	public void jvnUnLock() throws JvnException {
		// TODO Auto-generated method stub
		if (invalidate == Invalidate.NL) {
			switch (state) {
			case R:
				state = RWState.RC;
				break;
			case RWC:
			case W:
				state = RWState.WC;
				break;
			default : 
				System.out.println("erreur dans unlock.state : " + state.toString() +" /inv :  "+ invalidate.toString());
			}
		}
		else if(invalidate==Invalidate.R){
			switch (state) {
			case R:
			case W:
			case RWC:
				//TODO appel serveur notify
				state=RWState.NL;
				break;
				
			default :
				System.out.println("erreur dans unlock.state : " + state.toString() +" /inv :  "+ invalidate.toString());
			}
		}
		else if(invalidate==Invalidate.W){
			switch (state) {
			case W:
			case RWC:
				//TODO appel serveur notify
				state=RWState.NL;
				break;
				
			default :
				System.out.println("erreur dans unlock.state : " + state.toString() +" /inv :  "+ invalidate.toString());
			}
		}
		else if(invalidate==Invalidate.RW){
			switch (state) {
			case W:
				//TODO appel serveur notify
				state=RWState.NL;
				break;
				
			default :
				System.out.println("erreur dans unlock.state : " + state.toString() +" /inv :  "+ invalidate.toString());
			}
		}

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
		case WC:
			state = RWState.NL;
			break;

		case R:
		case W:
		case RWC:
			invalidate = Invalidate.R;
			break;
		default:
			// TODO trow exception
			System.out.println("erreur dans invalidatereader");
		}

	}

	public Serializable jvnInvalidateWriter() throws JvnException {
		switch (state) {
		case WC:
			state = RWState.NL;
			break;

		case W:
			invalidate = Invalidate.W;
		case RWC:
			state = RWState.R;
			invalidate = Invalidate.W;
			break;
		default:
			// TODO trow exception
			System.out.println("erreur dans invalidatewriter");
		}
		return object;
	}

	public Serializable jvnInvalidateWriterForReader() throws JvnException {
		switch (state) {
		case RWC:
		case WC:
			state = RWState.RC;
			break;
		case W:
			invalidate = Invalidate.RW;
			break;
		default:
			// TODO trow exception
			System.out.println("erreur dans invalidatewriterforreader");
		}
		return object;
	}

}
