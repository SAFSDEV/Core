package org.safs.tools.data;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

/**
 * This class is intended to be a lightweight class with few additional SAFS dependencies.
 * <p>
 * The purpose is to provide a command-line driven ability to access and update JDBC databases.
 * <p>
 * JVM Arguments:<br>
 * <ul>
 * <p>
 * <li><b>-noupdate</b><br>
 * If present, no attempt to update the data should occur.<br>
 * Generally, this is useful for debugging connection attempts/failures.<br>
 * Same as JVM Option: <b>-Dsafs.jdbc.noupdate</b>
 * <p>
 * <li><b>-dataid</b> "mydata"<br>
 * The data id to be used in our connection queries and updates.<br>
 * Same as JVM Option: <b>-Dsafs.jdbc.dataid</b>="&lt;data_id>"
 * <p>
 * <li><b>-datapath</b> "/tst/tools/deploymentdetails/dev/shrdata"<br>
 * The data path to be associated with the data_id.<br>
 * Same as JVM Option: <b>-Dsafs.jdbc.datapath</b>="'&lt;data_path>'"
 * <p>
 * <li><b>-where</b> "ticketid = 1846"<br>
 * The where clause to filter the queries and updates.<br>
 * Same as JVM Option: <b>-Dsafs.jdbc.where</b>="&lt;where_clause>"<br>
 * Ex: -Dsafs.jdbc.where="ticketid = 1846"
 * <p>
 * <li><b>-dataset</b> "zdeployment"<br>
 * The dataset to be updated in the database.<br>
 * Same as JVM Option: <b>-Dsafs.jdbc.dataset</b>="&lt;dataset_id>"<br>
 * Ex: -Dsafs.jdbc.dataset="zdeployment"
 * <p>
 * <li><b>-field</b> "entminer"<br>
 * The field/column to be updated in the dataset.<br>
 * Same as JVM Option: <b>-Dsafs.jdbc.field</b>="&lt;field_id>"<br>
 * Ex: -Dsafs.jdbc.field="entminer"
 * <p>
 * <li><b>-value</b> "SUCCESS"<br>
 * The value to be updated into the field in the dataset.<br>
 * Same as JVM Option: <b>-Dsafs.jdbc.value</b>="&lt;value>"<br>
 * Ex: -Dsafs.jdbc.value="SUCCESS"
 * <p>
 * <li><b>-connection</b> "protocol://database-server-hostname:port"<br>
 * The value to be used for the JDBC database connection.<br>
 * Same as JVM Option: <b>-Dsafs.jdbc.connection</b>="&lt;connection_uri>"<br>
 * Ex: -Dsafs.jdbc.connection="protocol://database-server-hostname:port"
 * <p>
 * <li><b>-props</b> "librefs=mydata '/tst/tools/deploymentdetails/dev/shrdata';undoPolicyNone=True"<br>
 * Zero or more name=value pairs (separated by semi-colons) to be used during the connection request.<br>
 * This argument can appear more than once, if needed.<br>
 * Same as JVM Option: <b>-Dsafs.jdbc.connection.props</b>="&lt;name=value;name=value>"<br>
 * Ex: -Dsafs.jdbc.connection.props="librefs=mydata '/tst/tools/deploymentdetails/dev/shrdata';undoPolicyNone=True"
 * <p>
 * <li><b>-opsource</b> "Support:canagl:org.safs.tools.data.GenericJDBC.java"<br>
 * Text to be remotely logged identifying the source of this update request.<br>
 * Same as JVM Option: <b>-Dsafs.jdbc.opsource</b>="&lt;text identifying update source>"<br>
 * Ex: -Dsafs.jdbc.opsource="Support:canagl:org.safs.tools.data.GenericJDBC.java"
 * <p>
 * <li><b>-drivers</b> "com.sas.net.sharenet.ShareNetDriver"<br>
 * Zero or more jdbc driver class names to append to the jdbc.drivers System property, separated by semi-colons.<br>
 * Same as JVM Option: <b>-Dsafs.jdbc.drivers</b>="&lt;driverclass1;driverclass2>"<br>
 * Ex: -Dsafs.jdbc.drivers="com.sas.net.sharenet.ShareNetDriver"
 * </ul>
 * <p>
 * This class requires the JDBC Drivers be in the CLASSPATH already.<br>
 * For example: For SAS/Share, this generally means that both associated JAR files be in the
 * System CLASSPATH ( or passed in the command-line):
 * <p>
 * <ul>
 * <b>
 * <li>sas.core.jar
 * <li>sas.intrnet.javatools.jar
 * </b>
 * </ul>
 * <p>
 * @author canagl
 */
