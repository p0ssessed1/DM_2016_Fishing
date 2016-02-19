package banking;

import java.util.List;

import org.osbot.rs07.api.filter.NameFilter;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.script.Script;

public class Banking {

	Script script;
	NameFilter<Item> keepItems;
	final int UP_KEY = 38;
	final int LEFT_KEY = 37;
	final int DOWN_KEY = 40;
	final int RIGHT_KEY = 39;
	
	Area bankArea;


	public Banking(Script script){
		this.script = script;
	}
	
	public boolean isIn(){
		return bankArea.contains(script.myPosition());
	}
	
	public void setArea(Area a){
		this.bankArea = a;
	}
	
	public Area getArea(){
		return bankArea;
	}
	
	public void setKeepItems(NameFilter<Item> l){
		this.keepItems = l;
	}
}
