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
package org.safs.auth;
/**
 * Logs for developers, not published to API DOC.
 *
 * History:
 * OCT 16, 2017 (Lei Wang) Removed hard-coded parameters and read information from class 'SSOAuth'.
 * OCT 24, 2017 (Lei Wang) Used a list of Parameter instead of a string to represent parameters used for authentication.
 */
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.Cookie;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.sync.CloseableHttpClient;
import org.apache.hc.client5.http.impl.sync.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.sync.HttpClientBuilder;
import org.apache.hc.client5.http.impl.sync.HttpClients;
import org.apache.hc.client5.http.methods.HttpGet;
import org.apache.hc.client5.http.methods.HttpPost;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.safs.IndependantLog;

/**
 * Converted from SasAuthentication.groovy provided by John W. Lewis.<br>
 * This class will authenticate with SSO server to get back auth-cookies, which can
 * be set to HttpClientBuilder, so that it can make request to the server demanding
 * the SSO (Single Sign On) authentication.<br>
 *
 */
public class SSOAuthentication {

	private final static long EXPIRE_INTERVAL_MSECS = 4 * 60 * 60 * 1000; // 4 hours

	private static SSOAuthentication singleton = null;

	private String ssoAuthenticationURL = null;
	private List<String> requiredCookies = null;
	private List<Parameter> parameters = null;

	private List<Cookie> cookieList = new ArrayList<Cookie>();
	private Date timeGotCookie = null;

	public static void main(String[] args)
	{
		System.out.println("==================================Start===========================");
		SSOAuthentication auth = SSOAuthentication.getInstance();
		HttpClientBuilder clientBuilder = HttpClients.custom();
		CloseableHttpClient client = null;

		try {
			//HttpClientBuilder
			auth.addCookies(clientBuilder);

			client = clientBuilder.build();

			//Lei: remove company private URL, this test will not work!
			HttpGet get = new HttpGet("An URL needs sso authentication.");

			CloseableHttpResponse response = client.execute(get);

			try {
				HttpEntity entity = response.getEntity();

				String responseString = EntityUtils.toString(entity);

				System.out.println(responseString);
			} finally {
				response.close();
			}
		} catch (Exception e) {
			System.err.println("Met "+e.toString());
		}finally {
			try {client.close();} catch (IOException e) {}
		}

		System.out.println("======================End=======================");
	}

	/**
	 * Don't use the constructor.   Call SasAuthentication.getInstance().
	 */
	private SSOAuthentication() {}

	public static synchronized SSOAuthentication getInstance() {
		if (singleton == null) {
			singleton = new SSOAuthentication();
		}
		return singleton;
	}

	/**
	 * @param ssoAuthenticationURL the ssoAuthenticationURL to set
	 */
	public void setSsoAuthenticationURL(String ssoAuthenticationURL) {
		this.ssoAuthenticationURL = ssoAuthenticationURL;
	}

	public SSOAuthentication init(SSOAuth ssoAuth) {
		this.ssoAuthenticationURL = ssoAuth.getSsoAuthenticationURL();
		this.requiredCookies = new ArrayList<String>(ssoAuth.getRequiredCookies());
		this.parameters = ssoAuth.getParameters();

		return this;
	}

	private boolean isCookieExpired() {
		if(timeGotCookie==null){
			//We haven't got any cookie, we need a new one, considered as expired.
			return true;
		}else{
			Date now = new Date();
			Date expiredTime = new Date(timeGotCookie.getTime() + EXPIRE_INTERVAL_MSECS);
			return now.after(expiredTime);
		}
	}

