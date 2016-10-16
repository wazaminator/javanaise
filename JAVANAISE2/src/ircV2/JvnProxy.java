package ircV2;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.logging.Logger;

import jvn.JvnException;
import jvn.JvnObject;
import jvn.JvnServerImpl;

public class JvnProxy implements InvocationHandler {
	private static final Logger LOGGER = Logger.getLogger(JvnProxy.class.getName());

	JvnObject jo;

	public JvnProxy(Serializable obj, String name) {
		JvnServerImpl js = JvnServerImpl.jvnGetServer();
		try {
			this.jo = js.jvnLookupObject(name);
		} catch (JvnException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (jo == null) {
			try {
				this.jo = js.jvnCreateObject(obj);
				this.jo.jvnUnLock();
				js.jvnRegisterObject(name, jo);
			} catch (JvnException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Object res = new Object();
		if(method.isAnnotationPresent(JvnRW.class)){
			switch(method.getAnnotation(JvnRW.class).mode()){
			case READ:
				jo.jvnLockRead();
				break;
			case WRITE:
				jo.jvnLockWrite();
				break;
			default:
				throw new JvnException("Proxy: Invoke error");
			}
			res = method.invoke(jo.jvnGetObjectState(), args);
			jo.jvnUnLock();
		}
		return res;
	}

	public static Object getInstance(Serializable obj, String name) {
		Object proxy = Proxy.newProxyInstance(obj.getClass().getClassLoader(), obj.getClass().getInterfaces(),
				new JvnProxy(obj, name));
		return proxy;

	}

}
