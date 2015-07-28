package org.safs.tools.status;
public class TestRecordStatusInfo implements StatusInfoInterface {

	protected StatusInterface     statusinfo     = null;
	protected TestRecordInterface testrecordinfo = null;

	/**
	 * Constructor for TestRecordStatusInfo
	 */
	public TestRecordStatusInfo() {
		super();
	}

	/**
	 * PREFERRED Constructor for TestRecordStatusInfo
	 */
	public TestRecordStatusInfo(StatusInterface statusinfo, 
	                            TestRecordInterface testrecordinfo) {
		this();
		setStatusInterface(statusinfo);
		setTestRecordInterface (testrecordinfo);
	}

	/**
	 * Set the statusinfo to the provided value.
	 */
	public void setStatusInterface(StatusInterface statusinfo) {
		this.statusinfo = statusinfo;
	}

	/**
	 * @see StatusInfoInterface#getStatusInterface()
	 */
	public StatusInterface getStatusInterface() {
		return statusinfo;
	}

	/**
	 * Set the testrecordinfo to the provided value.
	 */
	public void setTestRecordInterface(TestRecordInterface testrecordinfo) {
		this.testrecordinfo = testrecordinfo;
	}

	/**
	 * @see StatusInfoInterface#getTestRecordInterface()
	 */
	public TestRecordInterface getTestRecordInterface() {
		return testrecordinfo;
	}

}

