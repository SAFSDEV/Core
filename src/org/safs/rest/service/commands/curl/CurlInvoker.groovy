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

package org.safs.rest.service.commands.curl

import java.nio.charset.CharsetDecoder
import java.nio.charset.CharsetEncoder

import org.apache.hc.client5.http.impl.sync.HttpClients;
import org.apache.hc.client5.http.methods.RequestBuilder
import org.apache.hc.core5.http.ClassicHttpResponse
import org.apache.hc.core5.http.ContentLengthStrategy
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.config.ConnectionConfig
import org.apache.hc.core5.http.config.H1Config;
import org.apache.hc.core5.http.impl.BasicHttpTransportMetrics
import org.apache.hc.core5.http.impl.ConnSupport;
import org.apache.hc.core5.http.impl.DefaultContentLengthStrategy
import org.apache.hc.core5.http.impl.io.AbstractMessageParser;
import org.apache.hc.core5.http.impl.io.ContentLengthOutputStream
import org.apache.hc.core5.http.impl.io.DefaultHttpResponseWriterFactory;
import org.apache.hc.core5.http.impl.io.SessionInputBufferImpl;
import org.apache.hc.core5.http.impl.io.SessionOutputBufferImpl
import org.apache.hc.core5.http.io.HttpMessageWriter
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicLineParser;
import org.apache.hc.core5.http.message.LineParser
import org.apache.hc.core5.util.CharArrayBuffer
import org.safs.rest.service.commands.CommandInvoker
import org.safs.rest.service.commands.CommandResults
import org.safs.rest.service.commands.ExecutableCommand
import org.safs.rest.service.commands.curl.error.CurlErrorDetailsProvider

import groovy.util.logging.Slf4j


/**
 * CurlInvoker provides the ability to execute a CurlCommand and get a Response.
 *
 * <p>
 *      <strong>NOTE</strong>: SAFSREST consumers and test authors
 *      should <strong>NOT</strong> directly use CurlInvoker. Instead, use
 *      {@link RestConsumer} to interact with a REST API.
 * </p>
 *
 * @author Bruce.Faulkner
 * @since 0.0.1
 * @see RestConsumer
 *
 */
@Slf4j
class CurlInvoker {
    public static final CharSequence ECHO_EXECUTABLE = 'echo'

    public static final CharSequence ERROR_NO_CURL_COMMAND =
            'ERROR: CurlCommand parameter must not be null when attempting to call the CurlInvoker execute() method'

    /**
     * An instance of CommandInvoker may be injected via a constructor named
     * argument; otherwise, one will be created when the
     * {@link #execute(org.safs.rest.service.commands.curl.CurlCommand)}
     * method gets called.
     */
    @Delegate(excludes = ['execute'])
    CommandInvoker commandInvoker = null


    /**
     * Runs the curl command line represented by the curlCommand parameter, and
     * returns a {@link Response}
     *
     * @param curlCommand an instance of a {@link CurlCommand} representing the
     * desired request to be sent to a REST API.
     * @return an instance of a {@link Response} object with the results from
     * executing the {@link CurlCommand}
     */
    def execute(CurlCommand curlCommand) {
        CommandResults results

        if (!commandInvoker) {
            commandInvoker = new CommandInvoker()
        }

        if (curlCommand) {
            try {
                if (curlCommand.requestBody) {
                    // the next call will return null if execCurlFromJVM == false
                    results = pipeWithRequestBody curlCommand
                } else {
                    results = commandInvoker.execute curlCommand
                }
            } catch (Throwable t) {
                if (commandInvoker.useScript) {
                    // useScript == true is used during testing to make sure the logged bash or CMD
                    // command actually works.  If it fails, rethrow the exception.
                    throw t
                }
                if (t instanceof IOException) {
                    // curl is probably not on the PATH.
                    // Use the Apache HttpClient to make the call.
                    results = null
                } else {
                    throw t
                }
            }
            if (results == null) {
                // Use the Apache HttpClient to make the call.
                results = useHttpClient curlCommand
            }
        } else {
            log.error ERROR_NO_CURL_COMMAND
            throw new IllegalStateException(ERROR_NO_CURL_COMMAND)
        }

        logCurlError results

        // Create a response object from the results, even if a curl error occurred, to match existing
        // SAFSREST behavior to avoid breaking existing tests or silently registering a false positive.
        Response response = new Response(commandResults: results)

        response
    }


    private def pipeWithRequestBody(curlCommand) {
        CommandResults results

        def echoParameters = [
            executable: ECHO_EXECUTABLE,
            options: ['-n'], // don't output the trailing newline
            data: /'${curlCommand.requestBody}'/
        ]

        ExecutableCommand echoCommand = new ExecutableCommand(echoParameters)

        results = pipeTo echoCommand, curlCommand

        results
    }


    /**
     * If a curl error occurred when attempting to execute a {@link CurlCommand}, then log it.
     *
     * @param results the results of executing a {@link CurlCommand}.
     */
    private void logCurlError(CommandResults results) {
        CurlErrorDetailsProvider curlErrorDetailsProvider = new CurlErrorDetailsProvider(curlResults: results)

        // The log.error() method will always print something, even when the
        // curl exit code is 0 (SUCCESS). In the case of SUCCESS, the details
        // message should be an empty String, so a blank line will be printed
        // in the console/log immediately after the curl command. To preserve
        // existing behavior, only log the details if the results indicate an
        // error actually occurred.
        if (curlErrorDetailsProvider.isError()) {
            log.error curlErrorDetailsProvider.details
        }

    }

