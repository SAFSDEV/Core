/**
 * Copyright (C) SAS Institute, All rights reserved.
 * General Public License: https://www.gnu.org/licenses/gpl-3.0.en.html
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
**/
package org.safs.sockets;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TestOccupiedPort {
	public static int DEFAULT_SERVER_PORT = 2410;
	
	public static int MAX_SERVER_PORT = 2500;
	public static int NEXT_SERVER_PORT_PACE = 2;
	
	public List<ServerSocket> servers = new ArrayList<ServerSocket>();
	
	public TestOccupiedPort(){
		closeServerswhenShutdown();
	}
	
	public void closeServerswhenShutdown(){
		Runtime.getRuntime().addShutdownHook(new Thread(){
			public void run(){
				if(!servers.isEmpty()){
					Iterator<ServerSocket> iter = servers.iterator();
					while(iter.hasNext()){
						try {
							ServerSocket ser = iter.next();
							ser.close();
							debug("Closing socket server at port "+ser.getLocalPort());
						} catch (IOException e) {
							debug("Fail close socket server.");
						}
					}
				}
			}
		});
	}
	
	public void debug(String message){
		System.out.println(message);
	}
	
	public int getNextPort(int prevPort){

		if(prevPort+NEXT_SERVER_PORT_PACE > MAX_SERVER_PORT){
			throw new IllegalStateException("No more port to use.");
		}
		return prevPort+NEXT_SERVER_PORT_PACE;
	}
	
	public void occupyPort(int port){
		try {
			servers.add(new ServerSocket(port));
			debug("occupy port "+ port);
		} catch (IOException e) {
			debug("Fail to create socket server: Exception "+ e.getMessage());
		}
	}
	
	public static void main(String[] args){
		
		TestOccupiedPort top = new TestOccupiedPort();
		int port = DEFAULT_SERVER_PORT;
		ServerSocket server = null;

		//Occupy the next 5 ports from DEFAULT_SERVER_PORT
		for(int i=0;i<5;i++){
			top.occupyPort(port);
			port = top.getNextPort(port);
		}
		
		//reset port to DEFAULT_SERVER_PORT, start to create ServerSocket from DEFAULT_SERVER_PORT
		port = DEFAULT_SERVER_PORT;
		boolean keeptrying = true;
		while(keeptrying){
			try {
				server = new ServerSocket(port);
				top.servers.add(server);
				keeptrying = false;
				top.debug("Server is listening at port "+server.getLocalPort());
			} catch (IOException e) {
				top.debug("Fail to create socket server: Exception "+ e.getMessage());
				top.debug("Try next port "+top.getNextPort(port));
				port = top.getNextPort(port);
			}
		}
		
	}
	
}
