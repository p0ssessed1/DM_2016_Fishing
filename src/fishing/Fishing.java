package fishing;

import java.util.LinkedList;
import java.util.List;

import org.osbot.rs07.api.filter.ActionFilter;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.script.Script;

import dynamicArea.DynamicArea;
import main.Timer;

public class Fishing {

	/* TODO - Implement different fish types. */
	String fishName;
	SpotType spot;
	public Script script;
	DynamicArea dynamicArea;
	String action;

	List<Area> fishingAreas = new LinkedList<Area>();
	ActionFilter<NPC> actionFilter;
	ActionFilter<NPC> AllActions = new ActionFilter("Harpoon", "Cage", "Net", "Bait", "Lure");
	Timer t;

	public Fishing(Script script) {
		this.script = script;
		this.dynamicArea = new DynamicArea(this);
	}

	/**
	 * CODE SNIPPED: Optional<NPC> lobster =
	 * getNpcs().getAll().stream().filter(o -> o.hasAction("Cage")).min(new
	 * Comparator<NPC>() {
	 * 
	 * @Override public int compare(NPC a, NPC b) { return
	 *           getMap().distance(a.getPosition()) -
	 *           getMap().distance(b.getPosition()); } });
	 *           if(lobster.isPresent()){ lobster.get().interact("Cage"); }
	 */
	
	/**
	 * Walk to the closest fishing area. Uses WebWalk.
	 * 
	 * @return True if walking is successful.
	 * 		   False if already in area or unsuccessful.
	 */
	public boolean walkToArea(){
		if (!fishingAreas.contains(script.myPlayer())) {
			return script.getWalking().webWalk(dynamicArea.getClosestArea(fishingAreas));
		}
		else{
			return false;
		}
	}
	
	/**
	 * Attemps to fish with a timeout. Walks to nearest fishing area,
	 * Looks for fish that has been previously set up. Attemps to fish from that location
	 * 
	 * @return
	 * @throws InterruptedException
	 */
	public boolean fish() throws InterruptedException {
		boolean animated = false;
		
		if (isInArea()) {
			Timer local_timer = new Timer();
			while (!local_timer.timer(30000)) {
				NPC fishingSpot = script.getNpcs().closest(true, n -> actionFilter.match(n));
				if (isSpotValid(fishingSpot)) {
					List<FishAction> fishActionList = getFishFromSpot(fishingSpot);
					if (fishActionList.contains(fishName)) {
						fishingSpot.interact(action);
						t.reset();
						while (!script.myPlayer().isMoving() && !t.timer(Script.random(5000, 6000))) {
							Script.sleep(Script.random(100, 200));
						}
						t.reset();
						while (!(animated = script.myPlayer().isAnimating()) && !t.timer(Script.random(2500, 3000))) {
							Script.sleep(Script.random(100, 200));
						}
						return animated;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Checks if spot is valid as a fishing spot.
	 * 
	 * @param npc
	 * @return True : Fishing spot valid. False : Not a fishing spot.
	 */
	public boolean isSpotValid(NPC npc) {
		if (npc != null) {
			if (!npc.hasAction("Attack") && AllActions.match(npc)) {
				int id = npc.getId();
				if (id != -1) {
					for (NPC i : script.getNpcs().get(npc.getX(), npc.getY())) {
						if (i.getId() == id) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * Return if the player.
	 * 
	 * @return True : Player is in the fishing area. False: Player is not in the
	 *         fishing area.
	 */
	public boolean isInArea() {
		return fishingAreas.contains(script.myPosition());
	}

	/**
	 * Set action locally.
	 * 
	 * @param action
	 *            : Action to set.
	 */
	private void setAction(String action) {
		this.action = action;
		actionFilter = new ActionFilter<NPC>(action);
	}

	/**
	 * Set the type of fish that one wishes to fish. Also sets the action that
	 * corresponds to the fish and the type of spot.
	 * 
	 * @param fish
	 *            : Name of fish to fish.
	 */
	public void setFish(String fish) {
		this.fishName = fish;
		setAction(getAction(fish));
		this.spot = getSpotType(fish);
	}

	/**
	 * Function relates the fishing spot with what fish are contained there and
	 * what action corresponds to getting each fish.
	 * 
	 * @param spot
	 *            : The spot to query.
	 * @return List<FishAction> A list of fish names and actions relating to the
	 *         spot provided.
	 */
	public List<FishAction> getFishFromSpot(NPC spot) {
		String[] actions = spot.getActions();
		List<FishAction> fish_list = new LinkedList<FishAction>();
		switch (actions[0]) {
		case "Cage":
			fish_list.add(new FishAction("Lobster", "Cage"));
			fish_list.add(new FishAction("Swordfish", "Harpoon"));
			fish_list.add(new FishAction("Tuna", "Harpoon"));
			break;
		case "Net":
			if (actions[1] == "Bait") {
				fish_list.add(new FishAction("Anchovies", "Net"));
				fish_list.add(new FishAction("Shrimp", "Net"));
				fish_list.add(new FishAction("Sardines", "Bait"));
				fish_list.add(new FishAction("Herring", "Bait"));
			} else if (actions[1] == "Harpoon") {
				fish_list.add(new FishAction("Shark", "Harpoon"));
				fish_list.add(new FishAction("Mackerel", "Net"));
				fish_list.add(new FishAction("Bass", "Net"));
			}
			break;
		case "Lure":
			fish_list.add(new FishAction("Raw Salmon", "Lure"));
			fish_list.add(new FishAction("Raw Trout", "Lure"));
			fish_list.add(new FishAction("Raw Pike", "Bait"));
			break;
		}
		return fish_list;
	}

	/**
	 * Returns the action to perform to obtain the type of fish passed.
	 * 
	 * @param fish
	 *            name : Name of fish to get action for.
	 * @return String : Action to perform to get the fish.
	 */
	private String getAction(String fish_name) {
		String local_action = null;

		switch (fish_name) {

		case "Lobster":
			local_action = "Cage";
			break;
		case "Raw Tuna":
		case "Raw Swordfish":
		case "Raw Shark":
			local_action = "Harpoon";
			break;
		case "Raw Pike":
		case "Raw Herring":
		case "Raw Sardines":
			local_action = "Bait";
			break;
		case "Raw Trout":
		case "Raw Salmon":
			local_action = "Lure";
			break;
		case "Raw Shrimps":
		case "Raw Anchovies":
		case "Raw Bass":
		case "Raw Mackerel":
		case "Raw Cod":
			local_action = "Net";
			break;
		}

		return local_action;
	}

	/**
	 * Returns the spot type of the fish to obtain the type of fish passed.
	 * 
	 * @param fish
	 *            name : Name of fish to get action for.
	 * @return String : Action to perform to get the fish.
	 */
	private SpotType getSpotType(String fish_name) {
		SpotType sType = null;

		switch (fish_name) {
		case "Tuna":
		case "Swordfish":
		case "Lobster":
			sType = new SpotType("Cage", "Harpoon");
			break;
		case "Bass":
		case "Mackerel":
		case "Cod":
		case "Shark":
			sType = new SpotType("Net", "Harpoon");
			break;
		case "Shrimps":
		case "Anchovies":
		case "Herring":
		case "Sardines":
			sType = new SpotType("Net", "Bait");
			break;
		case "Pike":
		case "Trout":
		case "Salmon":
			sType = new SpotType("Bait", "Lure");
			break;
		}

		return sType;
	}
}
