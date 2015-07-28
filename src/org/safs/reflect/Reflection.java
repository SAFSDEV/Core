/** 
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs.reflect;

import java.lang.reflect.*;
import java.util.Vector;

import org.safs.jvmagent.NoSuchPropertyException;
import org.safs.jvmagent.SAFSActionUnsupportedRuntimeException;
import org.safs.jvmagent.SAFSInvalidActionArgumentRuntimeException;
import org.safs.jvmagent.SAFSObjectNotFoundRuntimeException;

/**
 * Utility class used to determine runtime Fields, Methods and other information.
 * 
 * @author Carl Nagle
 * @since Jun 3, 2005
 */
public class Reflection {
	
	/**
	 * "()" to signify a reflected indexed property.
	 */
	public static final String INDEX_MARKS = "()";

	/**
	 * "(" to signify a reflected indexed property.
	 */
	public static final String LEFT_INDEX_MARK = "(";

	/**
	 * ")" to signify a reflected indexed property.
	 */
	public static final String RIGHT_INDEX_MARK = ")";


	/**
	 * List of acceptable Field and Method property return types.<br/>
	 * Only Fields and Methods that return primitive classes and these 
	 * classes will be considered as potentially valid properties of the object.<br/>
	 * At some point we may want this to be an external text file.
	 */
	private static final String[] PROPERTY_CLASSES = { "java.lang.",
		                                                 "java.awt.Color",
		                                                 "java.awt.Cursor",
		                                                 "java.awt.Dimension",
		                                                 "java.awt.Font",
		                                                 "java.awt.Insets",
		                                                 "java.awt.Point",
		                                                 "java.awt.Rectangle"
		                                               };
	
	/**
	 * We only want to deal with primitives and a select number of other classes 
	 * whose toString() method returns a useful value.
	 * @param Class rctype -- the return type of the reflection Field or Method
	 * @return true if the return type is one we want to deal with, false otherwise.
	 * @see #PROPERTY_CLASSES
	 */
	private static boolean acceptPropertyReturnType( Class rctype ) {
		// skip array return types
		if ( rctype.isArray() ) return false;
		if ( rctype.isPrimitive() ) return true;
		if ( rctype.equals( java.lang.Void.class )) return false;
		String clazz = rctype.getName();
		for( int i=0;i<PROPERTY_CLASSES.length;i++ ){
			if ( clazz.equals( PROPERTY_CLASSES[i] )) return true;
			if ( clazz.startsWith( PROPERTY_CLASSES[i] )) return true;
		}
		return false;
	}
	
	
	/**
	 * Convert the Object value returned from a Field or Method call to a String 
	 * value based on the Class rctype.
	 * @param Object value -- the value returned from the Field or Method call
	 * @param Class rctype -- the return type of the reflection Field or Method
	 * @return String valueOf the value after cast to the appropriate rctype.
	 * @throws SAFSInvalidActionArgumentRuntimeException if rctype is not acceptable
	 * @see #acceptPropertyReturnType(Class)
	 * @see #PROPERTY_CLASSES
	 */
	private static String convertReflectedPropertyValue( Object value, Class rctype ) {
		if (! acceptPropertyReturnType( rctype ) )
			throw new SAFSInvalidActionArgumentRuntimeException(rctype.getName());	
		if ( rctype.isPrimitive() ){
			if (value instanceof String) return (String) value;
			if (value instanceof Character) return String.valueOf(((Character)value).charValue());
			if (value instanceof Integer) return String.valueOf(((Integer)value).intValue());
			if (value instanceof Long) return String.valueOf(((Long)value).longValue());
			if (value instanceof Short) return String.valueOf(((Short)value).shortValue());
			if (value instanceof Float) return String.valueOf(((Float)value).floatValue());
			if (value instanceof Double) return String.valueOf(((Double)value).doubleValue());
			if (value instanceof Byte) return String.valueOf(((Byte)value).byteValue());
		}
		return String.valueOf(value);
	}

	
	/**
	 * Deduce available property names at runtime via reflection.
	 * @param object -- the actual object or component to be evaluated for properties.
	 * @return String[] of recognized property names
	 * @see org.safs.jvmagent.LocalAgent#getPropertyNames(Object)
	 */
	public static String[] reflectPropertyNames(Object object) {
		Vector names = new Vector();
		
		// PUBLIC FIELDS
		try{
			Field[] fields = object.getClass().getFields();
			for ( int i=0; i< fields.length; i++ ) {
				Class rctype = fields[i].getType();
				if (acceptPropertyReturnType( rctype ) ) names.add( fields[i].getName() );
			}
		}
		catch(SecurityException sx){;}		

		// PUBLIC METHODS
		try{
			Method[] methods = object.getClass().getMethods();
			String method;
			Class[] params;
			Class rctype;
			Class arg;
			for ( int i=0; i< methods.length; i++ ) {
				method = methods[i].getName();
				
				// only accept isABC getABC and hasABC methods
				try{
					if (( method.startsWith( "is" ))&&(method.length() > 2)) {
						method = method.substring(2,3).toLowerCase().concat(method.substring(3));
					}else
					if (( method.startsWith( "get" ))&&(method.length() > 3)) {
						method = method.substring(3,4).toLowerCase().concat(method.substring(4));
					}else
					if (( method.startsWith( "has" ))&&(method.length() > 3)) {
						method = method.substring(3,4).toLowerCase().concat(method.substring(4));
					}else continue;
				}
				catch(Exception x){ continue; }
				
				params = methods[i].getParameterTypes();
				rctype = methods[i].getReturnType();
				
				// only accept methods with 0 or 1 args (int or long for indices)
				if ( params.length == 0 ) {
					if (acceptPropertyReturnType( rctype ) ) names.add( method );
				}else
				if ( params.length == 1 ) {
					arg = params[0];
					
					// only accept int and long args for indices
					if ( arg.isPrimitive() ) {
						if (( arg.equals( Integer.TYPE) )||( arg.equals( Long.TYPE ))) {					
							if (acceptPropertyReturnType( rctype ) ) names.add( method + INDEX_MARKS);
						}
					}
				}
			}
		}
		catch(SecurityException sx){;}
		
		return (String[]) names.toArray( new String[0] );
	}