    private useHttpClient(curlCommand) {
        def commandList = (makeNormalizedCommandList(curlCommand.commandList)).list

        def methodName
        commandList.each { parm ->
            switch (parm) {
                case CurlCommand.HEAD_OPTION: methodName = "head"; break
                case CurlCommand.DELETE_OPTION: methodName = "delete"; break
                case CurlCommand.POST_OPTION: methodName = "post"; break
                case CurlCommand.PUT_OPTION: methodName = "put"; break
                case CurlCommand.PATCH_OPTION: methodName = "patch"; break
            }

        }
        if (methodName == null) {
            methodName = "get"
        }
        /*
         * Use reflection to call a static method on RequestBuilder that is the
         * lowercase of the HTTP method.
         */
        def method = RequestBuilder.class.getMethod(methodName);
        RequestBuilder builder = ((RequestBuilder) method.invoke(null, null));
        def uri = commandList[-1]
        builder = builder.setUri(uri)

        def headersMultiLineString = ""
        commandList.each { parm ->
            if (parm.startsWith(CurlCommand.HEADER_OPTION)) {
                headersMultiLineString += parm[2..-1] + "\n"
            }
        }
        def headers = parseHeadersInMultiLineString(headersMultiLineString)
        def requestContentType
        headers.each { header ->
            if (header.name.equalsIgnoreCase(HttpHeaders.CONTENT_TYPE)) {
                requestContentType = header.value
            }
            if (header.name.equalsIgnoreCase(HttpHeaders.CONTENT_LENGTH)) {
                // don't add these headers - let httpclient do it.
            } else {
                builder = builder.addHeader(header)
            }
        }
        if (curlCommand.rawRequestBody) {
            def entity = requestContentType != null ?
                         new StringEntity(curlCommand.rawRequestBody, ContentType.parse(requestContentType)) :
                         new StringEntity(curlCommand.rawRequestBody);
            builder = builder.setEntity(entity);
        }

        def httpclient = HttpClients.createDefault()
        def response = httpclient.execute(builder.build())

        // Write the response to an output stream similar to the way HttpCore5 does it.
        def baos = new ByteArrayOutputStream()


        BasicHttpTransportMetrics outTransportMetrics = new BasicHttpTransportMetrics();

        ConnectionConfig cconfig = ConnectionConfig.DEFAULT
        int buffersize = cconfig.getBufferSize()
        H1Config h1Config = H1Config.DEFAULT
        CharsetEncoder charencoder = ConnSupport.createEncoder(cconfig)

        def outbuffer = new SessionOutputBufferImpl(outTransportMetrics, buffersize,
                                                    h1Config.getChunkSizeHint(), charencoder);

        HttpMessageWriter<ClassicHttpResponse> responseWriter = DefaultHttpResponseWriterFactory.INSTANCE.create()
        responseWriter.write(response, outbuffer, baos)
		outbuffer.flush(baos)

		// write the body if it exists
		def entity = response.getEntity()
		if (entity != null) {
			ContentLengthStrategy outgoingContentStrategy = DefaultContentLengthStrategy.INSTANCE
			long len = outgoingContentStrategy.determineLength(response)

			OutputStream outstream = new ContentLengthOutputStream(outbuffer, baos, len)
			entity.writeTo(outstream);
			outstream.close()
		}

        baos.close()

        String stdOut = baos.toString("UTF-8")

        def exitValue = 0
        def stdErr = ""
        CommandResults results =
            new CommandResults(output: stdOut, error: stdErr, exitValue: exitValue)

        log.debug  results.commandResultsString

        results
    }

    /**
     * Utility method to convert headers multi-line string to a Map
     * @param headers Multi-line String containing complete header info
     * @return Map of String
     */
     public static Map<String,String> getHeadersMapFromMultiLineString(String headers) {

        Header[] headersList = parseHeadersInMultiLineString(headers);

        // Now, return the headers as Collection<String>.
        Map<String, String> ret = new HashMap<String,String>();
        for (Header header : headersList) {
            ret.put(header.getName(), header.getValue());
        }
        return ret;
    }


    private static Header[] parseHeadersInMultiLineString(String headerString) {

        /*
         * This is pretty close to how Apache HTTP Core5 parses headers.
         */
        ByteArrayInputStream inputStream = new ByteArrayInputStream(headerString.getBytes("UTF-8"));

        int buffersize = org.apache.hc.core5.http.config.ConnectionConfig.DEFAULT.getBufferSize();
        BasicHttpTransportMetrics inTransportMetrics = new BasicHttpTransportMetrics();
        final H1Config H1CONFIG = H1Config.DEFAULT;
        final CharsetDecoder CHARDECODER = null;
        SessionInputBufferImpl inbuffer = new SessionInputBufferImpl(inTransportMetrics, buffersize, -1,
                                                                     H1CONFIG.getMaxLineLength(), CHARDECODER);
        final LineParser LINEPARSER = BasicLineParser.INSTANCE;

        List<CharArrayBuffer> headerLines = new ArrayList<>();
        final Header[] HEADERS = AbstractMessageParser.parseHeaders(
            inbuffer,
            inputStream,
            H1CONFIG.getMaxHeaderCount(),
            H1CONFIG.getMaxLineLength(),
            LINEPARSER,
            headerLines
        );
        return HEADERS;
    }
}
