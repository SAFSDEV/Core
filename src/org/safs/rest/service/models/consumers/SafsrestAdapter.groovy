package org.safs.rest.service.models.consumers

import org.safs.rest.service.commands.CommandInvoker
import org.safs.rest.service.commands.curl.CurlInvoker
import org.safs.rest.service.commands.curl.Response
import org.safs.rest.service.models.providers.SafsRestPropertyProvider
import org.safs.rest.service.models.providers.authentication.TokenProviderEntrypoints
import org.safs.rest.service.commands.curl.CurlCommand

import java.util.Map;
import org.apache.hc.core5.http.HttpException

import org.apache.hc.core5.testing.framework.ClientPOJOAdapter

/**
 *
 * <p>This adapter expects a request to be made up of POJOs such as Maps and Lists.  In Groovy
 * the request could be expressed like this:</p>
 *
 * <pre>
 *
 * def request = [
 *                   path    : "a/path",
 *                   method  : "GET",
 *                   query   : [
 *                                parm1 : "1",
 *                                parm2 : "2",
 *                             ]
 *                   headers : [
 *                                header1 : "stuff",
 *                                header2 : "more_stuff",
 *                             ]
 *                   contentType : "application/json",
 *                   body        : '{"location" : "home" }',
 *                ]
 * </pre>
 *
 * <p>The adapter will translate this request into SAFSREST calls.</p>
 *
 * <p>The response is then returned with POJOs with this structure:</p>
 *
 * <pre>
 *
 * def response = [
 *                    status      : 200,
 *                    headers     : [
 *                                      header1 : "response_stuff",
 *                                  ]
 *                    contentType : "application/json",
 *                    body        : '{"location" : "work" }',
 *                ]
 * </pre>
 */
class SafsrestAdapter extends ClientPOJOAdapter {
    public static final USERID = "userid"
    public static final PASSWORD = "password"
    
    public trustedUserid
    public trustedPassword
    public tokenProviderRootUrl
    public tokenProviderServiceName
    public tokenProviderAuthTokenResource
    
    /**
     * Use a bash or CMD shell to run curl.
     */
    def useScript = false
    
    /**
     * Use Java VM's execution facilities to run curl.
     */
    def execCurlFromJVM = false

    /**
     * Execute an HTTP request.
     *
     * @param defaultURI   the URI used by default.  The path in the request is
     *                     usually appended to it.
     * @param request      the request as specified above.
     *
     * @return the response to the request as specified above.
     *
     * @throws Exception in case of a problem
     */
    @Override
    public Map<String, Object> execute(
            String defaultURI,
            Map<String, Object> request)
    throws Exception {

        request = modifyRequest(request)
        
        SafsRestPropertyProvider propertyProvider = new SafsRestPropertyProvider()
        
        if (tokenProviderServiceName != null) {
            propertyProvider.tokenProviderServiceName = tokenProviderServiceName
        }
        if (tokenProviderAuthTokenResource != null) {
            propertyProvider.tokenProviderAuthTokenResource = tokenProviderAuthTokenResource
        }

        def userid = request.userid
        def password = request.password
        
        if (userid) {
            propertyProvider.userName = userid
        }
        if (trustedUserid) {
            propertyProvider.trustedUser = trustedUserid
        }

        /*
         * Inject a CommandInvoker only if needed.  The SAFSREST user does not normally inject one.
         * SAFSREST normally has useScript == false and execCurlFromJVM == false
         */
        def commandInvoker = useScript || execCurlFromJVM ? new CommandInvoker(
                                                                            useScript:useScript,
                                                                            execCurlFromJVM:execCurlFromJVM,
                                                                ) : null
                                                            
        CurlInvoker curlInvoker = new CurlInvoker(commandInvoker:commandInvoker)

        RestConsumer consumer = new RestConsumer(curlInvoker: curlInvoker, safsrestProperties: propertyProvider)

        // create the entrypoint by appending the path to the defaultURI
        def entrypoint = defaultURI + (request.path.startsWith("/") ? request.path[1..-1] : request.path)

        // add the query parameters to the entrypoint
        if (request.query) {
            entrypoint = entrypoint + "?"
            request.query.each { entry ->
                if (! entrypoint.endsWith("?")) {
                    entrypoint += "&"
                }
                entrypoint = entrypoint + entry.key + "=" + entry.value
            }
        }

        // later versions of curl or maybe HttpServer seem to default to HTTP 2.0.
        // Specify 1.1 specifically.
        def optionList = [] //["--http1.1"] TODO: this option is not available on the Jenkins server.

        // add headers to the optionList
        if (request.headers) {
            request.headers.each { entry -> optionList << /${CurlCommand.HEADER_OPTION}${entry.key}${CurlCommand.HEADER_FIELD_SEPARATOR}${entry.value}/ }
        }
        
        def uri = new URI(entrypoint)
        def rootUrl = "${uri.scheme}://${uri.host}:${uri.port}"
        propertyProvider.rootUrl = rootUrl
        
        if (userid && password) {
            // token has to be acquired after the rootUrl is set.
            acquireAuthToken(propertyProvider, password)
        }

        def safsrestMap = [
            entrypoint:entrypoint,
            options:optionList,
        ]

        if (request.body) {
            safsrestMap.requestBody = request.body
        }

        safsrestMap.contentType = request.contentType

        // call the appropriate method: get(), post(), etc.
        def response = consumer."${request.method.toLowerCase()}"(safsrestMap)

        // TODO: body is "" for head
        def body = request.method != 'HEAD' ? response.body : null

        /*
         * SAFSREST splits the contentType between contentType and charset, but some of the
         * tests are looking for the charset to be in the contentType.  So, return
         * contentTypeWithoutCharset and a contentType that has the charset.
         */
        def contentTypeWithoutCharset = response.contentType
        def contentType = contentTypeWithoutCharset + "; charset=${response.charset.toString().toLowerCase()}" as String

        def status = response.httpStatus.value()

        [
            contentType:contentType,
            contentTypeWithoutCharset:contentTypeWithoutCharset,
            charset:response.charset as String,
            body:body,
            status:status,
            headers:response.headers,
        ]
    }