public class GenericJDBC {

	/** "mydata" */
	public static final String DEFAULT_SHRDATA_ID = "mydata";

	/** "'/tst/tools/deploymentdetails/shrdata'" */
	public static final String DEFAULT_PROD_DATA = "'/tst/tools/deploymentdetails/shrdata'";

	/** "'/tst/tools/deploymentdetails/dev/shrdata'" */
	public static final String DEFAULT_DEV_DATA = "'/tst/tools/deploymentdetails/dev/shrdata'";

	/** "com.sas.net.sharenet.ShareNetDriver" */
	public static final String DEFAULT_DRIVERS = "com.sas.net.sharenet.ShareNetDriver";

	/**
	 * @deprecated NO default connection will be provided. Please provide when running test.
	 * <p>
	 * parameter <b>-connection</b> "protocol://database-server-hostname:port"<br>
	 * OR<br>
	 * JVM Option: <b>-Dsafs.jdbc.connection</b>="&lt;connection_uri>"<br>
	 * Ex: -Dsafs.jdbc.connection="protocol://database-server-hostname:port"
	 * <p>
	 */
	@Deprecated
	public static final String DEFAULT_CONNECTION = "protocol://database-server-hostname:port";

	/** defaults to {@value #DEFAULT_CONNECTION} */
	protected String _connection = null;
	/** defaults to {@value #DEFAULT_DRIVERS} */
	protected String _drivers = DEFAULT_DRIVERS;
	/** defaults to {@value #DEFAULT_SHRDATA_ID} */
	protected String _shrdataid = DEFAULT_SHRDATA_ID;
	/** defaults to {@value #DEFAULT_PROD_DATA} */
	protected String _datapath = DEFAULT_PROD_DATA;
	/**
	 * defaults to null.
	 * Must be set by command-line arguments or API calls before a call to {@link #updateData()}. */
	protected String _dataset = null;
	/**
	 * defaults to null.
	 * Must be set by command-line arguments or API calls before a call to {@link #updateData()}. */
	protected String _field = null;
	/**
	 * defaults to null.
	 * Must be set by command-line arguments or API calls before a call to {@link #updateData()}. */
	protected String _value = null;
	/**
	 * defaults to null.
	 * Should be set by command-line arguments or API calls before a call to {@link #updateData()}.
	 * A generic value may be used internally if none is provided beforehand. */
	protected String _opsource = null;
	/**
	 * defaults to null.
	 * Must be set by command-line arguments or API calls before a call to {@link #updateData()}. */
	protected String _where = null;
	/**
	 * defaults to null.
	 * Should be set by command-line arguments or API calls before a call to {@link #updateData()},
	 * if needed.
	 */
	protected Properties _props = null;

	/**
	 * default: true.
	 * set to false and any call to updateData will be ignored. */
	protected boolean _doUpdate = true;

	public static final String PROP_KEY_LIBREFS        = "librefs";
	public static final String PROP_KEY_UNDOPOLICYNONE = "undoPolicyNone";
	public static final String PROP_KEY_JDBC_DRIVERS   = "jdbc.drivers";

	public static final String PROP_KEY_SAFS_JDBC_DRIVERS   ="safs.jdbc.drivers";

	public static final String PROP_KEY_SAFS_JDBC_NOUPDATE  ="safs.jdbc.noupdate";
	public static final String PROP_KEY_SAFS_JDBC_DATAID    ="safs.jdbc.dataid";
	public static final String PROP_KEY_SAFS_JDBC_DATAPATH  ="safs.jdbc.datapath";
	public static final String PROP_KEY_SAFS_JDBC_WHERE     ="safs.jdbc.where";
	public static final String PROP_KEY_SAFS_JDBC_DATASET   ="safs.jdbc.dataset";
	public static final String PROP_KEY_SAFS_JDBC_FIELD     ="safs.jdbc.field";
	public static final String PROP_KEY_SAFS_JDBC_VALUE     ="safs.jdbc.value";
	public static final String PROP_KEY_SAFS_JDBC_CONNECTION="safs.jdbc.connection";
	public static final String PROP_KEY_SAFS_JDBC_PROPS     ="safs.jdbc.connection.props";
	public static final String PROP_KEY_SAFS_JDBC_OPSOURCE  ="safs.jdbc.opsource";

