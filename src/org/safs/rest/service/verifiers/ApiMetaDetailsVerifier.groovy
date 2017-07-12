// Copyright (c) 2016 by SAS Institute Inc., Cary, NC, USA. All Rights Reserved.

package org.safs.rest.service.verifiers

import groovy.util.logging.Slf4j

import org.safs.rest.service.commands.curl.Response



/**
 * Provides convenience methods to verify the details returned from retrieving
 * the /apiMeta resource from a REST API service.
 *
 * @author Bruce.Faulkner
 * @since December 16, 2015
 */
@Slf4j
class ApiMetaDetailsVerifier {
    public static final RESOURCE_LIST_DEPRECATED_MESSAGE = '''
        |   Warning: the resources property of the /apiMeta response has been deprecated.
        |       Because the response does contain the resources property, the list of resources will be verified.
        |       However, consider contacting the responsible developer to have the resources property removed to comply
        |       with current SAS REST API standards (see http://myhost.sas.com/wikipedia/Implementing_REST_APIs#API_metadata).
        |       resources property: {}
        '''.stripMargin('|')

    public static final MISSING_RESOURCE_LIST_MESSAGE = 'Warning: the resourceList does not exist or has no items. resourceList: {}'

    public static final DEFAULT_MEDIA_TYPE_VERSION = 1

    public static final MEDIA_TYPES_LIST_MINIMUM_SIZE = 0
    public static final EXPECTED_STANDARD_PROPERTIES_MINIMUM_SIZE = 1

    /**
     * The first year of Petrichor development.
     */
    public static final DEVELOPMENT_START_YEAR_FLOOR = 2014

    public static final LABEL_PROPERTY = 'label'

    public static final DESCRIPTION_PROPERTY = 'description'

    public static final VERSION_PROPERTY = 'version'
    public static final TIME_STAMP_PROPERTY = 'timeStamp'
    public static final BRANCH_PROPERTY = 'branch'
    public static final COMMIT_ID_PROPERTY = 'commitId'
    public static final COMMIT_TIME_STAMP_PROPERTY = 'commitTimeStamp'

    public static final BUILD_INFO_PROPERTIES = [
            VERSION_PROPERTY,
            TIME_STAMP_PROPERTY,
            BRANCH_PROPERTY,
            COMMIT_ID_PROPERTY,
            COMMIT_TIME_STAMP_PROPERTY
    ]



    /**
     * By default, there should be at least 1 top level collection or collection container resource found.
     */
    // NOTE: The explicit public scope qualifier has been intentionally left off from this definition
    // to force a getter to be generated. The getter needs to exist so that the default value can
    // be set appropriately when ApiMetaDetailsVerifier exists as a @Delegate in another class.
    static final EXPECTED_RESOURCES_MINIMUM_SIZE = 1


    @Deprecated
    boolean strict = false



    /**
     * Verifies the basic, general details of the response body returned from
     * performing a GET against the /apiMeta resource of a service. Performs
     * assertions about these details, so an AssertionError might be thrown.
     *
     * @param apiMetaDetails the Groovy (Map) representation of a response body
     * {@link Response#body} resulting from making a request for the apiMeta resource.
     * The response body should be slurped into JSON and that resulting object
     * should be passed as the apiMetaDetails parameter.
     * @param resourceList a List containing URIs of the resources to verify.
     * @param expectedResourcesMinimumSize the (optional) expected minimum number
     * of resources found in the response for an /apiMeta request. Defaults to
     * {@link #EXPECTED_RESOURCES_MINIMUM_SIZE}
     *
     * @see org.safs.rest.service.models.entrypoints.Entrypoint
     * @see org.safs.rest.service.commands.curl.Response
     */
    // NOTE: The default value for expectedResourcesMinimumSize has been qualified with the class
    // name to reduce the potential for a MissingPropertyException when a subclass of CoreRestSpecification
    // invokes this method through the Groovy @Delegate mechanism.
    void verifyApiMetaDetails(
            Map apiMetaDetails,
            List resourceList,
            int expectedResourcesMinimumSize = ApiMetaDetailsVerifier.EXPECTED_RESOURCES_MINIMUM_SIZE
    ) {
        resourceList?.each { resourceToVerify ->
            verifyStandardApiMetaProperties apiMetaDetails, resourceToVerify, expectedResourcesMinimumSize
        }

        verifyBuildInfoApiMetaProperties apiMetaDetails
    }


