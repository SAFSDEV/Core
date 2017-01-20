package org.safs.rest.service.models.consumers

import org.safs.rest.service.commands.curl.CurlInvoker
import org.safs.rest.service.commands.curl.Response
import org.safs.rest.service.commands.curl.CurlCommand

import java.util.Map

import org.apache.hc.core5.testing.framework.ClientPOJOAdapter;
import org.apache.hc.core5.testing.framework.ClientTestingAdapter
import org.apache.hc.core5.testing.framework.TestingFrameworkException;
import org.apache.hc.core5.testing.framework.TestingFrameworkRequestHandler;
import org.apache.hc.core5.testing.framework.TestingFramework

/**
 *
 * <p>This adapter assists the testing of an HTTP client.  This adapter in turn uses an
 * {@link SafsrestAdapter} to actually use the HTTP client to make the request.
 * See {@link SafsrestAdapter} to see the format of the request and the returned
 * response.  The format of the returned response is also the format of the parameter
 * called responseExpectations.</p>
 *
 * <p>This adapter will generally call the {@link SafsrestAdapter} methods of the same
 * name when these methods are called:</p>
 *
 * <pre>
 *
 * isRequestSupported
 * modifyRequest
 * execute
 *
 * </pre>
 *
 * <p>See these method's documentation in {@link SafsrestAdapter} for details.</p>
 *
 * <p>The value that this adapter adds is with the modifyResponseExpectations method.  Each
 * test will specify the response that is expected.  This adapter is able to modify the expectations 
 * in the modifyResponseExpectations method.</p>
 *
 * @since 5.0
 */
class SafsrestTestingAdapter extends ClientTestingAdapter {
    public SafsrestTestingAdapter() {
        /*
         * This adapter will perform the HTTP request and return the response in the
         * expected format.
         */
        super(new SafsrestAdapter())
    }



    /**
     * See the documentation for the same method in {@link SafsrestAdapter}.  This
     * method will typically call it.  However, this method also has access to the
     * test's response expectations if that is needed for some reason.  Furthermore,
     * this method also has access to the in-process HttpServer's request handler so
     * it can optionally call assertNothingThrown() before checking the response
     * further.  It is optional because the test framework will call it later.
     *
     * @param defaultURI           See execute method of {@link SafsrestAdapter}.
     * @param request              See execute method of {@link SafsrestAdapter}.
     * @param requestHandler       The request handler that checks the received HTTP request
     *                             with the request that was intended.  If there is a
     *                             mismatch of expectations, then the requestHandler will
     *                             throw an exception.  If this execute method does not want
     *                             to make further checks of the response in the case
     *                             the responseHandler threw, then the assertNothingThrown()
     *                             method should be called before doing further checks.
     * @param responseExpectations The response expectations of the test.
     * @return See return of the execute method of {@link HttpClientPOJOAdapter}.
     * @throws HttpServerTestingFrameworkException in the case of a problem.
     */
    public Map<String, Object> execute(
            String defaultURI,
            Map<String, Object> request,
            TestingFrameworkRequestHandler requestHandler,
            Map<String, Object> responseExpectations)
    throws TestingFrameworkException {

        def response = adapter.execute(defaultURI, request)

        requestHandler.assertNothingThrown()

        // TODO: check headers - SAFSREST does not give the ability to check all headers - just
        // specific ones like content type.

        // If the test expects a charset to be in the contentType, then return
        // the version that has the charset.  Otherwise, return the version
        // that does not have the charset.
        def charsetExpected = responseExpectations.contentType.contains("charset")
        if (! charsetExpected) {
            response.contentType = response.contentTypeWithoutCharset
        }
        response
    }

    void setUseScript(useScript) {
        adapter.setUseScript(useScript)
    }
    
    void setExecCurlFromJVM(execCurlFromJVM) {
        adapter.setExecCurlFromJVM(execCurlFromJVM)
    }
}