	public static final String ARG_NOUPDATE  ="-noupdate";
	public static final String ARG_DRIVERS    ="-drivers";
	public static final String ARG_DATAID    ="-dataid";
	public static final String ARG_DATAPATH  ="-datapath";
	public static final String ARG_WHERE     ="-where";
	public static final String ARG_DATASET   ="-dataset";
	public static final String ARG_FIELD     ="-field";
	public static final String ARG_VALUE     ="-value";
	public static final String ARG_CONNECTION="-connection";
	public static final String ARG_PROPS     ="-props";
	public static final String ARG_OPSOURCE  ="-opsource";

	/** Currently logs to System.out.  Other options may be supported in the future. */
	public static void log(String message){
		System.out.println(message);
	}

	/**
	 * @param enabled -- Set true (default) to enable database updates.
	 * False to disable/bypass database updates.
	 */
	public void enableUpdate(boolean enabled) {_doUpdate = enabled; }
	/**
	 * @return true if database updates are enabled.  false otherwise.
	 */
	public boolean isUpdateEnabled() {return _doUpdate; }

	/**
	 * @return the current value of the JDBC connection string.
	 */
	public String getConnection() {
		return _connection;
	}

	/**
	 * @param _connection -- Set/Change the value of the JDBC connection string.
	 */
	public void setConnection(String _connection) {
		this._connection = _connection;
	}

	/**
	 * @return the current value of the JDBC drivers string.
	 */
	public String getDrivers() {
		return _drivers;
	}

	/**
	 * @param _drivers -- Set/Change the value of the JDBC drivers string.
	 */
	public void setDrivers(String _drivers) {
		this._drivers = _drivers;
	}

	/**
	 * @return the current value of the Share data id to be used with JDBC.
	 * This might only apply to SAS Connect JDBC data connections.
	 */
	public String getShrdataid() {
		return _shrdataid;
	}

	/**
	 * @param _shrdataid -- Set/Change the Share data id to be used with JDBC.
	 * This might only apply to SAS Connect JDBC data connections.
	 */
	public void setShrdataid(String _shrdataid) {
		this._shrdataid = _shrdataid;
	}

	/**
	 * @return the current value of the database path to be used with JDBC.
	 */
	public String getDatapath() {
		return _datapath;
	}

	/**
	 * @param _datapath -- Set/Change the database path to be used with JDBC.
	 */
	public void setDatapath(String _datapath) {
		this._datapath = _datapath;
	}

	/**
	 * @return the current value of the dataset in the JDBC database to use.
	 */
	public String getDataset() {
		return _dataset;
	}

	/**
	 * @param _dataset -- Set/Change the dataset in the database to be used with JDBC.
	 */
	public void setDataset(String _dataset) {
		this._dataset = _dataset;
	}

	/**
	 * @return the current field/column name that will be used in the JDBC dataset.
	 */
	public String getFieldName() {
		return _field;
	}

	/**
	 * @param _field -- Set/Change the field/column name that will be used in the JDBC dataset.
	 */
	public void setFieldName(String _field) {
		this._field = _field;
	}

	/**
	 * @return the current field value set to be used on the field in the JDBC dataset.
	 */
	public String getFieldValue() {
		return _value;
	}

	/**
	 * @param _value -- Set/Change the field value to be used on the field in the JDBC dataset.
	 */
	public void setFieldValue(String _value) {
		this._value = _value;
	}

	/**
	 * @return the current opSource (comment) setting that will be sent to the JDBC database.
	 */
	public String getOpsource() {
		return _opsource;
	}

	/**
	 * @param _opsource -- Set/Change the opSource (comment) that will be sent to the JDBC database.
	 */
	public void setOpsource(String _opsource) {
		this._opsource = _opsource;
	}

	/**
	 * @return the current where clause setting that will be used on the JDBC database.
	 * This does NOT normally include the "where " prefix.
	 */
	public String getWhere() {
		return _where;
	}

