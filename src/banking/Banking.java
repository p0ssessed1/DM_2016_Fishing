package banking;

import java.util.List;
import java.util.Random;

import org.osbot.rs07.api.filter.NameFilter;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.script.Script;

import main.Timer;

public class Banking {

	Random rn;
	Script script;
	NameFilter<Item> keepItems;

	Area bankArea;
	Timer t;

	public Banking(Script script) {
		this.script = script;
		rn = new Random(script.myPlayer().getId());
	}

	public boolean isInArea() {
		return bankArea.contains(script.myPosition());
	}

	public void setArea(Area a) {
		this.bankArea = a;
	}

	public Area getArea() {
		return bankArea;
	}

	public void setKeepItems(NameFilter<Item> keepItemsFilter) {
		this.keepItems = keepItemsFilter;
	}

	public boolean walkToArea() {
		if (!bankArea.contains(script.myPlayer())) {
			return script.getWalking().webWalk(bankArea);
		}
		return false;
	}

	private NPC getBanker() {
		NPC banker = script.getNpcs().closestThatContains("Bank");
		if (banker != null && banker.hasAction("Bank")) {
			int id = banker.getId();
			if (id != -1) {
				for (NPC i : script.getNpcs().get(banker.getX(), banker.getY())) {
					if (i.getId() == id) {
						return banker;
					}
				}
			}
		}
		return null;
	}

	private RS2Object getBankBooth() {
		RS2Object bank = script.getObjects().closestThatContains("Bank Booth");
		if (bank != null && bank.hasAction("Bank")) {
			return bank;
		}

		return null;
	}

	public boolean bank() {
		boolean isOpen = false;
		if (!bankArea.contains(script.myPlayer())) {
			if (!walkToArea()) {
				return false;
			}
		}

		if (script.random(0, 1) == 0) {
			NPC banker = getBanker();
			banker.interact("Bank");
		} else {
			RS2Object bank = getBankBooth();
			bank.interact("Bank");
		}
		t.reset();
		while (!(isOpen = script.getBank().isOpen()) && !t.timer(rn.nextInt(2000) + 1000))
			;
		if (!isOpen) {
			return false;
		}

		if (script.getBank().depositAllExcept(keepItems)) {
			return true;
		} else {
			return false;
		}
	}
}
