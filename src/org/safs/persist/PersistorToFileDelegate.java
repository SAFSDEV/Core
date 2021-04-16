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
/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * OCT 24, 2017    (Lei Wang) Initial release.
 */
package org.safs.persist;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.safs.SAFSException;
import org.safs.tools.RuntimeDataInterface;

/**
 * Write Persistable object to a file, but it will delegate the job to a sub-class of {@link PersistorToString}
 * to get back the string format of the Persistable object, then it will write that string into the file.<br>
 *
 * @author Lei Wang
 *
 */
public abstract class PersistorToFileDelegate extends PersistorToFile{
	/**
	 * The delegate string persistor.
	 */
	PersistorToString delegatePersistor = null;

	/**
	 * @param runtime
	 * @param filename
	 */
	public PersistorToFileDelegate(RuntimeDataInterface runtime, String filename){
		super(runtime, filename);
		//The sub-class needs to instantiate the a concrete PersistorToString
		instantiateDelegatePersitor();
	}

	/**
	 * Instantiate a concrete PersistorToString.
	 * It will be called in the constructor {@link #PersistorToFileDelegate(RuntimeDataInterface, String)}.
	 */
	protected abstract void instantiateDelegatePersitor();

	@Override
	public Persistable unpickle(Map<String/*className*/, List<String>/*field-names*/> ignoredFields) throws SAFSException{
		//We should assign the 'ignoredFields' to the delegated persistor before calling super.unpickle
		delegatePersistor.ignoredFieldsForUnpickle = ignoredFields;
		return super.unpickle(ignoredFields);
	}

	@Override
	protected void beforeUnpickle()  throws SAFSException, IOException{
		super.beforeUnpickle();

		delegatePersistor.setStringFormatReader(reader);
		delegatePersistor.beforeUnpickle();
	}

	@Override
	protected Persistable doUnpickle()  throws SAFSException, IOException{
		return delegatePersistor.doUnpickle();
	}

	@Override
	protected final void write(Persistable persistable)  throws SAFSException, IOException{
		writer.write(delegatePersistor.parse(persistable, true));
	}

}