	/**
	 * @param _where -- Set/Change the where clause that will be used on the JDBC database.
	 * This setting should NOT include the "where " prefix.
	 */
	public void setWhere(String _where) {
		this._where = _where;
	}

	/**
	 * @return the current Properties used for JDBC database connection, if any.
	 * May return null.
	 */
	public Properties getProps() {
		return _props;
	}

	/**
	 * @param _props -- Set/Change the Properties used for JDBC database connection, if any.
	 * May be null.
	 */
	public void setProps(Properties _props) {
		this._props = _props;
	}

	/**
	 * Invokes {@link #updateData(String, Properties, String, String, String, String, String, String, String)}
	 * with all internal settings previously set or defaulted.
	 */
	public void updateData(){
		if(_connection==null){
			throw new RuntimeException("Please set the jdbc connection uri by \n"
					+ " parameter -connection \"protocol://database-server-hostname:port\" \n"
					+ " or \n"
					+ " JVM Option -Dsafs.jdbc.connection=\"protocol://database-server-hostname:port\" \n"
					+ " or \n"
					+ " this.setConnection(\"protocol://database-server-hostname:port\"); \n");
		}
		updateData(_connection,
				   _props,
				   _shrdataid,
				   _datapath,
				   _dataset,
				   _field,
				   _value,
				   _where,
				   _opsource
				   );
	}

	/**
	 * The primary routine used to update the database.
	 * <p>
	 * The typical update:
	 * <p><pre>
	     String update = "update "+ dataid +"."+ dataset
		          + " set "+ field +" = '"+ value +"'"    / * <-- single quotes critical * /
		          + " where "+ where
		          + " / * "+ opsource +" * /";

	   </pre>
	   <p>
	   Then verification:
	   <p><pre>
	     String query = "select * from "+ dataid +"."+ dataset
			      +" where "+ where
			      + "  / * "+ opsource +"  * /";
	 * </pre>
	 * <p>
	 * @param connectURI {@link #_connection}
	 * @param prop {@link #_props}
	 * @param dataid {@link #_shrdataid}
	 * @param datapath {@link #_datapath}
	 * @param dataset {@link #_dataset}
	 * @param field {@link #_field}
	 * @param value {@link #_value}
	 * @param where {@link #_where}
	 * @param opsource {@link #_opsource}
	 */
	public void updateData( String connectURI,
						    Properties prop,
						    String dataid,
						    String datapath,
						    String dataset,
						    String field,
						    String value,
						    String where,
						    String opsource){
	   int rc=0;
	   Connection conn = null;
	   Statement stmt = null;
	   ResultSet rs = null;
	   String temp = null;

	   if(! _doUpdate) {
	       log("INFO: UpdateData Bypassed, doUpdate=\"False\"");
		   return;
	   }

	   // Get the URI so we know if we are running in dev or prod
	   //String uri=request.getRequestURI();
	   //int x = uri.indexOf("/dev/");
	   try {

	     // Load the JDBC driver. CANAGL: not needed for JDBC 4.0 and later
	     // Class.forName("com.sas.net.sharenet.ShareNetDriver");
		 temp = System.getProperty(PROP_KEY_JDBC_DRIVERS);
		 if(temp == null) System.setProperty(PROP_KEY_JDBC_DRIVERS, DEFAULT_DRIVERS);

	     //SET UP OUR JDBC CONNECTION
		 if(connectURI == null){
			 connectURI = _connection;
		 }else{
			 _connection = connectURI;
		 }
		 log("INFO: Using connection at: "+ connectURI);

		 if(dataid == null){
			 dataid = _shrdataid;
		 }else{
			 _shrdataid = dataid;
		 }

		 if(datapath == null){
			 datapath = _datapath;
		 }
		 _datapath = datapath;

		 if(opsource == null){
			 opsource = "Support:CANAGL:"+ this.getClass().getName();
		 }
		 _opsource = opsource;

		 boolean abort = false;

		 if(dataset == null || dataset.length()==0){
	    	 log("ERROR, dataset \"-Dsafs.jdbc.dataset='adataset'\" or \"-dataset 'adataset'\" has not been specified.");
	    	 abort=true;
		 }
		 if(field == null || field.length()==0){
	    	 log("ERROR, field \"-Dsafs.jdbc.field='afield'\" or \"-field 'afield'\" has not been specified.");
	    	 abort=true;
		 }
		 if(value == null || value.length()==0){
	    	 log("ERROR, value \"-Dsafs.jdbc.value='avalue'\" or \"-value 'avalue'\" has not been specified.");
	    	 abort=true;
		 }

		 if(where == null || where.length()==0){
	    	 log("ERROR, where \"-Dsafs.jdbc.where='where clause'\" or \"-where 'where clause'\" has not been specified.");
	    	 abort=true;
		 }

		 if(abort){
	    	 log("ABORTED: Insufficient Arguments!");
	    	 return;
		 }

	     // First, we get some data;
		 if(prop == null){
		     prop = _props;
		     if (prop == null){
		    	 log("INFO: Creating default connection Properties...");
		    	 prop = new Properties();
				 prop.put(PROP_KEY_LIBREFS, dataid +" '"+ datapath +"'"); // <-- single quotes critical
				 prop.put(PROP_KEY_UNDOPOLICYNONE,"True");
		     }
		 }
		 log("INFO: connection props: "+ prop.toString());
	     conn = java.sql.DriverManager.getConnection("jdbc:"+ connectURI, prop);
	     //conn = java.sql.DriverManager.getConnection("jdbc:protocol://database-server-hostname:port",prop);
	     stmt = conn.createStatement();

	     String update = "update "+ dataid +"."+ dataset
		          + " set "+ field +" = '"+ value +"'"    /* <-- single quotes critical */
		          + " where "+ where
		          + " /* "+ opsource +" */";

	     log("INFO: Update Attempt: "+ update);

	     rc = stmt.executeUpdate(update);

	     //Our update was successful so continue
	     if (rc <= 1 ) {
	    	String query = "select * from "+ dataid +"."+ dataset
			           +" where "+ where
					   + "  /* "+ opsource +"  */";
	    	log("INFO: Update status: "+ query);
			//Query to see if our update worked
			rs = stmt.executeQuery(query);
			while(rs.next()) {
			  temp=null;
			  try{temp = rs.getString(field).trim();}
			  catch(Exception x){}
			  if(value.equals(temp)){ log("RESULT:  OK, Field: "+ field +"="+ temp ); }
			  else                   { log("RESULT: ERR, Field: "+ field +"="+ temp +", Expected "+ value ); }
			}
	     }else{
	    	 log("ERROR RC: "+ rc );
	     }

	     if (rs   != null) { rs.close(); }
	     if (stmt != null) { stmt.close(); }
	     if (conn != null) { conn.close(); }
	   }

	   // Detail the exception if we saw one.
	   catch (Exception e) {
	      log("ERROR: An error occurred when attempting to update the data: \n" + e );
	   }

	   // IMPORTANT:  Always close all Connection, Statement, and ResultSet objects
	   // in a finally clause!
	   finally {
	      try {
	         if (rs   != null) { rs.close(); }
	         if (stmt != null) { stmt.close(); }
	         if (conn != null) { conn.close(); }
	      }
	      catch (java.sql.SQLException e) {
	         log("ERROR: An error occurred when attempting to close data connections: \n" + e );
	      }
	   }
	}

