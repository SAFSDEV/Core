package org.safs.selenium;


public class SGuiObject {
	
	private String locator;
	private String compType;
	private String windowId;
	private boolean dynamic;
	
	public SGuiObject(String loc, String type, String window, boolean dynamic) {
		this.locator = loc;
		this.compType = type;
		this.windowId = window;
		this.dynamic = dynamic;
	}
	
	public String getLocator(){
		return locator;
	}
	
	public String getCompType(){
		return compType;
	}
	
	public String getWindowId(){
		return windowId;
	}
	
	public void setLocator(String loc){
		this.locator = loc;
	}
	
	public void setWindowId(String window){
		this.windowId = window;
	}
	
	public void setCompType(String type){
		this.compType = type;
	}
	
	public String toString(){
		return windowId+":"+compType + ":" + locator;
	}

	public boolean isDynamic() {
		return dynamic;
	}

	public void setDynamic(boolean dynamic) {
		this.dynamic = dynamic;
	}
}