    private acquireAuthToken(propertyProvider, password) {
        def savedRootUrl = propertyProvider.rootUrl
        try {
            if (tokenProviderRootUrl) {
                // if there is a different rootUrl for the authentication service, use it now.
                propertyProvider.rootUrl = tokenProviderRootUrl
            }
            propertyProvider.acquireAuthToken(password, trustedPassword)
        } finally {
            // restore the rootUrl
            propertyProvider.rootUrl = savedRootUrl
        }
    }

    /**
     * <p>Modify the request.</p>
     *
     * <p>In a testing context, a testing framework can call this method to allow
     * the adapter to change the request.  The request is then given to a
     * special request handler of the in-process HttpServer which will later check
     * an actual HTTP request against what is expected.</p>
     *
     * <p>In a production context, this is called by the execute method (if at all).</p>
     *
     * @param request the request as specified above.
     * @return the same request or a modification of it.
     */
    @Override
    public Map<String, Object> modifyRequest(Map<String, Object> request) {
        // create a closure to remove the body and contentType.
        def removeBody = {
            request.remove('body')
            request.remove('contentType')
        }

        /*
         * SAFSREST will not allow the setting of a body in these cases.
         */
        switch (request.method) {
            case 'HEAD':
            /* This was in stderr:
             *
             * Warning: You can only select one HTTP request method! You asked for both POST (-d, --data) and HEAD (-I, --head).
             */
                removeBody()
        }

        /*
         * SAFSREST will not allow the setting of a body in these cases.
         */
        switch (request.method) {
            case 'GET':
            /*
             * The curl option to send the input stream as the body is not set.
             */
                removeBody()
        }

        /*
         * If headers are set, strip any whitespace from them (except for Authorization).
         * SAFSREST does not work if headers have spaces in them.
         */
//        if (request.headers) {
//            def headers = [:]
//            request.headers.each { name, value ->
//                if (name == "Authorization") {
//                    headers.put(name, value)
//                    return
//                }
//                // strip whitespace:  //TODO: fix safsrest so this is not necessary
//                value = value.replaceAll("\\s+","")
//                headers.put(name, value)
//            }
//            request.headers = headers
//        }
        request
    }

    /**
     * Name of the HTTP Client that this adapter uses.
     *
     * @return name of the HTTP Client.
     */
    @Override
    public String getClientName() {
        "SAFSREST${useScript ? '_using_OS_shell' : ''}${execCurlFromJVM ? '_execCurlFromJVM' : ''}"
    }

    public setTrustedUserid(trustedUserid) {
        this.trustedUserid = trustedUserid
    }
    public setTrustedPassword(trustedPassword) {
        this.trustedPassword = trustedPassword
    }
    public setTokenProviderRootUrl(tokenProviderRootUrl) {
        this.tokenProviderRootUrl = tokenProviderRootUrl
    }
    public setTokenProviderServiceName(tokenProviderServiceName) {
        this.tokenProviderServiceName = tokenProviderServiceName
    }
    
    public setTokenProviderAuthTokenResource(tokenProviderAuthTokenResource) {
        this.tokenProviderAuthTokenResource = tokenProviderAuthTokenResource
    }
}
