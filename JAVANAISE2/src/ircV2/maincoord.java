package ircV2;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.print.attribute.standard.Severity;

import jvn.JvnCoordImpl;
import jvn.JvnRemoteCoord;

public class maincoord {
	private static final Logger LOGGER = Logger.getLogger(maincoord.class.getName());

	public maincoord() {

	}

	public static void main(String[] args) throws Exception {

		Registry r = LocateRegistry.createRegistry(1069);
		JvnRemoteCoord coord = new JvnCoordImpl();
		r.rebind("coord", coord);
		LOGGER.info("Coordinator ready");
	}
}
