package jvn;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.UnicastRemoteObject;

import com.sun.corba.se.spi.orbutil.fsm.State;

public class JvnObjectImpl extends UnicastRemoteObject implements JvnObject {

	
	Serializable object;
	RWState state;
	Invalidate invalidate;
	public JvnObjectImpl() throws RemoteException {
		// TODO Auto-generated constructor stub
		state=RWState.NL;
	}

	public JvnObjectImpl(int port) throws RemoteException {
		super(port);
		// TODO Auto-generated constructor stub
		state=RWState.NL;
	}

	public JvnObjectImpl(int port, RMIClientSocketFactory csf, RMIServerSocketFactory ssf) throws RemoteException {
		super(port, csf, ssf);
		// TODO Auto-generated constructor stub
		state=RWState.NL;
	}

	public JvnObjectImpl(Serializable o) throws RemoteException{
		// TODO Auto-generated constructor stub
		state=RWState.NL;
		object=o;
	}

	public void jvnLockRead() throws JvnException {
		switch (state) {
		case  RC : 
		case  NL :	
			state=RWState.R;
			//TODO apel serv
			break;
		case WC : 
			state=RWState.RWC;
		default :
			System.out.println("erreur dans lockread");
		}
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
		return object;
	}

	public void jvnInvalidateReader() throws JvnException {
		switch (state) {
		case  RC : 
		case WC: 
		  state=RWState.NL;
		  break;
		
		case R :
		case W :
		case RWC:
		invalidate=Invalidate.R;
		break;
		default : 
			//TODO trow exception
			System.out.println("erreur dans invalidatereader");
		}
		
	}

	public Serializable jvnInvalidateWriter() throws JvnException {
		switch (state) {
		case WC: 
		  state=RWState.NL;
		  break;
		
		case W :
			invalidate=Invalidate.W;
		case RWC:
		state=RWState.R;
		break;
		default : 
			//TODO trow exception
			System.out.println("erreur dans invalidatewriter");
		}	
		return object;
	}

	public Serializable jvnInvalidateWriterForReader() throws JvnException {
		switch (state) {
		case WC: 
		  state=RWState.R;
		  break;
		
		case W :
			invalidate=Invalidate.RW;
		case RWC:
		state=RWState.R;
		break;
		default : 
			//TODO trow exception
			System.out.println("erreur dans invalidatewriterforreader");
		}	
		return object;
	}

}