	/** Used internally to return a valid System Property value, or null. */
	protected String getSystemProperty(String pname){
		try{
			return System.getProperty(pname);
		}catch(Throwable t){}
		return null;
	}

	/**
	 * @param propsarg -- command-line -props value or JVM Option to be parsed and placed
	 * into {@link #_props}.
	 */
	public void processPropsArg(String propsarg){
		if(propsarg == null) return;
		final String semi = ";";
		final String assign = "=";
		try{
			String[] pairs = propsarg.split(semi);
			for(int i=0; i < pairs.length;i++){
				try{
					String[] pair = pairs[i].split(assign);
					String key = pair[0];
					String val = pair[1];
					if(_props == null) _props = new Properties();
					_props.setProperty(key, val);
				}catch(Exception x){}
			}
		}catch(Exception x){}
	}

	/**
	 * Create/Append a new JDBC Drivers value to any existing JDBC Drivers value stored in
	 * System Properties.
	 * @param driversarg -- one or more JDBC Driver class names separated by semi-colons as
	 * usually retrieved from the command-line arguments.
	 */
	protected void processDriversArg(String driversarg){
		if(driversarg == null) return;
		String val = getSystemProperty( PROP_KEY_JDBC_DRIVERS );
		val = (val == null) ? driversarg: val.concat(";"+ driversarg);
		System.setProperty(PROP_KEY_JDBC_DRIVERS, val);
	}

