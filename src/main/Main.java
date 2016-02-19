package main;

import org.osbot.rs07.api.ui.Message;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import antiban.Antiban;
import banking.Banking;
import dynamicArea.DynamicArea;
import fishing.Fishing;
import simpleGui.SimpleGui;

@ScriptManifest(author = "EmbeddedJ", info = "Simple Killer", name = "Beta Combat V0.4", version = .3, logo = "")
public class Main extends Script{
	Banking bank;
	Fishing fish;
	Antiban antiban;
	DynamicArea dynamicArea;
	
	@Override
	public void onStart() throws InterruptedException {
		bank = new Banking(this);
		fish = new Fishing(this);
		antiban = new Antiban(this);
		dynamicArea = new DynamicArea(this, this.fish);
		
		
		
		SimpleGui gui = new SimpleGui(this, this.fish, this.bank);
		gui.Setup();
		gui.Display();
	}
	
	@Override
	public int onLoop() throws InterruptedException {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public void onMessage(Message message){
		
	}
}
