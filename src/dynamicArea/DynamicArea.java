package dynamicArea;

import java.util.LinkedList;
import java.util.List;

import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.script.Script;
import fishing.Fishing;

public class DynamicArea {
	final int MAX_AREA_SIZE = 3;
	
	Fishing fish;

	public DynamicArea(Fishing fish){
		this.fish = fish;
	}

	public List<Area> CreateAreas(List<NPC> spots) {
		List<Area> areas = new LinkedList<Area>();

		for (NPC n : spots) {
			if (fish.isSpotValid(n)) {
				areas.add(n.getArea(MAX_AREA_SIZE));
			}
		}

		return areas;
	}
	
	/**
	 * Get the closest area in which to fish that does not contain the player.
	 * 
	 * @return Area: The area in which to fish.
	 */
	public Area getClosestArea(List<Area> areas) {
		Position myPosition = fish.script.myPlayer().getPosition();
		Position tryPosition = areas.get(0).getRandomPosition();
		Area closestArea = null;
		
		for(Area a: areas)
		{
			if(myPosition.distance(a.getRandomPosition()) < myPosition.distance(tryPosition)
					&& !a.contains(fish.script.myPlayer())){
				tryPosition = a.getRandomPosition();
				closestArea = a;
			}
		}
		return closestArea;
	}
}