	/**
	 * Called internally from reflectProperty methods.
	 * @param object - object being evaluated for a property value.
	 * @param property - the name of the property being evaluated.
	 * @throws SAFSInvalidActionArgumentRuntimeException if a problem exists.
	 */
	private static PropertyInfo parseProperty (Object object, String property ){
		PropertyInfo pinfo = new PropertyInfo( object, property );
		pinfo.setTrimmedProperty( property.trim() );
		
		pinfo.setBindex( pinfo.getTrimmedProperty().indexOf(LEFT_INDEX_MARK));
		pinfo.setEindex( pinfo.getTrimmedProperty().indexOf(RIGHT_INDEX_MARK));
		String sindex = null;
		int iindex = -1;
		long lindex = -1;

		if ( pinfo.getBindex() >= 0 ) {
			if (pinfo.getEindex() < pinfo.getBindex() ) {
				throw new SAFSInvalidActionArgumentRuntimeException(property);				
			}
			// handles the extract and validate the index information 
			pinfo.setSindex( pinfo.getTrimmedProperty().substring(pinfo.getBindex() + 1, pinfo.getEindex() ).trim());					
			// how to handle any (invalid index) exception thrown by setSindex?
			// currently passing them along
		}else if ( pinfo.getEindex() > -1 )
			throw new SAFSInvalidActionArgumentRuntimeException(property);				

		if (pinfo.isIndexed())
			pinfo.setTrimmedProperty(pinfo.getTrimmedProperty().substring(0, pinfo.getBindex()).trim());

		return pinfo;
	}
	
	
	/**
	 * Normally called internally from reflectProperty.
	 * @param object - object being evaluated for a property (Field) value.
	 * @param property - the name of the property being evaluated.
	 * @throws NoSuchFieldException( property )
	 * @throws SAFSInvalidActionArgumentRuntimeException( property );	
	 * @see #reflectProperty(Object,String)
	 */
	public static String reflectFieldProperty( Object object, String propertyname ) 
												throws NoSuchFieldException {
		if (object == null)
			throw new SAFSObjectNotFoundRuntimeException("Invalid Object");
		if (propertyname == null)
			throw new SAFSInvalidActionArgumentRuntimeException(propertyname);
		String property = propertyname.trim();
		try{
			// throws NoSuchFieldException if necessary
			Field field = object.getClass().getField( property ); 
			try{
				Class rctype = field.getType();
				Object rcvalue = field.get(object);				
				// convert to String based on rctype and return
				return convertReflectedPropertyValue( rcvalue, rctype );
			}
			catch(Exception x){
				throw new SAFSInvalidActionArgumentRuntimeException(property);								
			}			
		}
		catch(SecurityException sx){
			throw new SAFSInvalidActionArgumentRuntimeException(property);				
		}
	}