	/**
	 * Get the cookies after authenticating on SSO server.
	 */
	private void getNewCookies(){
		String debugmsg = SSOAuthentication.class.getSimpleName()+"getNewCookies(): ";

		if(ssoAuthenticationURL==null || ssoAuthenticationURL.isEmpty()){
			throw new IllegalArgumentException("ssoAuthenticationURL '"+ssoAuthenticationURL+"' is NOT valid!");
		}

//		/*
//		 * The SAS Certificate needs to be used or a problem occurs:
//		 * javax.net.ssl.SSLPeerUnverifiedException: peer not authenticated
//		 *
//		 * I was told by the web team to use the certificate SAS 333381_DigiCertCA.crt
//		 *
//		 * The certificate file is in the same package as this class, but
//		 * it is located in grails-app/conf to get it in the war so it
//		 * will end up on the classpath.
//		 */
//
//		// First, generate a certificate from the certificate file.
//		def inStream
//		def cert
//		try {
//			inStream = SasAuthentication.class.getResourceAsStream("333381_DigiCertCA.cer")
//			def cf = java.security.cert.CertificateFactory.getInstance("X.509")
//			cert = cf.generateCertificate(inStream)
//		} finally {
//			if (inStream != null) {
//				inStream.close()
//			}
//		}
//
//		// Now, create a keystore with the certificate.
//		def keystore = KeyStore.getInstance(KeyStore.getDefaultType())
//		keystore.load(null, null)
//		keystore.setCertificateEntry("sasCert", cert)
//
//		/*
//		 * The following code came partly from the Apache web site.  See
//		 * "Examples" under HttpClient 4.5.
//		 * (http://hc.apache.org/httpcomponents-client-ga/examples.html)
//		 *
//		 * There is a "Custom SSL context" example at
//		 * http://hc.apache.org/httpcomponents-client-ga/httpclient/examples/org/apache/http/examples/client/ClientCustomSSL.java
//		 *
//		 * It has the Apache license.
//		 */
//		// Trust own CA and all self-signed certs
//		def sslcontext = SSLContextBuilder.create()
//				.loadTrustMaterial(keystore,
//						new TrustSelfSignedStrategy())
//				.build();
//		// Allow TLSv1 protocol only
//		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
//				sslcontext,
//				[ "TLSv1" ] as String[],
//				null,
//				SSLConnectionSocketFactory.getDefaultHostnameVerifier());

		/*
		 * The following code regarding cookies is from
		 * http://stackoverflow.com/questions/19136736/how-to-handle-cookies-with-apache-httpclient-4-3
		 */
		RequestConfig globalConfig = RequestConfig.custom()
//			.setCookieSpec(CookieSpecs.BEST_MATCH)
			.build();
		BasicCookieStore cookieStore = new BasicCookieStore();
		HttpClientContext context = HttpClientContext.create();
		context.setCookieStore(cookieStore);

		CloseableHttpClient client = HttpClients.custom()
				.setDefaultRequestConfig(globalConfig)
				.setDefaultCookieStore(cookieStore)
//				.setSSLSocketFactory(sslsf)
				.build();

		try {
			HttpPost post = new HttpPost(ssoAuthenticationURL);

			List<BasicNameValuePair> data = new ArrayList<BasicNameValuePair>();
			for(Parameter param: parameters){
				//realm=xxx, IDToken1=user and IDToken2=password etc.
				data.add(new BasicNameValuePair(param.getName(), param.getValue()));
			}

//			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(data, "UTF-8");
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(data);
			post.setEntity(entity);

			CloseableHttpResponse response = client.execute(post);
			try {
				List<Cookie> cookies = cookieStore.getCookies();
				String name = null;
				cookieList.clear();

				IndependantLog.info(debugmsg+" trying get cookies '"+requiredCookies+"' from response after authentication on SSO server.");
				for(Cookie cookie:cookies){
					name = cookie.getName();
					for(String requiredCookie: requiredCookies){
						if(requiredCookie.equals(name)){
							cookieList.add(cookie);
							break;
						}
					}
				}

				IndependantLog.info(debugmsg+"Got Cookies: "+cookieList);
				if(requiredCookies.size()>cookieList.size()){
					throw new IllegalArgumentException("SSO Authentication unsuccessful: some required cookies are missing!");
				}

				timeGotCookie = new Date();
			} finally {
				response.close();
			}
		} catch (Exception e) {
			IndependantLog.error(debugmsg+"Met "+e.toString());
		} finally {
			try {client.close();} catch (IOException e) {}
		}
	}

	//a list of Cookie
	protected synchronized List<Cookie> getCookies() {
		if (isCookieExpired()) {
			getNewCookies();
		}
		return cookieList;
	}

	public synchronized void addCookies(HttpClientBuilder clientBuilder) {
		if (isCookieExpired()) {
			getNewCookies();
		}
		BasicCookieStore cookieStore = new BasicCookieStore();
		for(Cookie cookie: cookieList){
			cookieStore.addCookie(cookie);
		}
		clientBuilder.setDefaultCookieStore(cookieStore);
	}

}
