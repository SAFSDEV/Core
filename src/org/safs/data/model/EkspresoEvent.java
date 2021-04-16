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
 * @date 2018-03-02    (Lei Wang) Initial release.
 * @date 2018-09-03    (Lei Wang) Modified the fields name according to the Ekspreso Events specification.
 * @date 2018-09-10    (Lei Wang) Modified to extends the EkspresoEventObject from ekspreso-listener project.
 * @date 2018-10-22    (Lei Wang) Added two more constructors, modified field DATE_FORMAT_IN: as the dependency EkspresoEventObject has changed.
 * @date 2018-10-23    (Lei Wang) Added formatDateIn(): to format the date to a proper string accepted by Ekspreso Service.
 */
package org.safs.data.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.safs.data.model.ekspreso.Event;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sas.ekspreso.event_creator.model.EkspresoEventObject;

/**
 * @author Lei Wang
 */
@Entity
public class EkspresoEvent implements RestModel{

	/** 'SAFS Data Service' the default Ekspreso event_group for SAFS */
	public static final transient String EKSPRESO_EVENT_GROUP = "SAFS Data Service";

	/** 'ekspreso' the base path to access entity */
	public static final transient String REST_BASE_PATH = "ekspreso";

	/** "MM-dd-YYYY HH:MM:ss a z" copied from EkspresoEventObject, this format is needed by EkspresoEventObject.setEvent_datetime() */
	public static final java.lang.String DATE_FORMAT_IN = "MM-dd-YYYY HH:MM:ss a z";

	/** "YYYY-MM-dd'T'HH:MM:ss:SSS'Z'Z" copied from EkspresoEventObject, it is the internal format of this class field 'event_datetime' */
	public static final java.lang.String DATE_FORMAT_OUT = "YYYY-MM-dd'T'HH:MM:ss:SSS'Z'Z";

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	@Transient
	private EkspresoEventObject ekspreso_event;

	public EkspresoEvent(String event_type) {
		ekspreso_event = new EkspresoEventObject.Builder(EKSPRESO_EVENT_GROUP, event_type).build();
	}

	/**
	 * @param event_source String, Host/Machine/Source submitting event to Kafka.
	 * @param event_group String, Name of the app or process submitting the event. Such as "safs_data_service".
	 * @param event_user String, UserId of the app or process submitting the event.
	 * @param event_type String, Group-unique event type being submitting in the event. Such as "safs_test_start", or "safs_test_stop".
	 * @param event_datetime Date, Date/Time timestamp of the event occurrence on the source.
	 *                             This value may be different than the actual event occurrence that is found in the originating event_info.
	 */
	public EkspresoEvent(String event_source, String event_group, String event_user, String event_type, Date event_datetime) {
		ekspreso_event = new EkspresoEventObject.Builder(event_group, event_type)
				             .event_source(event_source)
				             .event_user(event_user).build();
	    ekspreso_event.setEvent_datetime(event_datetime);
	}

	/**
	 * @param event_source String, Host/Machine/Source submitting event to Kafka.
	 * @param event_group String, Name of the app or process submitting the event. Such as "safs_data_service".
	 * @param event_user String, UserId of the app or process submitting the event.
	 * @param event_type String, Group-unique event type being submitting in the event. Such as "safs_test_start", or "safs_test_stop".
	 * @param event_datetime Date, Date/Time timestamp of the event occurrence on the source.
	 *                             This value may be different than the actual event occurrence that is found in the originating event_info.
	 * @param failure_notification_recipients HashSet<String>, email addresses to notify in case of delivery failure. e.g. - ["user5@company.com", "user6@company.com"].
	 * @param event_expire_timestamp Date, Expiration timestamp for the event, a deadline by which this event should be processed.
	 */
	public EkspresoEvent(String event_source, String event_group, String event_user, String event_type,
			             Date event_datetime, HashSet<String> failure_notification_recipients,
			             Date event_expire_timestamp) {
		this(event_source, event_group, event_user, event_type, event_datetime);
		ekspreso_event.setFailure_notification_recipients(new ArrayList<String>(failure_notification_recipients));
		ekspreso_event.setEvent_expire_timestamp(event_expire_timestamp);
	}

