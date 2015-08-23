package com.peeyush.apppactera.ui;

/*
 * Class defining data structure of each list item
 */
public class RowData {

	private String title = "";
	private String desc = "";
	private String imageUrl = "";
	
	/**
     * Class defining data structure of each list item
     * @param title row title
     * @param desc row description
     * @param imageUrl row image url
     */
	RowData(String title, String desc, String imageUrl){
		
		this.title = title;
		this.desc = desc;
		this.imageUrl = imageUrl;
	}
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public String getImageUrl() {
		return imageUrl;
	}
	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
	
}
