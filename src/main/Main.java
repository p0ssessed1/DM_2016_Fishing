package main;

import org.osbot.rs07.api.ui.Message;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import antiban.Antiban;
import banking.Banking;
import dynamicArea.DynamicArea;
import fishing.Fishing;
import simpleGui.SimpleGui;

@ScriptManifest(author = "EmbeddedJ", info = "Dynamic Fisher", name = "Beta Dynamic Fisher v0.1", version = .1, logo = "")
public class Main extends Script {
	Banking bank;
	Fishing fish;
	Antiban antiban;

	@Override
	public void onStart() throws InterruptedException {
		bank = new Banking(this);
		fish = new Fishing(this);
		antiban = new Antiban(this, fish);
		getKeyboard().initializeModule();
		getCamera().initializeModule();

		SimpleGui gui = new SimpleGui(this, this.fish, this.bank);
		gui.Setup();
		gui.Display();
	}

	@Override
	public int onLoop() throws InterruptedException {
		if (fish.fish()) {
			log("Fishing.");
			if (antiban.AntibanHandler()) {
				log("Performed Antiban");
			} else{
				log("Antiban Failed.");
			}
		} else{
			log("Fishing Failse");
		}

		if (getInventory().isFull()) {
			if (bank.bank()) {
				log("Banking Succesful");
			}
			else{
				log("Banking Failed");
			}
		}

		return 0;
	}

	@Override
	public void onMessage(Message message) {

	}
}