	/** (Re)Set any arguments passed in as JVM -D Options. */
	public void processJVMOptions(){
		if(getSystemProperty( PROP_KEY_SAFS_JDBC_NOUPDATE) != null) enableUpdate(false);
		if(getSystemProperty( PROP_KEY_SAFS_JDBC_CONNECTION) != null) setConnection(getSystemProperty( PROP_KEY_SAFS_JDBC_CONNECTION ));
		if(getSystemProperty( PROP_KEY_SAFS_JDBC_DATAID) != null) setShrdataid(getSystemProperty( PROP_KEY_SAFS_JDBC_DATAID));
		if(getSystemProperty( PROP_KEY_SAFS_JDBC_DATAPATH) != null) setDatapath(getSystemProperty( PROP_KEY_SAFS_JDBC_DATAPATH));
		if(getSystemProperty( PROP_KEY_SAFS_JDBC_DATASET) != null) setDataset(getSystemProperty( PROP_KEY_SAFS_JDBC_DATASET));
		if(getSystemProperty( PROP_KEY_SAFS_JDBC_FIELD) != null) setFieldName(getSystemProperty( PROP_KEY_SAFS_JDBC_FIELD));
		if(getSystemProperty( PROP_KEY_SAFS_JDBC_VALUE) != null) setFieldValue(getSystemProperty( PROP_KEY_SAFS_JDBC_VALUE));
		if(getSystemProperty( PROP_KEY_SAFS_JDBC_WHERE) != null) setWhere(getSystemProperty( PROP_KEY_SAFS_JDBC_WHERE));
		if(getSystemProperty( PROP_KEY_SAFS_JDBC_OPSOURCE) != null) setOpsource(getSystemProperty( PROP_KEY_SAFS_JDBC_OPSOURCE));

		processPropsArg  ( getSystemProperty( PROP_KEY_SAFS_JDBC_PROPS   ) );
		processDriversArg( getSystemProperty( PROP_KEY_SAFS_JDBC_DRIVERS ) );
	}

	/** String[] args will override any processJVMOptions previously set--
	 * assuming JVM Options are processed BEFORE these args. */
	public void processArgs(String[] args){
		if(args == null) args = new String[0];
		String arg = null;
		for(int i=0;i < args.length;i++){
			try{
				arg = args[i];
				if(arg.length() == 0) continue;
				if(ARG_NOUPDATE.equalsIgnoreCase(arg))   { enableUpdate(false);     continue; }
				if(ARG_CONNECTION.equalsIgnoreCase(arg)) { setConnection(args[++i]); continue; }
				if(ARG_DATAID.equalsIgnoreCase(arg))     { setShrdataid(args[++i]); continue; }
				if(ARG_DATAPATH.equalsIgnoreCase(arg))   { setDatapath(args[++i]); continue; }
				if(ARG_DATASET.equalsIgnoreCase(arg))    { setDataset(args[++i]); continue; }
				if(ARG_FIELD.equalsIgnoreCase(arg))      { setFieldName(args[++i]); continue; }
				if(ARG_VALUE.equalsIgnoreCase(arg))      { setFieldValue(args[++i]); continue; }
				if(ARG_WHERE.equalsIgnoreCase(arg))      { setWhere(args[++i]); continue; }
				if(ARG_OPSOURCE.equalsIgnoreCase(arg))   { setOpsource(args[++i]); continue; }

				if(ARG_PROPS.equalsIgnoreCase(arg))      { processPropsArg(   args[++i])   ; continue; }
				if(ARG_DRIVERS.equalsIgnoreCase(arg))    { processDriversArg( args[++i])   ; continue; }
			}catch(Exception x){}
		}
	}

	/**
	 * primarily a testing/debug entry point.
	 * <p>
	 * command-line execution performs:
	 * <p>
	 * <ul><pre>
		GenericJDBC process = new GenericJDBC();
		process.processJVMOptions();
		process.processArgs(args);
		process.updateData();
	 * </pre></ul>
	 * @param args
	 */
	public static void main(String[] args) {
		GenericJDBC process = new GenericJDBC();
		process.processJVMOptions();
		process.processArgs(args);
		process.updateData();
	}

}