    /**
     * Verifies the basic, general details of the response body returned from
     * performing a GET against the /apiMeta resource of a service. Performs
     * assertions about these details, so an AssertionError might be thrown.
     *
     * @param apiMetaDetails the Groovy (Map) representation of a response body
     * {@link Response#body} resulting from making a request for the apiMeta resource.
     * The response body should be slurped into JSON and that resulting object
     * should be passed as the apiMetaDetails parameter.
     * @param serviceResource CharSequence representing the URI of the resource to
     * verify
     * @param expectedResourcesMinimumSize the (optional) expected minimum number
     * of resources found in the response for an /apiMeta request. Defaults to
     * {@link #EXPECTED_RESOURCES_MINIMUM_SIZE}
     *
     * @see org.safs.rest.service.models.entrypoints.Entrypoint
     * @see org.safs.rest.service.commands.curl.Response
     */
    // NOTE: The default value for expectedResourcesMinimumSize has been qualified with the class
    // name to reduce the potential for a MissingPropertyException when a subclass of CoreRestSpecification
    // invokes this method through the Groovy @Delegate mechanism.
    void verifyApiMetaDetails(
            Map apiMetaDetails,
            CharSequence serviceResource,
            int expectedResourcesMinimumSize = ApiMetaDetailsVerifier.EXPECTED_RESOURCES_MINIMUM_SIZE
    ) {
        verifyStandardApiMetaProperties apiMetaDetails, serviceResource, expectedResourcesMinimumSize
        verifyBuildInfoApiMetaProperties apiMetaDetails
    }


    /**
     * Verify the "standard" properties that should be present in the response from a GET request to /apiMeta.
     * The wikipedia guide to implementing REST APIs lists the "standard" properties
     * <a href="http://myhost.sas.com/wikipedia/Implementing_REST_APIs#apiMeta">here</a>.
     *
     * @param apiMetaDetails a Map containing the parsed details from the {@link Response}
     * @param serviceResource a top-level collection or container for the service under test
     * @param expectedResourcesMinimumSize the minimum number of resources expected in the resources collection
     * contained in apiMetaDetails
     */
    void verifyStandardApiMetaProperties(Map apiMetaDetails, CharSequence serviceResource, int expectedResourcesMinimumSize) {
        assert apiMetaDetails
        assert apiMetaDetails.size() >= EXPECTED_STANDARD_PROPERTIES_MINIMUM_SIZE

        // According to the implementation details for API metadata
        // (http://myhost.sas.com/wikipedia/Implementing_REST_APIs#API_metadata), the only property
        // required to be present and have a value is the developmentStartYear
        assert apiMetaDetails.developmentStartYear >= DEVELOPMENT_START_YEAR_FLOOR

        assert apiMetaDetails.mediaTypes

        verifyApiMetaResources apiMetaDetails, serviceResource, expectedResourcesMinimumSize

        verifyApiMetaMediaTypes apiMetaDetails
    }


    /**
     * Verifies the resources collection in the Response from a GET request to /apiMeta.
     *
     * @param apiMetaDetails a Map containing the parsed details from the {@link Response}
     * @param serviceResource a top-level collection or container for the service under test
     * @param expectedResourcesMinimumSize the minimum number of resources expected in the resources collection
     * contained in apiMetaDetails
     */
    void verifyApiMetaResources(Map apiMetaDetails, CharSequence serviceResource, int expectedResourcesMinimumSize) {
        Collection resources = apiMetaDetails.resources

        // On 02 August 2016, the SAS REST API standards have been updated to deprecate the resources
        // property of the /apiMeta response, along with the separate /apiMeta/resources resource. Instead,
        // the new resource /apiMeta/api (which returns an Open API Specification [Swagger] document)
        // should be used.
        //
        // As of 22 August 2016, many services still return the resources field, but some have stopped
        // returning it. Accordingly, the verification of the resources list can be done only if it is
        // present and has one or more entries. Otherwise, log a warning message to alert a test author
        // that no resources were present.
        //
        // Eventually, some future ***REMOVED*** story will address verifying the correct API metadata.

        if (resources?.size() > 0) {
            log.warn RESOURCE_LIST_DEPRECATED_MESSAGE, resources.inspect()

            verifyResourcesMinimumSize resources, expectedResourcesMinimumSize
            verifyResource resources, serviceResource
        } else {
            log.warn MISSING_RESOURCE_LIST_MESSAGE, resources.inspect()
        }
    }

