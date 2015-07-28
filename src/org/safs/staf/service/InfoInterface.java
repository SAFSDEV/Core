package org.safs.staf.service;

/**
 * This class serve as encapsulate the parameter information defined in STAF2.0 and STAF3.0
 * And it will be used in our AbstractXXXService
 */
public class InfoInterface {
    static public class InitInfo
    {
        public String name;
        public String parms;
        public int serviceType;
        public String writeLocation;

        public InitInfo(String name, String parms,
                        int serviceType, String writeLocation)
        {
            this.name = name;
            this.parms = parms;
            this.serviceType = serviceType;
            this.writeLocation = writeLocation;
        }
        public InitInfo(String name, String parms) {
			this.name = name;
			this.parms = parms;
		}
    }

    static public class RequestInfo
    {
        public String  stafInstanceUUID;
        public String  machine;
        public String  machineNickname;
        public String  handleName;
        public int     handle;
        public int     trustLevel;
        public boolean isLocalRequest;
        public int     diagEnabled;
        public String  request;
        public int     requestNumber;
        public String  user;
        public String  endpoint;
        public String  physicalInterfaceID;

        public RequestInfo(String stafInstanceUUID, String machine,
                           String machineNickname, String handleName,
                           int handle, int trustLevel,
                           boolean isLocalRequest, int diagEnabled,
                           String request, int requestNumber,
                           String user, String endpoint,
                           String physicalInterfaceID)
        {
            this.stafInstanceUUID = stafInstanceUUID;
            this.machine = machine;
            this.machineNickname = machineNickname;
            this.handleName = handleName;
            this.handle = handle;
            this.trustLevel = trustLevel;
            this.isLocalRequest = isLocalRequest;
            this.diagEnabled = diagEnabled;
            this.request = request;
            this.requestNumber = requestNumber;
            this.user = user;
            this.endpoint = endpoint;
            this.physicalInterfaceID = physicalInterfaceID;
        }
        
        public RequestInfo(String machine,int handle,String handleName,String request){
        	this.machine = machine;
        	this.handle = handle;
        	this.handleName = handleName;
        	this.request = request;
        }
        public RequestInfo(String machine,boolean isLocalRequest,String request){
        	this.machine = machine;
        	this.isLocalRequest = isLocalRequest;
        	this.request = request;
        }
        public RequestInfo(String request){
        	this.request = request;
        }
    }

}