	/**
	 * Called internally from reflectMethodProperty
	 * @throws NoSuchMethodException
	 * @see #reflectMethodProperty(Object,String)
	 */
	private static Method findPropertyMethod( PropertyInfo pinfo, Class[] cindex )
	                                            throws NoSuchMethodException {
		Class clazz = pinfo.getObject().getClass();
		try{ 
			return clazz.getMethod( "get"+ pinfo.getUCTrimmedProperty(), cindex );
			}catch(NoSuchMethodException nsm1){ try{
			return clazz.getMethod( "get"+ pinfo.getTrimmedProperty(), cindex );							
			}catch(NoSuchMethodException nsm2){ try{
			return clazz.getMethod( "get"+ pinfo.getLCTrimmedProperty(), cindex );							
			}catch(NoSuchMethodException nsm3){ try{
			return clazz.getMethod( "is"+ pinfo.getUCTrimmedProperty(), cindex );							
			}catch(NoSuchMethodException nsm4){ try{
			return clazz.getMethod( "is"+ pinfo.getTrimmedProperty(), cindex );							
			}catch(NoSuchMethodException nsm5){ try{
			return clazz.getMethod( "is"+ pinfo.getLCTrimmedProperty(), cindex );							
			}catch(NoSuchMethodException nsm6){ try{
			return clazz.getMethod( "has"+ pinfo.getUCTrimmedProperty(), cindex );							
			}catch(NoSuchMethodException nsm7){ try{
			return clazz.getMethod( "has"+ pinfo.getTrimmedProperty(), cindex );							
			}catch(NoSuchMethodException nsm8){ try{
			return clazz.getMethod( "has"+ pinfo.getLCTrimmedProperty(), cindex );							
			}catch(NoSuchMethodException nsm9){ 
			return clazz.getMethod( pinfo.getTrimmedProperty(), cindex );							
			}}}}}}}}
		}
	}
	
	/**
	 * Normally called internally from reflectProperty.
	 * @param object - object being evaluated for a property (Method) value.
	 * @param property - the name of the property being evaluated.
	 * @throws NoSuchMethodException( property )
	 * @throws SAFSInvalidActionArgumentRuntimeException( property );	
	 * @see #reflectProperty(Object,String)
	 */
	public static String reflectMethodProperty( Object object, String property ) 
												 throws NoSuchMethodException {

		PropertyInfo pinfo = parseProperty( object, property );
		
		Method method = null;
		Class clazz = object.getClass();
		Object[] args = new Object[0];
		Class[] cindex = new Class[0];
		try{
			if (pinfo.isIndexed() ){
				// MUST support long arg
				if ( pinfo.isLong() ) { 						
					cindex = new Class[] { Long.TYPE };						
					method = findPropertyMethod( pinfo, cindex );
					args = new Object[] { new Long(pinfo.getLindex()) };
				}else{						
					// try int first
					try{ 
						cindex = new Class[] { Integer.TYPE };
						method = findPropertyMethod( pinfo, cindex );
						args = new Object[] { new Integer( pinfo.getIindex()) };
					}
					// try long last
					catch( NoSuchMethodException nsmx ) {
						cindex = new Class[] { Long.TYPE };						
						method = findPropertyMethod( pinfo, cindex );
						args = new Object[] { new Long(pinfo.getLindex()) };
						pinfo.setLong(true);
					}
				}
			// not an indexed property
			}else{
				method = findPropertyMethod( pinfo, cindex); 
			}
			
			try{
				Class rctype = method.getReturnType();
				Object rcvalue = method.invoke(object, args);				
				// convert to String based on rctype and return
				return convertReflectedPropertyValue( rcvalue, rctype );
			}
			catch(Exception x){
				throw new SAFSInvalidActionArgumentRuntimeException(property);								
			}			
		}
		catch(SecurityException sx){
			throw new SAFSInvalidActionArgumentRuntimeException(property);				
		}
	}


	/**
	 * @param object - object being evaluated for a property (Field or Method) value.
	 * @param property - the name of the property being evaluated.
	 * @throws NoSuchPropertyException( property )
	 * @throws SAFSInvalidActionArgumentRuntimeException( property );	
	 */
	public static String reflectProperty( Object object, String property ) 
											throws NoSuchPropertyException{
		if (object == null)
			throw new SAFSObjectNotFoundRuntimeException("Invalid Object");
		if (property == null)
			throw new SAFSInvalidActionArgumentRuntimeException(property);
		
		try{ return reflectFieldProperty( object, property ); }
		catch(NoSuchFieldException nsf){;}

		try{ return reflectMethodProperty( object, property ); }
		catch(NoSuchMethodException nsf){
			throw new NoSuchPropertyException(property);
		}
	}	
}