    /**
     * Verifies the media types and media type names in the given apiMetaDetails map.
     *
     * @param apiMetaDetails a Map containing the parsed details from the {@link Response}
     */
    void verifyApiMetaMediaTypes(Map apiMetaDetails) {
        Collection mediaTypes = apiMetaDetails.mediaTypes

        assert mediaTypes?.size() >= MEDIA_TYPES_LIST_MINIMUM_SIZE

        mediaTypes.each { mediaTypeElement ->
            assert mediaTypeElement.name
            assert mediaTypeElement.version >= DEFAULT_MEDIA_TYPE_VERSION
        }

        Collection mediaTypeNames = apiMetaDetails.mediaTypeNames
        assert mediaTypeNames?.size() >= MEDIA_TYPES_LIST_MINIMUM_SIZE
    }


    /**
     * Verifies the build information properties that should be present in the response from a GET request
     * to /apiMeta. The wikipedia guide to implementing REST APIs lists the build information properties
     * at the bottom of <a href="http://myhost.sas.com/wikipedia/Implementing_REST_APIs#apiMeta">this section</a>.
     *
     * @param apiMetaDetails a Map containing the parsed details from the {@link Response}
     */
    void verifyBuildInfoApiMetaProperties(Map apiMetaDetails) {
        assert apiMetaDetails.serviceId

        // Because the verifier currently doesn't care about the specific value, and the description property might
        // be null, then only check to see that the property name is present without concern for the value.
        assert apiMetaDetails.keySet().contains(DESCRIPTION_PROPERTY)

        def buildInfo = apiMetaDetails.build
        assert buildInfo

        def buildInfoKeys = buildInfo.keySet()

        BUILD_INFO_PROPERTIES.each { key ->
            assert key in buildInfoKeys
        }
    }


    /**
     * Asserts that the actual resources collection contains at least the
     * number of items specified by the expectedResourcesMinimumSize
     * parameter.
     *
     * @param resources the collection of resources from an API meta
     * details object
     * @param expectedResourcesMinimumSize the minimum number of resources
     * expected to be in the collection
     */
    // NOTE: The default value for expectedResourcesMinimumSize has been qualified with the class
    // name to reduce the potential for a MissingPropertyException when a subclass of CoreRestSpecification
    // invokes this method through the Groovy @Delegate mechanism.
    void verifyResourcesMinimumSize(
            Collection resources,
            int expectedResourcesMinimumSize = ApiMetaDetailsVerifier.EXPECTED_RESOURCES_MINIMUM_SIZE
    ) {
        assert resources?.size() >= expectedResourcesMinimumSize
    }


    /**
     * Asserts that the resourceToVerify is indeed present in the resources
     * collection of an API meta details object.
     *
     * @param resources the collection of resources from an API meta details
     * object
     * @param resourceToVerify a CharSequence representing a URI that should
     * be present in the collection
     */
    void verifyResource(Collection resources, CharSequence resourceToVerify) {
        assert findResource(resources, resourceToVerify)
    }


    /**
     * Attempts to find a resource from the resources collection of an
     * API meta details object.
     *
     * @param resources the resources collection from an API meta details
     * object
     * @param resourceToFind a CharSequence containing a URI representation
     * of a resource. The findResource() method looks for an exact match
     * of resourceToFind in the resources collection.
     *
     * @return if found, the resource matching resourceToFind; null otherwise
     */
    def findResource(Collection resources, CharSequence resourceToFind) {
        def possibleResource = resources.find { resource ->
            resource == resourceToFind
        }

        possibleResource
    }
}