	/**
	 * @param event_source String, Host/Machine/Source submitting event to Kafka.
	 * @param event_group String, Name of the app or process submitting the event. Such as "safs_data_service".
	 * @param event_user String, UserId of the app or process submitting the event.
	 * @param event_type String, Group-unique event type being submitting in the event. Such as "safs_test_start", or "safs_test_stop".
	 * @param event_datetime Date, Date/Time timestamp of the event occurrence on the source.
	 *                             This value may be different than the actual event occurrence that is found in the originating event_info.
	 * @param event_info Object, Items in the original event being submitted as an ekspreso_event, expressed in key/value pairs. Such as { key1: value1, key2: value2, ..., keyn: valuen}
	 */
	@SuppressWarnings("unchecked")
	public EkspresoEvent(String event_source, String event_group, String event_user, String event_type, Date event_datetime, Object event_info) {
		this(event_source, event_group, event_user, event_type, event_datetime);
		//Convert the event_info into a Map object, which is required by the EkspresoEventObject
		if(event_info instanceof Map){
			HashMap<String,Object> requiredEventInfo = new HashMap<String,Object>();
			requiredEventInfo.putAll((Map<? extends String, ? extends Object>)event_info);
			ekspreso_event.setEvent_info(requiredEventInfo);
		}else if(event_info instanceof Event){
			Gson gson = new GsonBuilder().create();
			ekspreso_event.setEvent_info(gson.fromJson(gson.toJson(event_info), HashMap.class));

		}else if(event_info!=null){
			//Consider it as a json string
			Gson gson = new GsonBuilder().create();
			ekspreso_event.setEvent_info(gson.fromJson(event_info.toString(), HashMap.class));
		}
	}

	/**
	 * @param event_source String, Host/Machine/Source submitting event to Kafka.
	 * @param event_group String, Name of the app or process submitting the event. Such as "safs_data_service".
	 * @param event_user String, UserId of the app or process submitting the event.
	 * @param event_type String, Group-unique event type being submitting in the event. Such as "safs_test_start", or "safs_test_stop".
	 * @param event_datetime Date, Date/Time timestamp of the event occurrence on the source.
	 *                             This value may be different than the actual event occurrence that is found in the originating event_info.
	 * @param event_info Object, Items in the original event being submitted as an ekspreso_event, expressed in key/value pairs. Such as { key1: value1, key2: value2, ..., keyn: valuen}
	 * @param failure_notification_recipients HashSet<String>, email addresses to notify in case of delivery failure. e.g. - ["user5@company.com", "user6@company.com"].
	 * @param event_expire_timestamp Date, Expiration timestamp for the event, a deadline by which this event should be processed.
	 */
	public EkspresoEvent(String event_source, String event_group, String event_user, String event_type, Date event_datetime, Object event_info,
				         HashSet<String> failure_notification_recipients, Date event_expire_timestamp) {
		this(event_source, event_group, event_user, event_type, event_datetime, event_info);
		ekspreso_event.setFailure_notification_recipients(new ArrayList<String>(failure_notification_recipients));
		ekspreso_event.setEvent_expire_timestamp(event_expire_timestamp);
	}

//	/**
//	 * Get the Date object from the internal 'event_datetime' which is in format {@link #DATE_FORMAT_OUT}.
//	 */
//	public Date getEventDatetime() throws ParseException{
//		return new SimpleDateFormat(EkspresoEvent.DATE_FORMAT_OUT).parse(getEventDatetime());
//	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public String getRestPath() {
		return REST_BASE_PATH;
	}

	public EkspresoEventObject getEkspreso_event() {
		return ekspreso_event;
	}

	public void setEkspreso_event(EkspresoEventObject ekspreso_event) {
		this.ekspreso_event = ekspreso_event;
	}

}
